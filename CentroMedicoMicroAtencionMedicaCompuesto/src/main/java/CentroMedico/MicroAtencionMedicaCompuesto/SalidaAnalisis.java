package CentroMedico.MicroAtencionMedicaCompuesto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonPropertyOrder({"cestaAnalisis", "totalAnalisis"})
public record SalidaAnalisis(List<LineaAnalisis> cestaAnalisis, double totalAnalisis) { }