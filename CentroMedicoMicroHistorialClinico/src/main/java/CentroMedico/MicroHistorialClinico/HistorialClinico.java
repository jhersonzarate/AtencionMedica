package CentroMedico.MicroHistorialClinico;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class HistorialClinico {
    @Id
    @GeneratedValue
    private Long idHistorial;
    private String fecCreacion;
    private String dniPaciente;

    public HistorialClinico() { }

    public Long getIdHistorial() { return idHistorial; }
    public void setIdHistorial(Long idHistorial) { this.idHistorial = idHistorial; }
    public String getFecCreacion() { return fecCreacion; }
    public void setFecCreacion(String fecCreacion) { this.fecCreacion = fecCreacion; }
    public String getDniPaciente() { return dniPaciente; }
    public void setDniPaciente(String dniPaciente) { this.dniPaciente = dniPaciente; }
}