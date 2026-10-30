package CentroMedico.MicroAtencionMedica;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
@JsonPropertyOrder({"idAtencion", "fec", "diagnostico", "tratamiento"})
public class AtencionMedica {
    @Id
    @GeneratedValue
    private Long idAtencion;
    private String fec;
    private String diagnostico;
    private String tratamiento;

    public AtencionMedica() { }

    public Long getIdAtencion() { return idAtencion; }
    public void setIdAtencion(Long idAtencion) { this.idAtencion = idAtencion; }
    public String getFec() { return fec; }
    public void setFec(String fec) { this.fec = fec; }
    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
}