package CentroMedico.MicroRecetaCompuesto;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/receta")
public class ApiRecetaCompuesto {
    @Autowired
    private Servicio s;

    @PostMapping("/nueva")
    public void nuevaReceta() { s.nuevaReceta(); }

    @GetMapping("/medicamentos")
    public List<Medicamento> listarMedicamentos() { return s.listarMedicamentos(); }

    @PostMapping("/agregar")
    public void agregarMedicamento(@RequestBody EntradaReceta e) { s.agregarMedicamento(e); }

    @DeleteMapping("/quitar/{idLin}")
    public void quitarMedicamento(@PathVariable Long idLin) { s.quitarMedicamento(idLin); }

    @GetMapping("/ver")
    public List<LineaReceta> verReceta() { return s.verReceta(); }

    @GetMapping("/total")
    public double verTotal() { return s.verTotal(); }

    @GetMapping("/resumen")
    public SalidaReceta resumen() { return s.resumen(); }
}