package CentroMedico.MicroHistorialClinico;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@JsonPropertyOrder({"dni", "nom", "ape", "tel", "dir"})
public class Paciente {
    @Id
    private String dni; // el DNI es el id del paciente
    private String nom;
    private String ape;
    private String tel;
    private String dir;

    public Paciente() { }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getApe() { return ape; }
    public void setApe(String ape) { this.ape = ape; }
    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }
    public String getDir() { return dir; }
    public void setDir(String dir) { this.dir = dir; }
}