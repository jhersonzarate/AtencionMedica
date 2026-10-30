package CentroMedico.MicroHistorialClinico;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Repositorio extends JpaRepository<HistorialClinico, Long> {
    Optional<HistorialClinico> findFirstByDniPaciente(String dniPaciente);
}