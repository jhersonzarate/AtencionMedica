package CentroMedico.MicroAtencionMedicaCompuesto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"atencionMedica", "recetaCompuesto", "analisisCompuesto"})
public record SalidaAtencionMedica(AtencionMedica atencionMedica, SalidaReceta recetaCompuesto,
                                   SalidaAnalisis analisisCompuesto) { }