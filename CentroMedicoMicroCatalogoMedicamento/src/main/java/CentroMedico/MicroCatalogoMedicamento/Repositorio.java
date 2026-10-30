package CentroMedico.MicroCatalogoMedicamento;

import org.springframework.data.jpa.repository.JpaRepository;

public interface Repositorio extends JpaRepository<Medicamento, Long> {
    boolean existsByNomIgnoreCase(String nom);
}