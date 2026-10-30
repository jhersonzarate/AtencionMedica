package CentroMedico.MicroCatalogoMedicamento;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/medicamento")
public class ApiCatalogoMedicamento {
    @Autowired
    private Servicio s;

    @PostMapping("/grabar")
    public Medicamento grabar(@RequestBody Medicamento m) { return s.grabar(m); }

    @GetMapping("/buscar/{idMedicamento}")
    public Medicamento buscar(@PathVariable Long idMedicamento) { return s.buscar(idMedicamento); }

    @GetMapping("/listar")
    public List<Medicamento> listar() { return s.listar(); }
}