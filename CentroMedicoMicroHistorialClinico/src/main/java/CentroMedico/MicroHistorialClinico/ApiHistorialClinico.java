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

    @GetMapping("/consultarPorDni/{dni}")
    public HistorialClinico consultarPorDni(@PathVariable String dni) { return s.consultarPorDni(dni); }

    @PostMapping("/grabar")
    public HistorialClinico grabar(@RequestBody HistorialClinico h) { return s.grabar(h); }

    @GetMapping("/listar")
    public List<HistorialClinico> listar() { return s.listar(); }

    @PostMapping("/paciente/grabar")
    public Paciente grabarPaciente(@RequestBody Paciente p) { return s.grabarPaciente(p); }

    @GetMapping("/paciente/consultar/{dni}")
    public Paciente consultarPaciente(@PathVariable String dni) { return s.consultarPaciente(dni); }

    @GetMapping("/paciente/listar")
    public List<Paciente> listarPacientes() { return s.listarPacientes(); }
}