package CentroMedico.MicroVistaHistorialClinico;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

// Lo que devuelve MicroHistorialClinico en GET /historial/consultar/{idHistorial}
@JsonIgnoreProperties(ignoreUnknown = true)
public record HistorialClinico(Long idHistorial, String fecCreacion, String dniPaciente) implements Serializable { }