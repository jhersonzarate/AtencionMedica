package CentroMedico.MicroCitaMedica;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Repositorio extends JpaRepository<CitaMedica, Long> {
}