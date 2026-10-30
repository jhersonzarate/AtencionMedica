package CentroMedico.MicroValidarMedico;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Repositorio extends JpaRepository<Medico, Long> {
}