package CentroMedico.MicroRecetaCompuesto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Servicio {
    private static final String CATALOGO = "http://MicroCatalogoMedicamento/medicamento";
    private static final String CARRITO = "http://MicroCarritoReceta/carritoreceta";
    private static final int MAX_LINEAS = 10;

    @Autowired
    private RestTemplate rt;

    // Vacía la cesta (no devuelve stock: se usa al terminar una atención).
    public void nuevaReceta() {
        rt.delete(CARRITO + "/limpiar");
    }

    public List<Medicamento> listarMedicamentos() {
        Medicamento[] r = rt.getForObject(CATALOGO + "/listar", Medicamento[].class);
        return r == null ? new ArrayList<>() : Arrays.asList(r);
    }

    // Descuenta el stock en el catálogo y agrega la línea a la cesta.
    public void agregarMedicamento(EntradaReceta e) {
        if (e == null || e.idMedicamento() == null || e.cant() < 1) {
            throw malo("Indique el medicamento y una cantidad de al menos 1");
        }
        String ind = e.indicacion() == null ? "" : e.indicacion().trim();
        if (ind.length() < 3 || ind.length() > 120) {
            throw malo("La indicación debe tener de 3 a 120 caracteres");
        }
        Medicamento m = rt.getForObject(CATALOGO + "/buscar/" + e.idMedicamento(), Medicamento.class);
        if (m == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el medicamento " + e.idMedicamento());
        }
        if (m.stock() < e.cant()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stock insuficiente de " + m.nom() + " (disponible: " + m.stock() + ")");
        }
        List<LineaReceta> actual = verReceta();
        if (actual.size() >= MAX_LINEAS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La receta admite máximo " + MAX_LINEAS + " líneas");
        }
        for (LineaReceta l : actual) {
            if (l.nom().equalsIgnoreCase(m.nom())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese medicamento ya está en la receta");
            }
        }
        rt.postForObject(CATALOGO + "/grabar",
                new Medicamento(m.idMedicamento(), m.nom(), m.pre(), m.stock() - e.cant()), Medicamento.class);
        try {
            rt.postForObject(CARRITO + "/agregar",
                    new LineaReceta(null, m.nom(), m.pre(), e.cant(), ind, m.pre() * e.cant()), LineaReceta.class);
        } catch (RuntimeException ex) {
            try {
                rt.postForObject(CATALOGO + "/grabar", m, Medicamento.class); // devuelve el stock
            } catch (RuntimeException ignorada) {
                // si tampoco responde el catálogo, el stock queda descontado
            }
            throw ex;
        }
    }

    // Quita la línea de la cesta y devuelve su cantidad al stock.
    public void quitarMedicamento(Long idLin) {
        LineaReceta quitada = null;
        for (LineaReceta l : verReceta()) {
            if (l.idLin().equals(idLin)) {
                quitada = l;
                break;
            }
        }
        if (quitada == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la línea " + idLin + " en la receta");
        }
        rt.delete(CARRITO + "/quitar/" + idLin);
        for (Medicamento m : listarMedicamentos()) {
            if (m.nom().equalsIgnoreCase(quitada.nom())) {
                rt.postForObject(CATALOGO + "/grabar",
                        new Medicamento(m.idMedicamento(), m.nom(), m.pre(), m.stock() + quitada.cant()),
                        Medicamento.class);
                break;
            }
        }
    }

    public List<LineaReceta> verReceta() {
        LineaReceta[] r = rt.getForObject(CARRITO + "/listar", LineaReceta[].class);
        return r == null ? new ArrayList<>() : Arrays.asList(r);
    }

    public double verTotal() {
        Double t = rt.getForObject(CARRITO + "/total", Double.class);
        return t == null ? 0 : t;
    }

    // Arma la SalidaReceta del diagrama (cesta + total) para quien la consuma.
    public SalidaReceta resumen() {
        return new SalidaReceta(verReceta(), verTotal());
    }

    private static ResponseStatusException malo(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}