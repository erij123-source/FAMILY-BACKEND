package tn.educanet.backend.persistence;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "budget_historique", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_membre", "mois", "annee"})
})
public class BudgetHistorique implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_membre", nullable = false)
    private Membre membre;

    @Column(nullable = false)
    private int mois;

    @Column(nullable = false)
    private int annee;

    @Column(nullable = false)
    private double budget;

    @Column(nullable = false)
    private double carryover;

    public BudgetHistorique() {
        this.budget   = 0.0;
        this.carryover = 0.0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Membre getMembre() { return membre; }
    public void setMembre(Membre membre) { this.membre = membre; }

    public int getMois() { return mois; }
    public void setMois(int mois) { this.mois = mois; }

    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public double getCarryover() { return carryover; }
    public void setCarryover(double carryover) { this.carryover = carryover; }
}