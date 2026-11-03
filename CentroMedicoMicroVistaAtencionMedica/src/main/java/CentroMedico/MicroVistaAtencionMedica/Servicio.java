package CentroMedico.MicroVistaAtencionMedica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Servicio {
    private static final String ATENCION = "http://MicroAtencionMedicaCompuesto/atencion";

    @Autowired
    private RestTemplate rt;

    // Del diagrama: registrarAtencion(e). Valida todo, guarda, marca la cita y vacía las cestas.
    public SalidaAtencionMedica registrarAtencion(EntradaAtencionMedica e) {
        return rt.postForObject(ATENCION + "/registrar", e, SalidaAtencionMedica.class);
    }

    // Extra de solo lectura: receta y análisis que se van a firmar.
    public SalidaAtencionMedica verPrevia() {
        return rt.getForObject(ATENCION + "/previa", SalidaAtencionMedica.class);
    }

    // Del diagrama: obtenerDetalleAtencion(). Devuelve la ÚLTIMA atención registrada en todo el sistema.
    public SalidaAtencionMedica obtenerDetalleAtencion() {
        return rt.getForObject(ATENCION + "/detalle", SalidaAtencionMedica.class);
    }
}