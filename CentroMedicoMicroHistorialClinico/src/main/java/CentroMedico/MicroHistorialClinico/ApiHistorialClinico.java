package CentroMedico.MicroHistorialClinico;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/historial")
public class ApiHistorialClinico {
    @Autowired
    private Servicio s;

    // Del diagrama: consultarHistorial(dniPaciente)
    @GetMapping("/consultar/{dniPaciente}")
    public HistorialClinico consultarHistorial(@PathVariable String dniPaciente) {
        return s.consultarHistorial(dniPaciente);
    }

    @PostMapping("/grabar")
    public HistorialClinico grabar(@RequestBody HistorialClinico h) { return s.grabar(h); }

    @GetMapping("/listar")
    public List<HistorialClinico> listar() { return s.listar(); }

    // Paciente: solo grabar y listar (sus datos se ven al consultar el historial por DNI).
    @PostMapping("/paciente/grabar")
    public Paciente grabarPaciente(@RequestBody Paciente p) { return s.grabarPaciente(p); }

    @GetMapping("/paciente/listar")
    public List<Paciente> listarPacientes() { return s.listarPacientes(); }
}