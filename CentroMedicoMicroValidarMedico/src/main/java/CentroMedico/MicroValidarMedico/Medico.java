package CentroMedico.MicroValidarMedico;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
@JsonPropertyOrder({"idMedico", "nom", "pass", "especialidad"})
public class Medico {
    @Id
    @GeneratedValue
    private Long idMedico;
    private String nom;
    // Entra al grabar, pero nunca sale en validar, grabar ni listar.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String pass;
    private String especialidad;

    public Medico() { }

    public Long getIdMedico() { return idMedico; }
    public void setIdMedico(Long idMedico) { this.idMedico = idMedico; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
}