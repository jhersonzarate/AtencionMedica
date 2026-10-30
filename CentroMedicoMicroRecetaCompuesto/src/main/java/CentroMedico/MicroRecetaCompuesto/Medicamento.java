package CentroMedico.MicroRecetaCompuesto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"idMedicamento", "nom", "pre", "stock"})
public record Medicamento(Long idMedicamento, String nom, double pre, int stock) { }