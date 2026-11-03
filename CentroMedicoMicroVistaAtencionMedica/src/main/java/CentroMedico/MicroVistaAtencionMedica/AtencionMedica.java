package CentroMedico.MicroVistaAtencionMedica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Se envía sin idAtencion y vuelve con él.
@JsonIgnoreProperties(ignoreUnknown = true)
public record AtencionMedica(Long idAtencion, String fec, String diagnostico, String tratamiento) { }