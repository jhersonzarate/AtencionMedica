package CentroMedico.MicroCitaMedica;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class CitaMedica {
    @Id
    @GeneratedValue
    private Long idCita;
    private String fec;
    private String motivo;
    private Boolean estado;

    public CitaMedica() { }

    public Long getIdCita() { return idCita; }
    public void setIdCita(Long idCita) { this.idCita = idCita; }
    public String getFec() { return fec; }
    public void setFec(String fec) { this.fec = fec; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }
}