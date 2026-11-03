package CentroMedico.MicroVistaCarritoAnalisis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Lo que devuelve el catálogo a través de MicroAnalisisCompuesto.
@JsonIgnoreProperties(ignoreUnknown = true)
public record TipoAnalisis(Long idTipoAnalisis, String nom, double pre) { }