package CentroMedico.MicroRecetaCompuesto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"idLin", "nom", "pre", "cant", "indicacion", "importe"})
public record LineaReceta(Long idLin, String nom, double pre, int cant, String indicacion, double importe) { }