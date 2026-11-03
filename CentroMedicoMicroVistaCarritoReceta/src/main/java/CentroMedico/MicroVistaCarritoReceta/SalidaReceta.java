package CentroMedico.MicroVistaCarritoReceta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// La SalidaReceta del diagrama: cestaReceta + totalReceta.
@JsonIgnoreProperties(ignoreUnknown = true)
public record SalidaReceta(List<LineaReceta> cestaReceta, double totalReceta) { }