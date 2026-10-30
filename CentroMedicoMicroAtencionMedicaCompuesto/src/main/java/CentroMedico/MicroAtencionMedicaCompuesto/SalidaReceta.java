package CentroMedico.MicroAtencionMedicaCompuesto;

import java.util.List;

public record SalidaReceta(List<LineaReceta> cestaReceta, double totalReceta) { }