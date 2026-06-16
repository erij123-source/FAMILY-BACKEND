package tn.educanet.backend.service;

import java.util.List;
import java.util.Optional;
import tn.educanet.backend.dto.BudgetSummaryDTO;
import tn.educanet.backend.persistence.Membre;

public interface MembreService {

    // ── DTO snapshot mensuel ────────────────────────────────────────────────
    class BudgetSnapshot {
        public int    membreId;
        public String nomComplet;
        public double budget;
        public double carryover;
        public double spent;
        public double reste;
        public int    mois;
        public int    annee;
    }
    class BudgetHistoriqueItem {
        public int    membreId;
        public String nomComplet;
        public double budget;
        public double carryover;
        public int    mois;
        public int    annee;
    }

    List<BudgetHistoriqueItem> getBudgetHistoriqueParFamille(int familleId, int month, int year);

    // ── méthodes existantes (inchangées) ───────────────────────────────────
    Membre save(Membre membre);
    Optional<Membre> findById(int id);
    List<Membre> findAll();
    List<Membre> findByFamilleId(int familleId);
    Optional<Membre> findByUserIdAndFamilleId(int userId, int familleId);
    Optional<Membre> findByUserId(int userId);
    void deleteById(int id);
    void joinGroup(int userId, int familleId);
    void leaveGroup(int membreId);
    String getRole(int membreId);
    void changeRole(int membreId, Membre.Role role);
    double calculateRemainingBudget(int membreId, double expenses);
    void applyCarryover(int membreId);

    // ← NOUVEAU
    BudgetSnapshot getBudgetSnapshot(int membreId, int mois, int annee);

    // ── Mobile (React Native) endpoints ────────────────────────────────────
    /** Sets the monthly budget for a member and resets remainingBudget = budget.
     *  Used by the mobile admin UI to provision a member's allowance.        */
    Membre setBudget(int membreId, double budget);

    /** Live computed snapshot of the member's current monthly budget         */
    BudgetSummaryDTO getBudgetSummary(int membreId);

    /** Spend-guard: throws 403 if the member's budget is exhausted.          */
    void ensureCanSpend(int membreId);
}