package CentroMedico.MicroRecetaCompuesto;

import java.util.List;

public record SalidaReceta(List<LineaReceta> cestaReceta, double totalReceta) { }