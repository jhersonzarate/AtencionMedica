package CentroMedico.MicroCarritoReceta;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carritoreceta")
public class ApiCarritoReceta {
    @Autowired
    private Servicio s;

    @PostMapping("/agregar")
    public LineaReceta agregar(@RequestBody LineaReceta lin) { return s.agregar(lin); }

    @DeleteMapping("/quitar/{idLin}")
    public void quitar(@PathVariable Long idLin) { s.quitar(idLin); }

    @DeleteMapping("/limpiar")
    public void limpiar() { s.limpiar(); }

    @GetMapping("/listar")
    public List<LineaReceta> listar() { return s.listar(); }

    @GetMapping("/total")
    public double getTotal() { return s.getTotal(); }
}