package CentroMedico.MicroCatalogoTipoAnalisis;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Repositorio extends JpaRepository<TipoAnalisis, Long> {
    boolean existsByNomIgnoreCase(String nom);
}