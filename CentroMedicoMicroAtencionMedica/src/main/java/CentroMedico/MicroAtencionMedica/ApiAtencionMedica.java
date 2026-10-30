package CentroMedico.MicroAtencionMedica;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/atencionmedica")
public class ApiAtencionMedica {
    @Autowired
    private Servicio s;

    @PostMapping("/registrar")
    public AtencionMedica registrarAtencion(@RequestBody AtencionMedica a) { return s.registrarAtencion(a); }

    @GetMapping("/listar")
    public List<AtencionMedica> listar() { return s.listar(); }
}