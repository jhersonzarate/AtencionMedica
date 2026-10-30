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

    // Del diagrama: devuelve el historial, o null si no existe (el backend responde 200 vacío).
    public HistorialClinico consultar(Long idHistorial) {
        return rt.getForObject(HISTORIAL + "/consultar/{id}", HistorialClinico.class, idHistorial);
    }

    public List<HistorialClinico> listarHistoriales() {
        HistorialClinico[] v = rt.getForObject(HISTORIAL + "/listar", HistorialClinico[].class);
        return v == null ? List.of() : List.of(v);
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

    // Historial de un paciente según su DNI, o null si todavía no tiene.
    public HistorialClinico buscarPorDni(String dni) {
        for (HistorialClinico h : listarHistoriales()) {
            if (dni.equals(h.dniPaciente())) {
                return h;
            }
        }
        return null;
    }
}