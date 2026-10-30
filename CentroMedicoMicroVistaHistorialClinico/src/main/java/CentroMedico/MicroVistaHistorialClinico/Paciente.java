package CentroMedico.MicroVistaHistorialClinico;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

// Igual que el paciente del backend: sirve para enviar (grabar) y para recibir (grabar y listar).
@JsonIgnoreProperties(ignoreUnknown = true)
public record Paciente(String dni, String nom, String ape, String tel, String dir) implements Serializable { }