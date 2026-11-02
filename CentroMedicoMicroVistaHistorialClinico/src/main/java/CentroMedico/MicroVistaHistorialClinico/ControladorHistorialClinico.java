package CentroMedico.MicroVistaHistorialClinico;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorHistorialClinico {
    private static final String SESION = "medico";

    @Autowired
    private Servicio s;

    @Value("${vista.secreto}")
    private String secreto;

    @Value("${vista.validarmedico.url}")
    private String urlIngreso;

    @Value("${vista.cita.url}")
    private String urlCita;

    // Llegada desde otra vista con el pase firmado: crea la sesión de ESTA app y limpia la URL.
    @GetMapping("/entrar")
    public String entrar(@RequestParam(defaultValue = "") String t, HttpServletRequest request) {
        Medico m = TokenMedico.leer(t, secreto);
        if (m == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        HttpSession sesion = request.getSession();
        request.changeSessionId();
        sesion.setAttribute(SESION, m);
        return "redirect:/";
    }

    // Pantalla de dos columnas: izquierda registra pacientes, derecha consulta un historial por su código.
    @GetMapping("/")
    public String pantalla(@RequestParam(required = false) String idHistorial,
                           HttpSession sesion, Model model) {
        Medico m = (Medico) sesion.getAttribute(SESION);
        if (m == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        model.addAttribute("medico", m);
        if (idHistorial == null) {
            return "historial";
        }

        String codigo = idHistorial.trim();
        model.addAttribute("idHistorial", codigo);
        if (codigo.isEmpty()) {
            model.addAttribute("errorConsulta", "Escriba el código del historial clínico");
            return "historial";
        }
        if (!codigo.matches("\\d{1,18}")) {
            model.addAttribute("errorConsulta", "El código del historial es un número");
            return "historial";
        }

        try {
            // Del diagrama: consultarHistorial(idHistorial)
            HistorialClinico h = s.consultar(Long.parseLong(codigo));
            if (h == null) {
                model.addAttribute("errorConsulta", "No existe un historial clínico con el código " + codigo);
            } else {
                model.addAttribute("historial", h);
                try {
                    model.addAttribute("paciente", s.buscarPaciente(h.dniPaciente()));
                } catch (RuntimeException ignorada) {
                    // se muestra el historial aunque no se puedan traer los datos del paciente
                }
            }
        } catch (RuntimeException ex) {
            model.addAttribute("errorConsulta", mensaje(ex));
        }
        return "historial";
    }

    // Columna izquierda: graba al paciente y enseguida crea su historial (el backend exige ese orden).
    @PostMapping("/paciente")
    public String registrarPaciente(@RequestParam(defaultValue = "") String dni,
                                    @RequestParam(defaultValue = "") String nom,
                                    @RequestParam(defaultValue = "") String ape,
                                    @RequestParam(defaultValue = "") String tel,
                                    @RequestParam(defaultValue = "") String dir,
                                    HttpSession sesion, RedirectAttributes ra) {
        if (sesion.getAttribute(SESION) == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        String d = dni.trim();
        String n = nom.trim();
        String a = ape.trim();
        String t = tel.trim();
        String di = dir.trim();
        String error = null;
        String nota = null;

        // Las mismas reglas que valida el backend, para avisar antes de llamarlo.
        if (!d.matches("\\d{8}")) {
            error = "El DNI debe tener 8 dígitos";
        } else if (n.isEmpty() || a.isEmpty()) {
            error = "Escriba los nombres y los apellidos";
        } else if (!t.matches("\\d{7,9}")) {
            error = "El teléfono debe tener de 7 a 9 dígitos";
        } else if (di.length() < 5 || di.length() > 120) {
            error = "La dirección debe tener de 5 a 120 caracteres";
        }

        // Paso 1: el paciente.
        if (error == null) {
            try {
                s.grabarPaciente(new Paciente(d, n, a, t, di));
            } catch (HttpClientErrorException.Conflict ex) {
                // El DNI ya estaba grabado: solo se sigue si todavía no tiene historial.
                try {
                    HistorialClinico previo = s.buscarPorDni(d);
                    if (previo != null) {
                        error = "Ese paciente ya está registrado; su historial es el código " + previo.idHistorial();
                    } else {
                        nota = "El DNI ya estaba grabado; se creó su historial.";
                    }
                } catch (RuntimeException ex2) {
                    error = mensaje(ex2);
                }
            } catch (RuntimeException ex) {
                error = mensaje(ex);
            }
        }

        // Paso 2: su historial.
        if (error == null) {
            try {
                HistorialClinico h = s.crearHistorial(d);
                if (h != null && h.idHistorial() != null) {
                    ra.addFlashAttribute("idNuevo", h.idHistorial());
                    ra.addFlashAttribute("dniNuevo", d);
                    ra.addFlashAttribute("nombreNuevo", n + " " + a);
                    if (nota != null) {
                        ra.addFlashAttribute("nota", nota);
                    }
                    return "redirect:/";
                }
                error = "El paciente se grabó, pero no se pudo crear su historial. Pulse Registrar de nuevo.";
            } catch (RuntimeException ex) {
                error = "El paciente se grabó, pero no se pudo crear su historial: " + mensaje(ex)
                        + ". Pulse Registrar de nuevo.";
            }
        }

        ra.addFlashAttribute("errorRegistro", error);
        ra.addFlashAttribute("dni", d);
        ra.addFlashAttribute("nom", n);
        ra.addFlashAttribute("ape", a);
        ra.addFlashAttribute("tel", t);
        ra.addFlashAttribute("dir", di);
        return "redirect:/";
    }

    // Pasa a la vista de cita médica con un pase NUEVO (el de la llegada ya pudo vencer).
    @GetMapping("/continuar")
    public String continuar(@RequestParam(defaultValue = "") String idHistorial, HttpSession sesion) {
        Medico m = (Medico) sesion.getAttribute(SESION);
        if (m == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        if (!idHistorial.matches("\\d{1,18}")) {
            return "redirect:/";
        }
        return "redirect:" + urlCita + "/entrar?t=" + TokenMedico.crear(m, secreto, 60)
                + "&idHistorial=" + idHistorial;
    }

    // Cierra esta sesión y la del login.
    @PostMapping("/salir")
    public String salir(HttpSession sesion) {
        sesion.invalidate();
        return "redirect:" + urlIngreso + "/cerrar";
    }

    private static String mensaje(RuntimeException ex) {
        if (ex instanceof ResourceAccessException) {
            return "El servicio de historial clínico no responde. Verifique que MicroHistorialClinico esté registrado en Eureka.";
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