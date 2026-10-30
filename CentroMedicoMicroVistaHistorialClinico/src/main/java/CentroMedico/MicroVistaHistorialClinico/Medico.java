package CentroMedico.MicroVistaHistorialClinico;

import java.io.Serializable;

// El médico que viene en el pase firmado desde el login.
public record Medico(Long idMedico, String nom, String especialidad) implements Serializable { }