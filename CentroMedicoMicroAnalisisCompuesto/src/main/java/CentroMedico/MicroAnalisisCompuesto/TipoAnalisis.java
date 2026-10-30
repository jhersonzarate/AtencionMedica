package CentroMedico.MicroAnalisisCompuesto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"idTipoAnalisis", "nom", "pre"})
public record TipoAnalisis(Long idTipoAnalisis, String nom, double pre) { }