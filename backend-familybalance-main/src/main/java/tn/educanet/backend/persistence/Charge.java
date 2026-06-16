package tn.educanet.backend.persistence;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Charge implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {
        FIXE, VARIABLE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "idCompte", nullable = false)
    private Compte compte;

    @ManyToOne
    @JoinColumn(name = "idMembre", nullable = false)
    private Membre membre;

    @ManyToOne
    @JoinColumn(name = "idCategorie")
    private Categorie categorie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private double amount;

    private double budget_limit;

    @Column(nullable = false)
    private LocalDate chargeDate;

    // Date à laquelle l'argent sort effectivement du compte. Indépendante de
    // chargeDate : permet à l'utilisateur d'enregistrer une charge "pour août"
    // payée en mai. Les totaux mensuels et le calcul du reste à vivre sont
    // basés sur paymentDate (chargeDate ne sert qu'à l'affichage et au tri).
    private LocalDate paymentDate;

    private String description;

    public Charge() {
        this.budget_limit = 0.0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Compte getCompte() { return compte; }
    public void setCompte(Compte compte) { this.compte = compte; }

    public Membre getMembre() { return membre; }
    public void setMembre(Membre membre) { this.membre = membre; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getCeiling() { return budget_limit; }
    public void setCeiling(double budget_limit) { this.budget_limit = budget_limit; }

    public LocalDate getChargeDate() { return chargeDate; }
    public void setChargeDate(LocalDate chargeDate) { this.chargeDate = chargeDate; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

}