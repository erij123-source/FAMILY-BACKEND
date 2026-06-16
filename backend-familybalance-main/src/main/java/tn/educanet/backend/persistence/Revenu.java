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
public class Revenu implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Kind {
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

    @Column(nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Kind kind;

    @Column(nullable = false)
    private LocalDate perceptionDate;

    private String description;

    // ── FIX #3 + #8 : champs pour filtrage précis par mois/année ───────────
    @Column
    private String type;         // ex: "DISTRIBUTION_BUDGET"

    @Column
    private Integer mois;        // ex: 5 pour mai

    @Column
    private Integer annee;       // ex: 2026

    @Column
    private String moisNom;      // ex: "mai"
    // ────────────────────────────────────────────────────────────────────────

    public Revenu() {
        this.kind = Kind.FIXE;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Compte getCompte() { return compte; }
    public void setCompte(Compte compte) { this.compte = compte; }

    public Membre getMembre() { return membre; }
    public void setMembre(Membre membre) { this.membre = membre; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Kind getKind() { return kind; }
    public void setKind(Kind kind) { this.kind = kind; }

    public LocalDate getPerceptionDate() { return perceptionDate; }
    public void setPerceptionDate(LocalDate perceptionDate) { this.perceptionDate = perceptionDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // ── Getters / Setters FIX #3 + #8 ──────────────────────────────────────
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getMois() { return mois; }
    public void setMois(Integer mois) { this.mois = mois; }

    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }

    public String getMoisNom() { return moisNom; }
    public void setMoisNom(String moisNom) { this.moisNom = moisNom; }
    // ────────────────────────────────────────────────────────────────────────
}