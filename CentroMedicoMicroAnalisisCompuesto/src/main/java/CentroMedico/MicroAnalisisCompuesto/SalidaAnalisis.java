package CentroMedico.MicroAnalisisCompuesto;

import java.util.List;

public record SalidaAnalisis(List<LineaAnalisis> cestaAnalisis, double totalAnalisis) { }