package CentroMedico.MicroVistaAtencionMedica;

// La EntradaAtencionMedica del diagrama.
public record EntradaAtencionMedica(HistorialClinico historialClinico, MedicoFirma medico,
                                    CitaMedica citaMedica, AtencionMedica atencionMedica) { }