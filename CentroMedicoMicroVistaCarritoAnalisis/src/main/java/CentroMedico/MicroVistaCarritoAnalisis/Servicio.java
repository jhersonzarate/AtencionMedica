package CentroMedico.MicroVistaCarritoAnalisis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Servicio {
    private static final String ANALISIS = "http://MicroAnalisisCompuesto/analisis";

    @Autowired
    private RestTemplate rt;

    // Del diagrama: listarTiposAnalisis()
    public List<TipoAnalisis> listarTiposAnalisis() {
        TipoAnalisis[] r = rt.getForObject(ANALISIS + "/tipos", TipoAnalisis[].class);
        return r == null ? new ArrayList<>() : Arrays.asList(r);
    }

    // Del diagrama: agregarTipoAnalisis(e)
    public void agregarTipoAnalisis(EntradaAnalisis e) {
        rt.postForObject(ANALISIS + "/agregar", e, Void.class);
    }

    // Del diagrama: quitarTipoAnalisis(idLin)
    public void quitarTipoAnalisis(Long idLin) {
        rt.delete(ANALISIS + "/quitar/{id}", idLin);
    }

    // Del diagrama: verAnalisis() + verTotal(), en una sola llamada (SalidaAnalisis).
    public SalidaAnalisis verResumen() {
        return rt.getForObject(ANALISIS + "/resumen", SalidaAnalisis.class);
    }

    // Extra para que el catálogo vacío se pueda llenar.
    public TipoAnalisis grabarTipoAnalisis(TipoAnalisisNuevo t) {
        return rt.postForObject(ANALISIS + "/grabartipo", t, TipoAnalisis.class);
    }
}