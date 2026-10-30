package CentroMedico.MicroVistaValidarMedico;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Servicio {
    private static final String MEDICO = "http://MicroValidarMedico/medico";

    @Autowired
    private RestTemplate rt;

    // Devuelve el médico si el código y la pass coinciden; null si no (el backend responde 200 vacío).
    public Medico validar(Long idMedico, String pass) {
        return rt.postForObject(MEDICO + "/validar", new EntradaMedico(idMedico, pass), Medico.class);
    }

    // Graba el médico; el backend genera el código y devuelve el médico sin la pass.
    public Medico registrar(String nom, String pass, String especialidad) {
        return rt.postForObject(MEDICO + "/grabar", new RegistroMedico(nom, pass, especialidad), Medico.class);
    }
}