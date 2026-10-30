package CentroMedico.MicroAtencionMedicaCompuesto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonPropertyOrder({"cestaReceta", "totalReceta"})
public record SalidaReceta(List<LineaReceta> cestaReceta, double totalReceta) { }