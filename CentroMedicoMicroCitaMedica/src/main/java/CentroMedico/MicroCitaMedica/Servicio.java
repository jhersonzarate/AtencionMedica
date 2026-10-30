package CentroMedico.MicroCitaMedica;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Servicio {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    @Autowired
    private Repositorio r;

    // Del diagrama
    public CitaMedica consultarCita(Long idCita) {
        return r.findById(idCita).orElse(null);
    }

    // Apoyo de pruebas. Sin idCita crea una cita pendiente; con idCita existente la actualiza
    // (así el compuesto puede marcarla como atendida).
    public CitaMedica grabar(CitaMedica c) {
        if (c == null) {
            throw malo("Faltan los datos de la cita");
        }
        String motivo = c.getMotivo() == null ? "" : c.getMotivo().trim();
        if (motivo.length() < 3 || motivo.length() > 120) {
            throw malo("El motivo debe tener de 3 a 120 caracteres");
        }
        LocalDate fec;
        try {
            fec = LocalDate.parse(c.getFec());
        } catch (RuntimeException ex) {
            throw malo("La fecha de la cita no es válida (aaaa-mm-dd)");
        }
        if (c.getIdCita() == null) {
            if (fec.isBefore(LocalDate.now(LIMA))) {
                throw malo("La fecha de la cita no puede ser pasada");
            }
            c.setEstado(false);
        } else if (!r.existsById(c.getIdCita())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la cita " + c.getIdCita());
        } else if (c.getEstado() == null) {
            c.setEstado(false);
        }
        c.setFec(fec.toString());
        c.setMotivo(motivo);
        return r.save(c);
    }

    public List<CitaMedica> listar() {
        return r.findAll();
    }

    private static ResponseStatusException malo(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}