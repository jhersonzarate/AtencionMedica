package CentroMedico.MicroHistorialClinico;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Servicio {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    @Autowired
    private Repositorio r;
    @Autowired
    private RepositorioPaciente rp;

    // Del diagrama
    public HistorialClinico consultarHistorial(Long idHistorial) {
        return r.findById(idHistorial).orElse(null);
    }

    // Consulta por DNI (el DNI es el id del paciente)
    public HistorialClinico consultarPorDni(String dni) {
        return r.findFirstByDniPaciente(dni).orElse(null);
    }

    // Apoyo de pruebas: el paciente debe existir antes de crear su historial.
    public HistorialClinico grabar(HistorialClinico h) {
        if (h == null || h.getDniPaciente() == null || !h.getDniPaciente().matches("\\d{8}")) {
            throw malo("El DNI del paciente debe tener 8 dígitos");
        }
        if (!rp.existsById(h.getDniPaciente())) {
            throw malo("Primero grabe al paciente con ese DNI");
        }
        if (r.findFirstByDniPaciente(h.getDniPaciente()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese paciente ya tiene historial clínico");
        }
        if (h.getFecCreacion() == null || h.getFecCreacion().isBlank()) {
            h.setFecCreacion(LocalDate.now(LIMA).toString());
        } else {
            try {
                if (LocalDate.parse(h.getFecCreacion()).isAfter(LocalDate.now(LIMA))) {
                    throw malo("La fecha de creación no puede ser futura");
                }
            } catch (java.time.format.DateTimeParseException ex) {
                throw malo("La fecha de creación no es válida (aaaa-mm-dd)");
            }
        }
        h.setIdHistorial(null);
        return r.save(h);
    }

    public List<HistorialClinico> listar() {
        return r.findAll();
    }

    // ---- Paciente (solo NetBeans; no está en el diagrama) ----
        public Paciente grabarPaciente(Paciente p) {
        if (p == null || p.getDni() == null || !p.getDni().matches("\\d{8}")) {
            throw malo("El DNI debe tener 8 dígitos");
        }
        if (p.getNom() == null || p.getNom().isBlank() || p.getApe() == null || p.getApe().isBlank()) {
            throw malo("El nombre y los apellidos son obligatorios");
        }
        if (p.getTel() == null || !p.getTel().trim().matches("\\d{7,9}")) {
            throw malo("El teléfono debe tener de 7 a 9 dígitos");
        }
        if (p.getDir() == null || p.getDir().trim().length() < 5 || p.getDir().trim().length() > 120) {
            throw malo("La dirección debe tener de 5 a 120 caracteres");
        }
        if (rp.existsById(p.getDni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un paciente con ese DNI");
        }
        p.setNom(p.getNom().trim());
        p.setApe(p.getApe().trim());
        p.setTel(p.getTel().trim());
        p.setDir(p.getDir().trim());
        return rp.save(p);
    }

    public Paciente consultarPaciente(String dni) {
        return rp.findById(dni).orElse(null);
    }

    public List<Paciente> listarPacientes() {
        return rp.findAll();
    }

    private static ResponseStatusException malo(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}