package CentroMedico.MicroVistaAtencionMedica;

// El médico que firma: su código (de la sesión) y la contraseña que acaba de escribir.
public record MedicoFirma(Long idMedico, String pass) { }