package CentroMedico.MicroCatalogoTipoAnalisis;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tipoanalisis")
public class ApiCatalogoTipoAnalisis {
    @Autowired
    private Servicio s;

    @PostMapping("/grabar")
    public TipoAnalisis grabar(@RequestBody TipoAnalisis t) { return s.grabar(t); }

    @GetMapping("/buscar/{idTipoAnalisis}")
    public TipoAnalisis buscar(@PathVariable Long idTipoAnalisis) { return s.buscar(idTipoAnalisis); }

    @GetMapping("/listar")
    public List<TipoAnalisis> listar() { return s.listar(); }
}