package CentroMedico.MicroAnalisisCompuesto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"idLin", "nom", "pre", "indicacion", "importe"})
public record LineaAnalisis(Long idLin, String nom, double pre, String indicacion, double importe) { }