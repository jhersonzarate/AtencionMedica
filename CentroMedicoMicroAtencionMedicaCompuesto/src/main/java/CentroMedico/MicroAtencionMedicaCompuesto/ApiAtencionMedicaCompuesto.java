package CentroMedico.MicroAtencionMedicaCompuesto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/atencion")
public class ApiAtencionMedicaCompuesto {
    @Autowired
    private Servicio s;

    @PostMapping("/registrar")
    public SalidaAtencionMedica registrarAtencion(@RequestBody EntradaAtencionMedica e) {
        return s.registrarAtencion(e);
    }

    @GetMapping("/previa")
    public SalidaAtencionMedica verPrevia() {
        return s.verPrevia();
    }

    @GetMapping("/detalle")
    public SalidaAtencionMedica obtenerDetalleAtencion() {
        return s.obtenerDetalleAtencion();
    }
}