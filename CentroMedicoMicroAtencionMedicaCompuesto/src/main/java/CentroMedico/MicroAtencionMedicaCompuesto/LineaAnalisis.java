package CentroMedico.MicroAtencionMedicaCompuesto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LineaAnalisis(Long idLin, String nom, double pre, String indicacion, double importe) { }