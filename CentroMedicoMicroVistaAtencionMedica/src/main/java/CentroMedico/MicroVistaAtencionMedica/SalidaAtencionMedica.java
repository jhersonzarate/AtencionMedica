package CentroMedico.MicroVistaAtencionMedica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// atencionMedica viene en null en la vista previa (todavía no se registra nada).
@JsonIgnoreProperties(ignoreUnknown = true)
public record SalidaAtencionMedica(AtencionMedica atencionMedica, SalidaReceta recetaCompuesto,
                                   SalidaAnalisis analisisCompuesto) { }