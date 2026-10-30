package CentroMedico.MicroCarritoAnalisis;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Servicio {
    @Autowired
    private Repositorio r;

    public LineaAnalisis agregar(LineaAnalisis lin) {
        if (lin == null || lin.getNom() == null || lin.getNom().isBlank() || !(lin.getPre() > 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La línea de análisis no es válida");
        }
        lin.setIdLin(null);
        return r.save(lin);
    }

    public void quitar(Long idLin) {
        r.deleteById(idLin);
    }

    public void limpiar() {
        r.deleteAll();
    }

    public List<LineaAnalisis> listar() {
        return r.findAll();
    }

    public double getTotal() {
        double total = 0;
        for (LineaAnalisis l : r.findAll()) {
            total += l.getImporte();
        }
        return total;
    }
}