package CentroMedico.MicroVistaCitaMedica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Servicio {
    private static final String CITA = "http://MicroCitaMedica/cita";

    @Autowired
    private RestTemplate rt;

    // Del diagrama: devuelve la cita, o null si no existe (el backend responde 200 vacío).
    public CitaMedica consultar(Long idCita) {
        return rt.getForObject(CITA + "/consultar/{id}", CitaMedica.class, idCita);
    }

    // Graba una cita nueva: el backend la deja pendiente (400 si el motivo o la fecha no cumplen).
    public CitaMedica grabar(CitaNueva c) {
        return rt.postForObject(CITA + "/grabar", c, CitaMedica.class);
    }
}