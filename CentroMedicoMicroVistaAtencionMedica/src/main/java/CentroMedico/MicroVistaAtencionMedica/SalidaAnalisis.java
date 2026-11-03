package CentroMedico.MicroVistaAtencionMedica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SalidaAnalisis(List<LineaAnalisis> cestaAnalisis, double totalAnalisis) { }