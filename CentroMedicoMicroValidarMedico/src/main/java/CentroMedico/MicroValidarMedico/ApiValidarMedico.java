package CentroMedico.MicroValidarMedico;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/medico")
public class ApiValidarMedico {
    @Autowired
    private Servicio s;

    @PostMapping("/validar")
    public Medico validar(@RequestBody EntradaMedico e) { return s.validar(e); }

    @PostMapping("/grabar")
    public Medico grabar(@RequestBody Medico m) { return s.grabar(m); }

    @GetMapping("/listar")
    public List<Medico> listar() { return s.listar(); }
}