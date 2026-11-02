package CentroMedico.MicroHistorialClinico;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/historial")
public class ApiHistorialClinico {
    @Autowired
    private Servicio s;

    @GetMapping("/consultar/{idHistorial}")
    public HistorialClinico consultarHistorial(@PathVariable Long idHistorial) {
        return s.consultarHistorial(idHistorial);
    }

    @PostMapping("/grabar")
    public HistorialClinico grabar(@RequestBody HistorialClinico h) { return s.grabar(h); }

    @GetMapping("/listar")
    public List<HistorialClinico> listar() { return s.listar(); }

    // Paciente: solo grabar y listar (no se busca; se consulta por el historial).
    @PostMapping("/paciente/grabar")
    public Paciente grabarPaciente(@RequestBody Paciente p) { return s.grabarPaciente(p); }

    @GetMapping("/paciente/listar")
    public List<Paciente> listarPacientes() { return s.listarPacientes(); }
}