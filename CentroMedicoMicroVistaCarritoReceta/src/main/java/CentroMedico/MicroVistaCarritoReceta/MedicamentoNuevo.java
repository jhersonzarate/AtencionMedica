package CentroMedico.MicroVistaCarritoReceta;

// Cuerpo de POST /receta/grabarmedicamento (el id lo genera el catálogo).
public record MedicamentoNuevo(String nom, double pre, int stock) { }