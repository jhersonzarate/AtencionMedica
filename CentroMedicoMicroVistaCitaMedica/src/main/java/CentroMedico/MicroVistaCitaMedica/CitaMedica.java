package CentroMedico.MicroVistaCitaMedica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

// Lo que devuelve MicroCitaMedica (consultar y grabar). estado: false = pendiente, true = atendida.
@JsonIgnoreProperties(ignoreUnknown = true)
public record CitaMedica(Long idCita, String fec, String motivo, Boolean estado) implements Serializable { }