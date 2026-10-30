package CentroMedico.MicroAnalisisCompuesto;

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
    private static final String CATALOGO = "http://MicroCatalogoTipoAnalisis/tipoanalisis";
    private static final String CARRITO = "http://MicroCarritoAnalisis/carritoanalisis";
    private static final int MAX_LINEAS = 10;

    @Autowired
    private RestTemplate rt;

    public void nuevoAnalisis() {
        rt.delete(CARRITO + "/limpiar");
    }

    public List<TipoAnalisis> listarTiposAnalisis() {
        TipoAnalisis[] r = rt.getForObject(CATALOGO + "/listar", TipoAnalisis[].class);
        return r == null ? new ArrayList<>() : Arrays.asList(r);
    }

    // Un análisis se solicita una sola vez: no hay cantidad.
    public void agregarTipoAnalisis(EntradaAnalisis e) {
        if (e == null || e.idTipoAnalisis() == null) {
            throw malo("Indique el tipo de análisis");
        }
        String ind = e.indicacion() == null ? "" : e.indicacion().trim();
        if (ind.length() < 3 || ind.length() > 120) {
            throw malo("La indicación debe tener de 3 a 120 caracteres");
        }
        TipoAnalisis t = rt.getForObject(CATALOGO + "/buscar/" + e.idTipoAnalisis(), TipoAnalisis.class);
        if (t == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el tipo de análisis " + e.idTipoAnalisis());
        }
        List<LineaAnalisis> actual = verAnalisis();
        if (actual.size() >= MAX_LINEAS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El pedido admite máximo " + MAX_LINEAS + " líneas");
        }
        for (LineaAnalisis l : actual) {
            if (l.nom().equalsIgnoreCase(t.nom())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese análisis ya está en el pedido");
            }
        }
        rt.postForObject(CARRITO + "/agregar",
                new LineaAnalisis(null, t.nom(), t.pre(), ind, t.pre()), LineaAnalisis.class);
    }

    public void quitarTipoAnalisis(Long idLin) {
        for (LineaAnalisis l : verAnalisis()) {
            if (l.idLin().equals(idLin)) {
                rt.delete(CARRITO + "/quitar/" + idLin);
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la línea " + idLin + " en el pedido");
    }

    public List<LineaAnalisis> verAnalisis() {
        LineaAnalisis[] r = rt.getForObject(CARRITO + "/listar", LineaAnalisis[].class);
        return r == null ? new ArrayList<>() : Arrays.asList(r);
    }

    public double verTotal() {
        Double t = rt.getForObject(CARRITO + "/total", Double.class);
        return t == null ? 0 : t;
    }

    // Arma la SalidaAnalisis del diagrama (cesta + total) para quien la consuma.
    public SalidaAnalisis resumen() {
        return new SalidaAnalisis(verAnalisis(), verTotal());
    }

    private static ResponseStatusException malo(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}