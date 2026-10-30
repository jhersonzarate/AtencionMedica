package CentroMedico.MicroAtencionMedicaCompuesto;

public record SalidaAtencionMedica(AtencionMedica atencionMedica, SalidaReceta recetaCompuesto,
                                   SalidaAnalisis analisisCompuesto) { }