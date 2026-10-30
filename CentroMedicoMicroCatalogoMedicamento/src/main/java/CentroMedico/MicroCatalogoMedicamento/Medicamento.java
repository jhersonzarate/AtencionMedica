package CentroMedico.MicroCatalogoMedicamento;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
@JsonPropertyOrder({"idMedicamento", "nom", "pre", "stock"})
public class Medicamento {
    @Id
    @GeneratedValue
    private Long idMedicamento;
    private String nom;
    private double pre;
    private int stock;

    public Medicamento() { }

    public Long getIdMedicamento() { return idMedicamento; }
    public void setIdMedicamento(Long idMedicamento) { this.idMedicamento = idMedicamento; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public double getPre() { return pre; }
    public void setPre(double pre) { this.pre = pre; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}