package CentroMedico.MicroVistaHistorialClinico;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Servicio {
    private static final String HISTORIAL = "http://MicroHistorialClinico/historial";

    @Autowired
    private RestTemplate rt;

    // Del diagrama: consultarHistorial(dniPaciente). Devuelve null si el paciente no tiene historial (200 vacío).
    public HistorialClinico consultar(String dniPaciente) {
        return rt.getForObject(HISTORIAL + "/consultar/{dni}", HistorialClinico.class, dniPaciente);
    }

    public List<Paciente> listarPacientes() {
        Paciente[] v = rt.getForObject(HISTORIAL + "/paciente/listar", Paciente[].class);
        return v == null ? List.of() : List.of(v);
    }

    // 1.º el paciente (400 si algún dato no cumple, 409 si el DNI ya existe).
    public Paciente grabarPaciente(Paciente p) {
        return rt.postForObject(HISTORIAL + "/paciente/grabar", p, Paciente.class);
    }

    // 2.º su historial (el DNI ya debe estar grabado; 409 si ya tiene historial).
    public HistorialClinico crearHistorial(String dni) {
        return rt.postForObject(HISTORIAL + "/grabar", new HistorialNuevo(dni), HistorialClinico.class);
    }

    // Datos del paciente según su DNI (el backend solo los expone en su lista), o null si no aparece.
    public Paciente buscarPaciente(String dni) {
        for (Paciente p : listarPacientes()) {
            if (dni != null && dni.equals(p.dni())) {
                return p;
            }
        }
        return null;
    }
}