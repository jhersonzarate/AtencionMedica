package CentroMedico.MicroCatalogoMedicamento;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Servicio {
    @Autowired
    private Repositorio r;

    // Sin idMedicamento crea uno nuevo; con idMedicamento existente lo actualiza
    // (así MicroRecetaCompuesto descuenta y repone el stock).
    public Medicamento grabar(Medicamento m) {
        if (m == null || m.getNom() == null) {
            throw malo("Faltan los datos del medicamento");
        }
        String nom = m.getNom().trim();
        if (nom.length() < 2 || nom.length() > 80) {
            throw malo("El nombre debe tener de 2 a 80 caracteres");
        }
        if (!(m.getPre() > 0)) {
            throw malo("El precio debe ser mayor que 0");
        }
        if (m.getStock() < 0) {
            throw malo("El stock no puede ser negativo");
        }
        if (m.getIdMedicamento() == null) {
            if (r.existsByNomIgnoreCase(nom)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un medicamento con ese nombre");
            }
        } else if (!r.existsById(m.getIdMedicamento())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el medicamento " + m.getIdMedicamento());
        }
        m.setNom(nom);
        return r.save(m);
    }

    public Medicamento buscar(Long idMedicamento) {
        return r.findById(idMedicamento).orElse(null);
    }

    public List<Medicamento> listar() {
        return r.findAll();
    }

    private static ResponseStatusException malo(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}