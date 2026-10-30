package CentroMedico.MicroHistorialClinico;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioPaciente extends JpaRepository<Paciente, String> {
}