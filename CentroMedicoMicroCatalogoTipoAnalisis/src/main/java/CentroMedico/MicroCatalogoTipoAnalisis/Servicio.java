package CentroMedico.MicroCatalogoTipoAnalisis;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Servicio {
    @Autowired
    private Repositorio r;

    public TipoAnalisis grabar(TipoAnalisis t) {
        if (t == null || t.getNom() == null) {
            throw malo("Faltan los datos del tipo de análisis");
        }
        String nom = t.getNom().trim();
        if (nom.length() < 2 || nom.length() > 80) {
            throw malo("El nombre debe tener de 2 a 80 caracteres");
        }
        if (!(t.getPre() > 0)) {
            throw malo("El precio debe ser mayor que 0");
        }
        if (t.getIdTipoAnalisis() == null) {
            if (r.existsByNomIgnoreCase(nom)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un tipo de análisis con ese nombre");
            }
        } else if (!r.existsById(t.getIdTipoAnalisis())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el tipo de análisis " + t.getIdTipoAnalisis());
        }
        t.setNom(nom);
        return r.save(t);
    }

    public TipoAnalisis buscar(Long idTipoAnalisis) {
        return r.findById(idTipoAnalisis).orElse(null);
    }

    public List<TipoAnalisis> listar() {
        return r.findAll();
    }

    private static ResponseStatusException malo(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}