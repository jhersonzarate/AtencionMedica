package CentroMedico.MicroVistaCarritoAnalisis;

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
public class ControladorCarritoAnalisis {
    private static final String SESION = "medico";
    private static final String HISTORIAL = "idHistorial";
    private static final String CITA = "idCita";
    private static final int MAX_LINEAS = 10; // el mismo tope que valida MicroAnalisisCompuesto

    @Autowired
    private Servicio s;

    @Value("${vista.secreto}")
    private String secreto;

    @Value("${vista.validarmedico.url}")
    private String urlIngreso;

    @Value("${vista.historial.url}")
    private String urlHistorial;

    @Value("${vista.receta.url}")
    private String urlReceta;

    @Value("${vista.atencion.url}")
    private String urlAtencion;

    // Llegada desde la vista de receta (o desde la de atención): pase firmado + historial + cita.
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

    // Pantalla de dos columnas: izquierda agrega y registra tipos de análisis, derecha muestra el pedido.
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
            // Sin cita no hay atención: se vuelve a la receta con el historial elegido, que la reencamina.
            return "redirect:" + irAReceta(m, historial, null);
        }
        model.addAttribute("medico", m);
        model.addAttribute("historialActual", historial);
        model.addAttribute("citaActual", cita);
        model.addAttribute("maxLineas", MAX_LINEAS);

        List<TipoAnalisis> catalogo = new ArrayList<>();
        List<LineaAnalisis> lineas = new ArrayList<>();
        Set<String> enPedido = new HashSet<>();
        double total = 0;
        try {
            SalidaAnalisis r = s.verResumen();
            if (r != null && r.cestaAnalisis() != null) {
                lineas = r.cestaAnalisis();
                total = r.totalAnalisis();
            }
            // El backend no permite repetir un análisis en el pedido: se marcan en la lista.
            for (LineaAnalisis l : lineas) {
                enPedido.add(l.nom().toLowerCase());
            }
            catalogo = s.listarTiposAnalisis();
        } catch (RuntimeException ex) {
            model.addAttribute("errorServicio", mensaje(ex));
        }
        model.addAttribute("catalogo", catalogo);
        model.addAttribute("enPedido", enPedido);
        model.addAttribute("lineas", lineas);
        model.addAttribute("total", total);
        model.addAttribute("llena", lineas.size() >= MAX_LINEAS);
        return "analisis";
    }

    // Columna izquierda, arriba: agrega un tipo de análisis al pedido (sin cantidad).
    @PostMapping("/agregar")
    public String agregar(@RequestParam(defaultValue = "") String idTipoAnalisis,
                          @RequestParam(defaultValue = "") String indicacion,
                          HttpSession sesion, RedirectAttributes ra) {
        if (sesion.getAttribute(SESION) == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        String ind = indicacion.trim();
        String error = null;

        // Las mismas reglas que valida el backend, para avisar antes de llamarlo.
        if (!idTipoAnalisis.matches("\\d{1,18}")) {
            error = "Elija un tipo de análisis";
        } else if (ind.length() < 3 || ind.length() > 120) {
            error = "La indicación debe tener de 3 a 120 caracteres";
        }

        if (error == null) {
            try {
                s.agregarTipoAnalisis(new EntradaAnalisis(Long.valueOf(idTipoAnalisis), ind));
                return "redirect:/";
            } catch (RuntimeException ex) {
                error = mensaje(ex);
            }
        }

        ra.addFlashAttribute("errorAgregar", error);
        ra.addFlashAttribute("tipoSel", idTipoAnalisis);
        ra.addFlashAttribute("indicacion", ind);
        return "redirect:/";
    }

    // Columna izquierda, abajo: registra un tipo de análisis nuevo en el catálogo.
    @PostMapping("/tipo")
    public String registrarTipo(@RequestParam(defaultValue = "") String nom,
                                @RequestParam(defaultValue = "") String pre,
                                HttpSession sesion, RedirectAttributes ra) {
        if (sesion.getAttribute(SESION) == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        String n = nom.trim().replaceAll("\\s+", " ");
        String p = pre.trim().replace(',', '.');
        String error = null;

        if (n.length() < 2 || n.length() > 80) {
            error = "El nombre debe tener de 2 a 80 caracteres";
        } else if (!p.matches("\\d{1,5}(\\.\\d{1,2})?") || Double.parseDouble(p) <= 0) {
            error = "El precio debe ser mayor que 0, en soles y con hasta 2 decimales";
        }

        if (error == null) {
            try {
                TipoAnalisis g = s.grabarTipoAnalisis(new TipoAnalisisNuevo(n, Double.parseDouble(p)));
                if (g != null && g.idTipoAnalisis() != null) {
                    ra.addFlashAttribute("okTipo", "Tipo de análisis registrado: " + g.nom());
                    return "redirect:/";
                }
                error = "No se pudo registrar el tipo de análisis. Intente de nuevo.";
            } catch (RuntimeException ex) {
                error = mensaje(ex);
            }
        }

        ra.addFlashAttribute("errorTipo", error);
        ra.addFlashAttribute("nom", n);
        ra.addFlashAttribute("pre", pre.trim());
        return "redirect:/";
    }

    // Columna derecha: quita una línea del pedido.
    @PostMapping("/quitar")
    public String quitar(@RequestParam(defaultValue = "") String idLin,
                         HttpSession sesion, RedirectAttributes ra) {
        if (sesion.getAttribute(SESION) == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        if (idLin.matches("\\d{1,18}")) {
            try {
                s.quitarTipoAnalisis(Long.valueOf(idLin));
            } catch (RuntimeException ex) {
                ra.addFlashAttribute("errorPedido", mensaje(ex));
            }
        }
        return "redirect:/";
    }

    // Pasa a la vista de atención médica con un pase NUEVO (el pedido puede quedar vacío: es opcional).
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
            return "redirect:" + irAReceta(m, historial, null);
        }
        return "redirect:" + urlAtencion + "/entrar?t=" + TokenMedico.crear(m, secreto, 60)
                + "&idHistorial=" + historial + "&idCita=" + cita;
    }

    // Vuelve a la vista de receta con el mismo historial y la misma cita.
    @GetMapping("/receta")
    public String volverAReceta(HttpSession sesion) {
        Medico m = (Medico) sesion.getAttribute(SESION);
        if (m == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        Long historial = (Long) sesion.getAttribute(HISTORIAL);
        if (historial == null) {
            return "redirect:" + irAHistorial(m);
        }
        return "redirect:" + irAReceta(m, historial, (Long) sesion.getAttribute(CITA));
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

    private String irAReceta(Medico m, Long historial, Long cita) {
        return urlReceta + "/entrar?t=" + TokenMedico.crear(m, secreto, 60)
                + "&idHistorial=" + historial + (cita == null ? "" : "&idCita=" + cita);
    }

    private static String mensaje(RuntimeException ex) {
        if (ex instanceof ResourceAccessException) {
            return "El servicio de análisis no responde. Verifique que MicroAnalisisCompuesto esté registrado en Eureka.";
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