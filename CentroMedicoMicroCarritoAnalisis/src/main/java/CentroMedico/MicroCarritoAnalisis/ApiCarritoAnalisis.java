package CentroMedico.MicroCarritoAnalisis;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carritoanalisis")
public class ApiCarritoAnalisis {
    @Autowired
    private Servicio s;

    @PostMapping("/agregar")
    public LineaAnalisis agregar(@RequestBody LineaAnalisis lin) { return s.agregar(lin); }

    @DeleteMapping("/quitar/{idLin}")
    public void quitar(@PathVariable Long idLin) { s.quitar(idLin); }

    @DeleteMapping("/limpiar")
    public void limpiar() { s.limpiar(); }

    @GetMapping("/listar")
    public List<LineaAnalisis> listar() { return s.listar(); }

    @GetMapping("/total")
    public double getTotal() { return s.getTotal(); }
}