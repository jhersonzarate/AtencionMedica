package CentroMedico.MicroVistaCitaMedica;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Map;
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
public class ControladorCitaMedica {
    private static final String SESION = "medico";
    private static final String HISTORIAL = "idHistorial";
    private static final ZoneId LIMA = ZoneId.of("America/Lima");

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

    // Llegada desde la vista de historial clínico: pase firmado + código del historial elegido.
    // Crea la sesión de ESTA app, guarda el historial y limpia la URL.
    @GetMapping("/entrar")
    public String entrar(@RequestParam(defaultValue = "") String t,
                         @RequestParam(defaultValue = "") String idHistorial,
                         HttpServletRequest request) {
        Medico m = TokenMedico.leer(t, secreto);
        if (m == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        HttpSession sesion = request.getSession();
        request.changeSessionId();
        sesion.setAttribute(SESION, m);
        sesion.removeAttribute(HISTORIAL); // cada llegada parte de cero
        if (idHistorial.matches("\\d{1,18}")) {
            sesion.setAttribute(HISTORIAL, Long.valueOf(idHistorial));
        }
        return "redirect:/";
    }

    // Pantalla de dos columnas: izquierda registra citas, derecha consulta una cita por su código.
    @GetMapping("/")
    public String pantalla(@RequestParam(required = false) String idCita,
                           HttpSession sesion, Model model) {
        Medico m = (Medico) sesion.getAttribute(SESION);
        if (m == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        Long historial = (Long) sesion.getAttribute(HISTORIAL);
        if (historial == null) {
            // Sin historial elegido no hay cita que atender: se vuelve a elegirlo.
            return "redirect:" + irAHistorial(m);
        }
        model.addAttribute("medico", m);
        model.addAttribute("historialActual", historial);
        model.addAttribute("hoy", LocalDate.now(LIMA).toString());
        if (idCita == null) {
            return "cita";
        }

        String codigo = idCita.trim();
        model.addAttribute("idCita", codigo);
        if (codigo.isEmpty()) {
            model.addAttribute("errorConsulta", "Escriba el código de la cita médica");
            return "cita";
        }
        if (!codigo.matches("\\d{1,18}")) {
            model.addAttribute("errorConsulta", "El código de la cita es un número");
            return "cita";
        }

        try {
            // Del diagrama: consultarCita(idCita)
            CitaMedica c = s.consultar(Long.parseLong(codigo));
            if (c == null) {
                model.addAttribute("errorConsulta", "No existe una cita médica con el código " + codigo);
            } else {
                model.addAttribute("cita", c);
                model.addAttribute("atendida", Boolean.TRUE.equals(c.estado()));
            }
        } catch (RuntimeException ex) {
            model.addAttribute("errorConsulta", mensaje(ex));
        }
        return "cita";
    }

    // Columna izquierda: graba la cita (queda pendiente) y la pantalla vuelve con el formulario limpio.
    @PostMapping("/cita")
    public String registrarCita(@RequestParam(defaultValue = "") String fec,
                                @RequestParam(defaultValue = "") String motivo,
                                HttpSession sesion, RedirectAttributes ra) {
        if (sesion.getAttribute(SESION) == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        String f = fec.trim();
        String mo = motivo.trim();
        String error = null;

        // Las mismas reglas que valida el backend, para avisar antes de llamarlo.
        LocalDate fecha = parsearFecha(f);
        if (fecha == null) {
            error = "Escriba una fecha válida";
        } else if (fecha.isBefore(LocalDate.now(LIMA))) {
            error = "La fecha de la cita no puede ser pasada";
        } else if (mo.length() < 3 || mo.length() > 120) {
            error = "El motivo debe tener de 3 a 120 caracteres";
        }

        if (error == null) {
            try {
                CitaMedica c = s.grabar(new CitaNueva(fecha.toString(), mo));
                if (c != null && c.idCita() != null) {
                    return "redirect:/";
                }
                error = "No se pudo registrar la cita. Intente de nuevo.";
            } catch (RuntimeException ex) {
                error = mensaje(ex);
            }
        }

        ra.addFlashAttribute("errorRegistro", error);
        ra.addFlashAttribute("fec", f);
        ra.addFlashAttribute("motivo", mo);
        return "redirect:/";
    }

    // Pasa a la vista de la receta (MicroVistaCarritoReceta) con un pase NUEVO. Solo una cita
    // pendiente puede seguir: se vuelve a consultar aquí porque su estado pudo cambiar.
    @GetMapping("/continuar")
    public String continuar(@RequestParam(defaultValue = "") String idCita, HttpSession sesion) {
        Medico m = (Medico) sesion.getAttribute(SESION);
        if (m == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        Long historial = (Long) sesion.getAttribute(HISTORIAL);
        if (historial == null) {
            return "redirect:" + irAHistorial(m);
        }
        if (!idCita.matches("\\d{1,18}")) {
            return "redirect:/";
        }
        try {
            CitaMedica c = s.consultar(Long.parseLong(idCita));
            if (c == null || Boolean.TRUE.equals(c.estado())) {
                return "redirect:/?idCita=" + idCita; // la pantalla muestra el motivo del bloqueo
            }
        } catch (RuntimeException ex) {
            return "redirect:/?idCita=" + idCita; // la pantalla muestra el error del servicio
        }
        return "redirect:" + urlReceta + "/entrar?t=" + TokenMedico.crear(m, secreto, 60)
                + "&idHistorial=" + historial + "&idCita=" + idCita;
    }

    // Vuelve a la vista de historial clínico para elegir otro historial.
    @GetMapping("/historial")
    public String historial(HttpSession sesion) {
        Medico m = (Medico) sesion.getAttribute(SESION);
        if (m == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        return "redirect:" + irAHistorial(m);
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

    private static LocalDate parsearFecha(String f) {
        try {
            return LocalDate.parse(f);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static String mensaje(RuntimeException ex) {
        if (ex instanceof ResourceAccessException) {
            return "El servicio de citas médicas no responde. Verifique que MicroCitaMedica esté registrado en Eureka.";
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