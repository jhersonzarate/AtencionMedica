package CentroMedico.MicroVistaCarritoReceta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Servicio {
    private static final String RECETA = "http://MicroRecetaCompuesto/receta";

    @Autowired
    private RestTemplate rt;

    // Del diagrama: listarMedicamentos()
    public List<Medicamento> listarMedicamentos() {
        Medicamento[] r = rt.getForObject(RECETA + "/medicamentos", Medicamento[].class);
        return r == null ? new ArrayList<>() : Arrays.asList(r);
    }

    // Del diagrama: agregarMedicamento(e). El compuesto valida y descuenta el stock.
    public void agregarMedicamento(EntradaReceta e) {
        rt.postForObject(RECETA + "/agregar", e, Void.class);
    }

    // Del diagrama: quitarMedicamento(idLin). El compuesto devuelve el stock.
    public void quitarMedicamento(Long idLin) {
        rt.delete(RECETA + "/quitar/{id}", idLin);
    }

    // Del diagrama: verReceta() + verTotal(), en una sola llamada (SalidaReceta).
    public SalidaReceta verResumen() {
        return rt.getForObject(RECETA + "/resumen", SalidaReceta.class);
    }

    // Extra para que el catálogo vacío se pueda llenar.
    public Medicamento grabarMedicamento(MedicamentoNuevo m) {
        return rt.postForObject(RECETA + "/grabarmedicamento", m, Medicamento.class);
    }
}