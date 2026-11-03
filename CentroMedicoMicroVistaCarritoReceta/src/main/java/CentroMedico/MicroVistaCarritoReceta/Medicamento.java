package CentroMedico.MicroVistaCarritoReceta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Lo que devuelve el catálogo a través de MicroRecetaCompuesto.
@JsonIgnoreProperties(ignoreUnknown = true)
public record Medicamento(Long idMedicamento, String nom, double pre, int stock) { }