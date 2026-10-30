package CentroMedico.MicroAnalisisCompuesto;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analisis")
public class ApiAnalisisCompuesto {
    @Autowired
    private Servicio s;

    @PostMapping("/nuevo")
    public void nuevoAnalisis() { s.nuevoAnalisis(); }

    @GetMapping("/tipos")
    public List<TipoAnalisis> listarTiposAnalisis() { return s.listarTiposAnalisis(); }

    @PostMapping("/agregar")
    public void agregarTipoAnalisis(@RequestBody EntradaAnalisis e) { s.agregarTipoAnalisis(e); }

    @DeleteMapping("/quitar/{idLin}")
    public void quitarTipoAnalisis(@PathVariable Long idLin) { s.quitarTipoAnalisis(idLin); }

    @GetMapping("/ver")
    public List<LineaAnalisis> verAnalisis() { return s.verAnalisis(); }

    @GetMapping("/total")
    public double verTotal() { return s.verTotal(); }

    @GetMapping("/resumen")
    public SalidaAnalisis resumen() { return s.resumen(); }
}