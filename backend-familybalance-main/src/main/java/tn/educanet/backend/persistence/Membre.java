package tn.educanet.backend.persistence;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "membre", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"idUser", "idFamille"})
})
public class Membre implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Role {
        ADMINISTRATEUR, GESTIONNAIRE, CONSULTANT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "idUser", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "idFamille", nullable = false)
    private Famille famille;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinDate;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private double budget;

    @Column(nullable = false)
    private double remainingBudget;

    @Column(nullable = false)
    private double carryoverBudget;

    @Column
    private Integer budgetMois;

    @Column
    private Integer budgetAnnee;
    
 // Après les champs budgetMois / budgetAnnee existants, ajouter :

    @Column
    private Integer carryoverMois;

    @Column
    private Integer carryoverAnnee;

    // Getters/Setters
    public Integer getCarryoverMois() { return carryoverMois; }
    public void setCarryoverMois(Integer carryoverMois) { this.carryoverMois = carryoverMois; }

    public Integer getCarryoverAnnee() { return carryoverAnnee; }
    public void setCarryoverAnnee(Integer carryoverAnnee) { this.carryoverAnnee = carryoverAnnee; }
    
 
    public Membre() {
        this.joinDate = LocalDateTime.now();
        this.active = true;
        this.role = Role.CONSULTANT;
        this.budget = 0.0;
        this.remainingBudget = 0.0;
        this.carryoverBudget = 0.0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Famille getFamille() { return famille; }
    public void setFamille(Famille famille) { this.famille = famille; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDateTime joinDate) { this.joinDate = joinDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public double getRemainingBudget() { return remainingBudget; }
    public void setRemainingBudget(double remainingBudget) { this.remainingBudget = remainingBudget; }

    public double getCarryoverBudget() { return carryoverBudget; }
    public void setCarryoverBudget(double carryoverBudget) { this.carryoverBudget = carryoverBudget; }

    // ── Getters/Setters budgetMois + budgetAnnee ────────────────────────────
    public Integer getBudgetMois() { return budgetMois; }
    public void setBudgetMois(Integer budgetMois) { this.budgetMois = budgetMois; }

    public Integer getBudgetAnnee() { return budgetAnnee; }
    public void setBudgetAnnee(Integer budgetAnnee) { this.budgetAnnee = budgetAnnee; }
}