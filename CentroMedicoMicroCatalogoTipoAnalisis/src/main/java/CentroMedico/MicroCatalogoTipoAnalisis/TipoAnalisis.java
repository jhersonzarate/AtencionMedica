package CentroMedico.MicroCatalogoTipoAnalisis;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class TipoAnalisis {
    @Id
    @GeneratedValue
    private Long idTipoAnalisis;
    private String nom;
    private double pre;

    public TipoAnalisis() { }

    public Long getIdTipoAnalisis() { return idTipoAnalisis; }
    public void setIdTipoAnalisis(Long idTipoAnalisis) { this.idTipoAnalisis = idTipoAnalisis; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public double getPre() { return pre; }
    public void setPre(double pre) { this.pre = pre; }
}