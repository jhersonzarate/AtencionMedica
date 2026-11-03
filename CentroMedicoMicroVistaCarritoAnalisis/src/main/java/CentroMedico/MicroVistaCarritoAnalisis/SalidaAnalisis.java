package CentroMedico.MicroVistaCarritoAnalisis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// La SalidaAnalisis del diagrama: cestaAnalisis + totalAnalisis.
@JsonIgnoreProperties(ignoreUnknown = true)
public record SalidaAnalisis(List<LineaAnalisis> cestaAnalisis, double totalAnalisis) { }