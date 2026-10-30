package CentroMedico.MicroVistaValidarMedico;

// Cuerpo de POST /medico/grabar (el id lo genera el backend)
public record RegistroMedico(String nom, String pass, String especialidad) { }