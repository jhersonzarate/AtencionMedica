package CentroMedico.MicroAtencionMedica;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Repositorio extends JpaRepository<AtencionMedica, Long> {
}