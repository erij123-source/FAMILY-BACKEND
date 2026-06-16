package tn.educanet.backend.persistence;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Compte implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "idFamille", nullable = false, unique = true)
    private Famille famille;

    /** Total revenus - Total budgets distribués aux membres */
    @Column(nullable = false)
    private double remainingBalance;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private LocalDateTime lastUpdate;
    
    @Column(nullable = false)
    private double currentBalance;

    /** Report mensuel : revenu non distribué du mois précédent, transféré au
     *  nouveau mois (analogue à Membre.carryoverBudget). Nullable pour que les
     *  comptes existants migrent sans valeur par défaut imposée. Estampillé
     *  avec le mois/année auquel le report s'applique. */
    @Column
    private Double revenuCarryover;

    @Column
    private Integer carryoverMois;

    @Column
    private Integer carryoverAnnee;


    public Compte() {
        this.remainingBalance = 0.0;
        this.currentBalance = 0.0;
        this.currency = "TND";
        this.lastUpdate = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Famille getFamille() { return famille; }
    public void setFamille(Famille famille) { this.famille = famille; }

    public double getRemainingBalance() { return remainingBalance; }
    public void setRemainingBalance(double remainingBalance) { this.remainingBalance = remainingBalance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }
    
    public double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(double currentBalance) { this.currentBalance = currentBalance; }

    public Double getRevenuCarryover() { return revenuCarryover; }
    public void setRevenuCarryover(Double revenuCarryover) { this.revenuCarryover = revenuCarryover; }

    public Integer getCarryoverMois() { return carryoverMois; }
    public void setCarryoverMois(Integer carryoverMois) { this.carryoverMois = carryoverMois; }

    public Integer getCarryoverAnnee() { return carryoverAnnee; }
    public void setCarryoverAnnee(Integer carryoverAnnee) { this.carryoverAnnee = carryoverAnnee; }

}