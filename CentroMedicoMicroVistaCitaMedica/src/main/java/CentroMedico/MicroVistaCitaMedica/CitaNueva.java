package CentroMedico.MicroVistaCitaMedica;

// Cuerpo de POST /cita/grabar para una cita nueva (el id y el estado pendiente los pone el backend).
public record CitaNueva(String fec, String motivo) { }