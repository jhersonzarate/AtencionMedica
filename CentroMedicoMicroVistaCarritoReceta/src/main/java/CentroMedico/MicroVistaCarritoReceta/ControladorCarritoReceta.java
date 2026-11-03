package CentroMedico.MicroVistaCarritoReceta;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorCarritoReceta {
    private static final String SESION = "medico";
    private static final String HISTORIAL = "idHistorial";
    private static final String CITA = "idCita";
    private static final int MAX_LINEAS = 10; // el mismo tope que valida MicroRecetaCompuesto

    @Autowired
    private Servicio s;

    @Value("${vista.secreto}")
    private String secreto;

    @Value("${vista.validarmedico.url}")
    private String urlIngreso;

    @Value("${vista.historial.url}")
    private String urlHistorial;

    @Value("${vista.cita.url}")
    private String urlCita;

    @Value("${vista.analisis.url}")
    private String urlAnalisis;

    // Llegada desde la vista de cita (o desde la de análisis): pase firmado + historial + cita.
    @GetMapping("/entrar")
    public String entrar(@RequestParam(defaultValue = "") String t,
                         @RequestParam(defaultValue = "") String idHistorial,
                         @RequestParam(defaultValue = "") String idCita,
                         HttpServletRequest request) {
        Medico m = TokenMedico.leer(t, secreto);
        if (m == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        HttpSession sesion = request.getSession();
        request.changeSessionId();
        sesion.setAttribute(SESION, m);
        sesion.removeAttribute(HISTORIAL);
        sesion.removeAttribute(CITA);
        if (idHistorial.matches("\\d{1,18}")) {
            sesion.setAttribute(HISTORIAL, Long.valueOf(idHistorial));
        }
        if (idCita.matches("\\d{1,18}")) {
            sesion.setAttribute(CITA, Long.valueOf(idCita));
        }
        return "redirect:/";
    }

    // Pantalla de dos columnas: izquierda agrega y registra medicamentos, derecha muestra la receta.
    @GetMapping("/")
    public String pantalla(HttpSession sesion, Model model) {
        Medico m = (Medico) sesion.getAttribute(SESION);
        if (m == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        Long historial = (Long) sesion.getAttribute(HISTORIAL);
        if (historial == null) {
            return "redirect:" + irAHistorial(m);
        }
        Long cita = (Long) sesion.getAttribute(CITA);
        if (cita == null) {
            return "redirect:" + irACita(m, historial);
        }
        model.addAttribute("medico", m);
        model.addAttribute("historialActual", historial);
        model.addAttribute("citaActual", cita);
        model.addAttribute("maxLineas", MAX_LINEAS);

        List<Medicamento> catalogo = new ArrayList<>();
        List<LineaReceta> lineas = new ArrayList<>();
        Set<String> enReceta = new HashSet<>();
        double total = 0;
        try {
            SalidaReceta r = s.verResumen();
            if (r != null && r.cestaReceta() != null) {
                lineas = r.cestaReceta();
                total = r.totalReceta();
            }
            // El backend no permite repetir un medicamento en la receta: se marcan en la lista.
            for (LineaReceta l : lineas) {
                enReceta.add(l.nom().toLowerCase());
            }
            catalogo = s.listarMedicamentos();
        } catch (RuntimeException ex) {
            model.addAttribute("errorServicio", mensaje(ex));
        }
        model.addAttribute("catalogo", catalogo);
        model.addAttribute("enReceta", enReceta);
        model.addAttribute("lineas", lineas);
        model.addAttribute("total", total);
        model.addAttribute("llena", lineas.size() >= MAX_LINEAS);
        return "receta";
    }

    // Columna izquierda, arriba: agrega un medicamento a la receta.
    @PostMapping("/agregar")
    public String agregar(@RequestParam(defaultValue = "") String idMedicamento,
                          @RequestParam(defaultValue = "") String cant,
                          @RequestParam(defaultValue = "") String indicacion,
                          HttpSession sesion, RedirectAttributes ra) {
        if (sesion.getAttribute(SESION) == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        String c = cant.trim();
        String ind = indicacion.trim();
        String error = null;

        // Las mismas reglas que valida el backend, para avisar antes de llamarlo.
        if (!idMedicamento.matches("\\d{1,18}")) {
            error = "Elija un medicamento";
        } else if (!c.matches("\\d{1,3}") || Integer.parseInt(c) < 1) {
            error = "La cantidad debe ser un número entero de 1 a 999";
        } else if (ind.length() < 3 || ind.length() > 120) {
            error = "La indicación debe tener de 3 a 120 caracteres";
        }

        if (error == null) {
            try {
                s.agregarMedicamento(new EntradaReceta(Long.valueOf(idMedicamento), Integer.parseInt(c), ind));
                return "redirect:/";
            } catch (RuntimeException ex) {
                error = mensaje(ex);
            }
        }

        ra.addFlashAttribute("errorAgregar", error);
        ra.addFlashAttribute("medSel", idMedicamento);
        ra.addFlashAttribute("cant", c);
        ra.addFlashAttribute("indicacion", ind);
        return "redirect:/";
    }

    // Columna izquierda, abajo: registra un medicamento nuevo en el catálogo.
    @PostMapping("/medicamento")
    public String registrarMedicamento(@RequestParam(defaultValue = "") String nom,
                                       @RequestParam(defaultValue = "") String pre,
                                       @RequestParam(defaultValue = "") String stock,
                                       HttpSession sesion, RedirectAttributes ra) {
        if (sesion.getAttribute(SESION) == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        String n = nom.trim().replaceAll("\\s+", " ");
        String p = pre.trim().replace(',', '.');
        String st = stock.trim();
        String error = null;

        if (n.length() < 2 || n.length() > 80) {
            error = "El nombre debe tener de 2 a 80 caracteres";
        } else if (!p.matches("\\d{1,5}(\\.\\d{1,2})?") || Double.parseDouble(p) <= 0) {
            error = "El precio debe ser mayor que 0, en soles y con hasta 2 decimales";
        } else if (!st.matches("\\d{1,5}")) {
            error = "El stock es un número entero de 0 en adelante";
        }

        if (error == null) {
            try {
                Medicamento g = s.grabarMedicamento(
                        new MedicamentoNuevo(n, Double.parseDouble(p), Integer.parseInt(st)));
                if (g != null && g.idMedicamento() != null) {
                    ra.addFlashAttribute("okMedicamento", "Medicamento registrado: " + g.nom());
                    return "redirect:/";
                }
                error = "No se pudo registrar el medicamento. Intente de nuevo.";
            } catch (RuntimeException ex) {
                error = mensaje(ex);
            }
        }

        ra.addFlashAttribute("errorMedicamento", error);
        ra.addFlashAttribute("nom", n);
        ra.addFlashAttribute("pre", pre.trim());
        ra.addFlashAttribute("stock", st);
        return "redirect:/";
    }

    // Columna derecha: quita una línea de la receta (el stock se devuelve en el backend).
    @PostMapping("/quitar")
    public String quitar(@RequestParam(defaultValue = "") String idLin,
                         HttpSession sesion, RedirectAttributes ra) {
        if (sesion.getAttribute(SESION) == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        if (idLin.matches("\\d{1,18}")) {
            try {
                s.quitarMedicamento(Long.valueOf(idLin));
            } catch (RuntimeException ex) {
                ra.addFlashAttribute("errorReceta", mensaje(ex));
            }
        }
        return "redirect:/";
    }

    // Pasa a la vista de análisis con un pase NUEVO (la receta puede quedar vacía: es opcional).
    @GetMapping("/continuar")
    public String continuar(HttpSession sesion) {
        Medico m = (Medico) sesion.getAttribute(SESION);
        if (m == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        Long historial = (Long) sesion.getAttribute(HISTORIAL);
        if (historial == null) {
            return "redirect:" + irAHistorial(m);
        }
        Long cita = (Long) sesion.getAttribute(CITA);
        if (cita == null) {
            return "redirect:" + irACita(m, historial);
        }
        return "redirect:" + urlAnalisis + "/entrar?t=" + TokenMedico.crear(m, secreto, 60)
                + "&idHistorial=" + historial + "&idCita=" + cita;
    }

    // Vuelve a la vista de cita con el mismo historial.
    @GetMapping("/cita")
    public String volverACita(HttpSession sesion) {
        Medico m = (Medico) sesion.getAttribute(SESION);
        if (m == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        Long historial = (Long) sesion.getAttribute(HISTORIAL);
        if (historial == null) {
            return "redirect:" + irAHistorial(m);
        }
        return "redirect:" + irACita(m, historial);
    }

    // Cierra esta sesión y la del login.
    @PostMapping("/salir")
    public String salir(HttpSession sesion) {
        sesion.invalidate();
        return "redirect:" + urlIngreso + "/cerrar";
    }

    private String irAHistorial(Medico m) {
        return urlHistorial + "/entrar?t=" + TokenMedico.crear(m, secreto, 60);
    }

    private String irACita(Medico m, Long historial) {
        return urlCita + "/entrar?t=" + TokenMedico.crear(m, secreto, 60) + "&idHistorial=" + historial;
    }

    private static String mensaje(RuntimeException ex) {
        if (ex instanceof ResourceAccessException) {
            return "El servicio de recetas no responde. Verifique que MicroRecetaCompuesto esté registrado en Eureka.";
        }
        if (ex instanceof HttpStatusCodeException h) {
            try {
                Map<?, ?> cuerpo = h.getResponseBodyAs(Map.class);
                if (cuerpo != null && cuerpo.get("message") != null) {
                    return String.valueOf(cuerpo.get("message"));
                }
            } catch (RuntimeException ignorada) {
                // cuerpo ilegible: se usa el texto genérico
            }
        }
        return "No se pudo completar la operación. Intente de nuevo.";
    }
}