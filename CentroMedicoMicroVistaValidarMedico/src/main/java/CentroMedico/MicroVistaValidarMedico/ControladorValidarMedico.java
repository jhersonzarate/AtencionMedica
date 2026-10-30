package CentroMedico.MicroVistaValidarMedico;

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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorValidarMedico {
    private static final String SESION = "medico";
    private static final String CREDENCIALES_MALAS = "Código de médico o contraseña incorrectos";

    @Autowired
    private Servicio s;

    @Value("${vista.secreto}")
    private String secreto;

    @Value("${vista.historial.url}")
    private String urlHistorial;

    @GetMapping("/")
    public String raiz() {
        return "redirect:/ingreso";
    }

    // ---------- Ingreso ----------
    @GetMapping("/ingreso")
    public String ingreso(HttpSession sesion) {
        Medico m = (Medico) sesion.getAttribute(SESION);
        if (m != null) {
            return "redirect:" + destino(m); // ya inició sesión: sigue a la siguiente vista
        }
        return "ingreso";
    }

    @PostMapping("/ingreso")
    public String ingresar(@RequestParam(defaultValue = "") String idMedico,
                           @RequestParam(defaultValue = "") String pass,
                           HttpServletRequest request, Model model) {
        String codigo = idMedico.trim();
        model.addAttribute("idMedico", codigo); // la pass nunca vuelve a la página

        if (codigo.isEmpty() || pass.isBlank()) {
            model.addAttribute("error", "Escriba su código de médico y su contraseña");
            return "ingreso";
        }
        if (!codigo.matches("\\d{1,18}")) {
            model.addAttribute("error", "El código de médico es un número");
            return "ingreso";
        }
        try {
            Medico m = s.validar(Long.parseLong(codigo), pass);
            if (m == null) {
                model.addAttribute("error", CREDENCIALES_MALAS);
                return "ingreso";
            }
            HttpSession sesion = request.getSession();
            request.changeSessionId(); // evita fijación de sesión
            sesion.setAttribute(SESION, m);
            return "redirect:" + destino(m);
        } catch (RuntimeException ex) {
            model.addAttribute("error", mensaje(ex));
            return "ingreso";
        }
    }

    // ---------- Registro ----------
    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam(defaultValue = "") String nom,
                            @RequestParam(defaultValue = "") String especialidad,
                            @RequestParam(defaultValue = "") String pass,
                            Model model, RedirectAttributes ra) {
        String n = nom.trim();
        String e = especialidad.trim();
        String error = null;

        if (n.length() < 3 || n.length() > 100) {
            error = "Escriba su nombre completo (de 3 a 100 caracteres)";
        } else if (e.length() < 3 || e.length() > 60) {
            error = "Escriba su especialidad (de 3 a 60 caracteres)";
        } else if (pass.isBlank()) {
            error = "Escriba una contraseña";
        }

        if (error == null) {
            try {
                Medico m = s.registrar(n, pass, e);
                if (m != null && m.idMedico() != null) {
                    ra.addFlashAttribute("codigo", m.idMedico());
                    ra.addFlashAttribute("nom", m.nom());
                    return "redirect:/registro/listo";
                }
                error = "No se pudo registrar al médico. Intente de nuevo.";
            } catch (RuntimeException ex) {
                error = mensaje(ex);
            }
        }
        model.addAttribute("error", error);
        model.addAttribute("nom", n);
        model.addAttribute("especialidad", e);
        return "registro";
    }

    // Confirmación: solo existe justo después de registrar (si se recarga, vuelve al ingreso).
    @GetMapping("/registro/listo")
    public String listo(Model model) {
        if (!model.containsAttribute("codigo")) {
            return "redirect:/ingreso";
        }
        return "registro-listo";
    }

    // ---------- Cierre de sesión (las demás vistas redirigen aquí al pulsar "Salir") ----------
    @GetMapping("/cerrar")
    public String cerrar(HttpSession sesion) {
        sesion.invalidate();
        return "redirect:/ingreso";
    }

    // URL de la siguiente vista con un pase firmado de 60 segundos.
    private String destino(Medico m) {
        return urlHistorial + "/entrar?t=" + TokenMedico.crear(m, secreto, 60);
    }

    // Traduce los errores del micro a un texto claro para la pantalla.
    private static String mensaje(RuntimeException ex) {
        if (ex instanceof ResourceAccessException) {
            return "El servicio de médicos no responde. Verifique que MicroValidarMedico esté registrado en Eureka.";
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