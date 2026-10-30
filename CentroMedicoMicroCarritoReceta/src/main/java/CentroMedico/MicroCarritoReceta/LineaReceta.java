package CentroMedico.MicroCarritoReceta;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class LineaReceta {
    @Id
    @GeneratedValue
    private Long idLin;
    private String nom;
    private double pre;
    private int cant;
    private String indicacion;

    public LineaReceta() { }

    public double getImporte() { return pre * cant; }

    public Long getIdLin() { return idLin; }
    public void setIdLin(Long idLin) { this.idLin = idLin; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public double getPre() { return pre; }
    public void setPre(double pre) { this.pre = pre; }
    public int getCant() { return cant; }
    public void setCant(int cant) { this.cant = cant; }
    public String getIndicacion() { return indicacion; }
    public void setIndicacion(String indicacion) { this.indicacion = indicacion; }
}