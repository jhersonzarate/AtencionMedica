package CentroMedico.MicroCitaMedica;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cita")
public class ApiCitaMedica {
    @Autowired
    private Servicio s;

    @GetMapping("/consultar/{idCita}")
    public CitaMedica consultarCita(@PathVariable Long idCita) { return s.consultarCita(idCita); }

    @PostMapping("/grabar")
    public CitaMedica grabar(@RequestBody CitaMedica c) { return s.grabar(c); }

    @GetMapping("/listar")
    public List<CitaMedica> listar() { return s.listar(); }
}