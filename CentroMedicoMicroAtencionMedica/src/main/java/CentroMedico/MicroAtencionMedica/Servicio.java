package CentroMedico.MicroAtencionMedica;

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

    // Del diagrama (devuelve la atención guardada para conocer su idAtencion).
    public AtencionMedica registrarAtencion(AtencionMedica a) {
        if (a == null) {
            throw malo("Faltan los datos de la atención");
        }
        LocalDate fec;
        try {
            fec = LocalDate.parse(a.getFec());
        } catch (RuntimeException ex) {
            throw malo("La fecha de atención no es válida (aaaa-mm-dd)");
        }
        if (fec.isAfter(LocalDate.now(LIMA))) {
            throw malo("La fecha de atención no puede ser futura");
        }
        String dx = a.getDiagnostico() == null ? "" : a.getDiagnostico().trim();
        String tx = a.getTratamiento() == null ? "" : a.getTratamiento().trim();
        if (dx.length() < 5 || dx.length() > 500) {
            throw malo("El diagnóstico debe tener de 5 a 500 caracteres");
        }
        if (tx.length() < 5 || tx.length() > 500) {
            throw malo("El tratamiento debe tener de 5 a 500 caracteres");
        }
        a.setIdAtencion(null);
        a.setFec(fec.toString());
        a.setDiagnostico(dx);
        a.setTratamiento(tx);
        return r.save(a);
    }

    // Apoyo de pruebas: ver las atenciones guardadas en el navegador.
    public List<AtencionMedica> listar() {
        return r.findAll();
    }

    private static ResponseStatusException malo(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}