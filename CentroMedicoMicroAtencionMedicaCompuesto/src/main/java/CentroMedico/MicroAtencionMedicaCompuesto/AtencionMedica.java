package CentroMedico.MicroAtencionMedicaCompuesto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"idAtencion", "fec", "diagnostico", "tratamiento"})
public record AtencionMedica(Long idAtencion, String fec, String diagnostico, String tratamiento) { }