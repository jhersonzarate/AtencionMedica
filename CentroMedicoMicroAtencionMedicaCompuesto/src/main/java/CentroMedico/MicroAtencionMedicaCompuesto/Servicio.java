package CentroMedico.MicroAtencionMedicaCompuesto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Servicio {
    private static final String MEDICO = "http://MicroValidarMedico/medico";
    private static final String HISTORIAL = "http://MicroHistorialClinico/historial";
    private static final String CITA = "http://MicroCitaMedica/cita";
    private static final String ATENCION = "http://MicroAtencionMedica/atencionmedica";
    private static final String RECETA = "http://MicroRecetaCompuesto/receta";
    private static final String ANALISIS = "http://MicroAnalisisCompuesto/analisis";

    @Autowired
    private RestTemplate rt;

    // Sin BD: solo recuerda en memoria la última atención registrada.
    private volatile SalidaAtencionMedica ultima;

    public SalidaAtencionMedica registrarAtencion(EntradaAtencionMedica e) {
        if (e == null || e.medico() == null || e.historialClinico() == null
                || e.citaMedica() == null || e.atencionMedica() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Faltan datos: historialClinico, medico, citaMedica y atencionMedica son obligatorios");
        }
        if (e.historialClinico().idHistorial() == null || e.citaMedica().idCita() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indique el idHistorial y el idCita");
        }

        // 1. MicroValidarMedico: el médico debe ser válido
        Medico m = rt.postForObject(MEDICO + "/validar",
                new EntradaMedico(e.medico().idMedico(), e.medico().pass()), Medico.class);
        if (m == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Médico no válido");
        }

        // 2. MicroHistorialClinico: el historial debe existir
        HistorialClinico h = rt.getForObject(HISTORIAL + "/consultar/" + e.historialClinico().idHistorial(),
                HistorialClinico.class);
        if (h == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe el historial " + e.historialClinico().idHistorial());
        }

        // 3. MicroCitaMedica: la cita debe existir y estar pendiente
        CitaMedica c = rt.getForObject(CITA + "/consultar/" + e.citaMedica().idCita(), CitaMedica.class);
        if (c == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la cita " + e.citaMedica().idCita());
        }
        if (Boolean.TRUE.equals(c.estado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cita " + c.idCita() + " ya fue atendida");
        }

        // 4. MicroRecetaCompuesto y MicroAnalisisCompuesto: lo que hay en las cestas
        SalidaReceta receta = rt.getForObject(RECETA + "/resumen", SalidaReceta.class);
        SalidaAnalisis analisis = rt.getForObject(ANALISIS + "/resumen", SalidaAnalisis.class);

        // 5. MicroAtencionMedica: guarda la atención (valida fecha, diagnóstico y tratamiento)
        AtencionMedica guardada = rt.postForObject(ATENCION + "/registrar", e.atencionMedica(), AtencionMedica.class);

        // 6. MicroCitaMedica: la cita pasa a atendida (grabar con idCita actualiza)
        rt.postForObject(CITA + "/grabar", new CitaMedica(c.idCita(), c.fec(), c.motivo(), true), CitaMedica.class);

        // 7. Vacía las cestas para la siguiente atención
        rt.postForObject(RECETA + "/nueva", null, Void.class);
        rt.postForObject(ANALISIS + "/nuevo", null, Void.class);

        SalidaAtencionMedica salida = new SalidaAtencionMedica(guardada, receta, analisis);
        ultima = salida;
        return salida;
    }

    // Detalle de la última atención registrada.
    public SalidaAtencionMedica obtenerDetalleAtencion() {
        SalidaAtencionMedica u = ultima;
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aún no se registró ninguna atención");
        }
        return u;
    }
}