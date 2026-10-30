package CentroMedico.MicroVistaValidarMedico;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

// Lo que devuelve MicroValidarMedico (la pass nunca viene en la respuesta).
@JsonIgnoreProperties(ignoreUnknown = true)
public record Medico(Long idMedico, String nom, String especialidad) implements Serializable { }