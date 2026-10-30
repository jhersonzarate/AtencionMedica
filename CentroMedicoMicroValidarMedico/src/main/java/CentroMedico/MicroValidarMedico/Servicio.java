package CentroMedico.MicroValidarMedico;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Servicio {
    @Autowired
    private Repositorio r;

    // Del diagrama: devuelve el médico si el código y la pass coinciden; si no, null.
    public Medico validar(EntradaMedico e) {
        if (e == null || e.getIdMedico() == null || e.getPass() == null) {
            return null;
        }
        Medico m = r.findById(e.getIdMedico()).orElse(null);
        if (m != null && e.getPass().equals(m.getPass())) {
            return m;
        }
        return null;
    }

    // Apoyo de pruebas (no está en el diagrama): sembrar y ver médicos.
    public Medico grabar(Medico m) {
        if (m == null || m.getNom() == null || m.getNom().isBlank()
                || m.getPass() == null || m.getPass().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre y la pass son obligatorios");
        }
        m.setIdMedico(null);
        return r.save(m);
    }

    public List<Medico> listar() {
        return r.findAll();
    }
}