package CentroMedico.MicroCarritoAnalisis;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class LineaAnalisis {
    @Id
    @GeneratedValue
    private Long idLin;
    private String nom;
    private double pre;
    private String indicacion;

    public LineaAnalisis() { }

    // Un análisis se solicita una sola vez: el importe es el precio.
    public double getImporte() { return pre; }

    public Long getIdLin() { return idLin; }
    public void setIdLin(Long idLin) { this.idLin = idLin; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public double getPre() { return pre; }
    public void setPre(double pre) { this.pre = pre; }
    public String getIndicacion() { return indicacion; }
    public void setIndicacion(String indicacion) { this.indicacion = indicacion; }
}