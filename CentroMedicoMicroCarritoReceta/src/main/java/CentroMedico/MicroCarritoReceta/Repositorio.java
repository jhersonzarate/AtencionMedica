package CentroMedico.MicroCarritoReceta;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Repositorio extends JpaRepository<LineaReceta, Long> {
}