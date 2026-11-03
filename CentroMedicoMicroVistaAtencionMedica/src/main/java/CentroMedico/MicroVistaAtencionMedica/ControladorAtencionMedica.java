package CentroMedico.MicroVistaAtencionMedica;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorAtencionMedica {
    private static final String SESION = "medico";
    private static final String HISTORIAL = "idHistorial";
    private static final String CITA = "idCita";
    private static final String ATENDIDA = "idAtencion"; // la atención que ESTE médico acaba de registrar
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

    @Value("${vista.analisis.url}")
    private String urlAnalisis;

    // Llegada desde la vista de análisis: pase firmado + historial + cita.
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
        sesion.removeAttribute(ATENDIDA);
        if (idHistorial.matches("\\d{1,18}")) {
            sesion.setAttribute(HISTORIAL, Long.valueOf(idHistorial));
        }
        if (idCita.matches("\\d{1,18}")) {
            sesion.setAttribute(CITA, Long.valueOf(idCita));
        }
        return "redirect:/";
    }

    // Pantalla de dos columnas: izquierda registra la atención, derecha muestra lo que se va a firmar.
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
            // Sin cita no hay atención: la receta reencamina al historial o a la cita.
            return "redirect:" + irAReceta(m, historial, null);
        }
        model.addAttribute("medico", m);
        model.addAttribute("historialActual", historial);
        model.addAttribute("citaActual", cita);
        model.addAttribute("hoy", LocalDate.now(LIMA).toString());

        List<LineaReceta> lineasReceta = List.of();
        List<LineaAnalisis> lineasAnalisis = List.of();
        double totalReceta = 0;
        double totalAnalisis = 0;
        try {
            SalidaAtencionMedica p = s.verPrevia();
            if (p != null && p.recetaCompuesto() != null && p.recetaCompuesto().cestaReceta() != null) {
                lineasReceta = p.recetaCompuesto().cestaReceta();
                totalReceta = p.recetaCompuesto().totalReceta();
            }
            if (p != null && p.analisisCompuesto() != null && p.analisisCompuesto().cestaAnalisis() != null) {
                lineasAnalisis = p.analisisCompuesto().cestaAnalisis();
                totalAnalisis = p.analisisCompuesto().totalAnalisis();
            }
        } catch (RuntimeException ex) {
            model.addAttribute("errorServicio", mensaje(ex));
        }
        model.addAttribute("lineasReceta", lineasReceta);
        model.addAttribute("totalReceta", totalReceta);
        model.addAttribute("lineasAnalisis", lineasAnalisis);
        model.addAttribute("totalAnalisis", totalAnalisis);
        model.addAttribute("totalGeneral", totalReceta + totalAnalisis);
        return "atencion";
    }

    // Columna izquierda: registra la atención. La contraseña del médico es su firma.
    @PostMapping("/atencion")
    public String registrar(@RequestParam(defaultValue = "") String fec,
                            @RequestParam(defaultValue = "") String diagnostico,
                            @RequestParam(defaultValue = "") String tratamiento,
                            @RequestParam(defaultValue = "") String pass,
                            HttpSession sesion, RedirectAttributes ra) {
        Medico m = (Medico) sesion.getAttribute(SESION);
        if (m == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        Long historial = (Long) sesion.getAttribute(HISTORIAL);
        Long cita = (Long) sesion.getAttribute(CITA);
        if (historial == null || cita == null) {
            return "redirect:/";
        }
        String f = fec.trim();
        String dx = diagnostico.trim();
        String tx = tratamiento.trim();
        String error = null;

        // Las mismas reglas que valida el backend, para avisar antes de llamarlo.
        LocalDate fecha = parsearFecha(f);
        if (fecha == null) {
            error = "Escriba una fecha de atención válida";
        } else if (fecha.isAfter(LocalDate.now(LIMA))) {
            error = "La fecha de atención no puede ser futura";
        } else if (dx.length() < 5 || dx.length() > 500) {
            error = "El diagnóstico debe tener de 5 a 500 caracteres";
        } else if (tx.length() < 5 || tx.length() > 500) {
            error = "El tratamiento debe tener de 5 a 500 caracteres";
        } else if (pass.isEmpty()) {
            error = "Escriba su contraseña para firmar la atención";
        }

        if (error == null) {
            try {
                SalidaAtencionMedica r = s.registrarAtencion(new EntradaAtencionMedica(
                        new HistorialClinico(historial),
                        new MedicoFirma(m.idMedico(), pass), // el código sale de la sesión, no del formulario
                        new CitaMedica(cita),
                        new AtencionMedica(null, fecha.toString(), dx, tx)));
                if (r != null && r.atencionMedica() != null && r.atencionMedica().idAtencion() != null) {
                    sesion.setAttribute(ATENDIDA, r.atencionMedica().idAtencion());
                    return "redirect:/detalle";
                }
                error = "No se pudo registrar la atención. Intente de nuevo.";
            } catch (RuntimeException ex) {
                error = mensajeRegistro(ex);
            }
        }

        // La contraseña nunca se devuelve al formulario.
        ra.addFlashAttribute("errorRegistro", error);
        ra.addFlashAttribute("fec", f);
        ra.addFlashAttribute("diagnostico", dx);
        ra.addFlashAttribute("tratamiento", tx);
        return "redirect:/";
    }

    // Detalle de la atención recién registrada (obtenerDetalleAtencion del diagrama).
    @GetMapping("/detalle")
    public String detalle(HttpSession sesion, Model model) {
        Medico m = (Medico) sesion.getAttribute(SESION);
        if (m == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        Long mia = (Long) sesion.getAttribute(ATENDIDA);
        if (mia == null) {
            return "redirect:/"; // no registró ninguna atención en esta sesión
        }
        model.addAttribute("medico", m);
        try {
            SalidaAtencionMedica d = s.obtenerDetalleAtencion();
            // El backend devuelve la última de todo el sistema: solo se muestra si es la de este médico.
            if (d == null || d.atencionMedica() == null || !mia.equals(d.atencionMedica().idAtencion())) {
                model.addAttribute("errorDetalle",
                        "La atención se registró, pero otra posterior reemplazó este detalle. "
                        + "Puede verla en MicroAtencionMedica: /atencionmedica/listar.");
            } else {
                model.addAttribute("atencion", d.atencionMedica());
                SalidaReceta r = d.recetaCompuesto();
                SalidaAnalisis a = d.analisisCompuesto();
                List<LineaReceta> lr = r != null && r.cestaReceta() != null ? r.cestaReceta() : List.of();
                List<LineaAnalisis> la = a != null && a.cestaAnalisis() != null ? a.cestaAnalisis() : List.of();
                double tr = r != null ? r.totalReceta() : 0;
                double ta = a != null ? a.totalAnalisis() : 0;
                model.addAttribute("lineasReceta", lr);
                model.addAttribute("totalReceta", tr);
                model.addAttribute("lineasAnalisis", la);
                model.addAttribute("totalAnalisis", ta);
                model.addAttribute("totalGeneral", tr + ta);
            }
        } catch (RuntimeException ex) {
            model.addAttribute("errorDetalle", mensaje(ex));
        }
        return "detalle";
    }

    // Editar la receta: vuelve a su vista con el mismo historial y cita.
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

    // Editar el pedido de análisis: vuelve a su vista con el mismo historial y cita.
    @GetMapping("/analisis")
    public String volverAAnalisis(HttpSession sesion) {
        Medico m = (Medico) sesion.getAttribute(SESION);
        if (m == null) {
            return "redirect:" + urlIngreso + "/ingreso";
        }
        Long historial = (Long) sesion.getAttribute(HISTORIAL);
        Long cita = (Long) sesion.getAttribute(CITA);
        if (historial == null) {
            return "redirect:" + irAHistorial(m);
        }
        if (cita == null) {
            return "redirect:" + irAReceta(m, historial, null);
        }
        return "redirect:" + urlAnalisis + "/entrar?t=" + TokenMedico.crear(m, secreto, 60)
                + "&idHistorial=" + historial + "&idCita=" + cita;
    }

    // Vuelve a la vista de historial clínico para atender a otro paciente.
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

    private String irAReceta(Medico m, Long historial, Long cita) {
        return urlReceta + "/entrar?t=" + TokenMedico.crear(m, secreto, 60)
                + "&idHistorial=" + historial + (cita == null ? "" : "&idCita=" + cita);
    }

    private static LocalDate parsearFecha(String f) {
        try {
            return LocalDate.parse(f);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    // Errores al registrar: la contraseña incorrecta llega como 401 desde MicroValidarMedico.
    private static String mensajeRegistro(RuntimeException ex) {
        if (ex instanceof HttpStatusCodeException h && h.getStatusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED)) {
            return "Contraseña incorrecta. La atención no se registró.";
        }
        return mensaje(ex);
    }

    private static String mensaje(RuntimeException ex) {
        if (ex instanceof ResourceAccessException) {
            return "El servicio de atención no responde. Verifique que MicroAtencionMedicaCompuesto esté registrado en Eureka.";
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