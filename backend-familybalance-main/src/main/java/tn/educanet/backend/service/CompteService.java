package tn.educanet.backend.service;

import java.util.List;
import java.util.Optional;

import tn.educanet.backend.persistence.Compte;

public interface CompteService {

    Compte save(Compte compte);
    Optional<Compte> findById(int id);
    Optional<Compte> findByFamilleId(int familleId);
    List<Compte> findAll();
    void deleteById(int id);

    /** totalRevenu - totalBudgetDistribué */
    double calculateRemainingBalance(int familleId);

    /** totalRevenu de la famille */
    double getTotalRevenu(int familleId);

    /** totalBudget distribué à tous les membres (tous mois) */
    double getTotalBudgetDistributed(int familleId);

    // ── FIX #8 : version filtrée par mois/année (optionnels) ───────────────
    /** totalBudget distribué filtré par mois et année si fournis, sinon tous mois */
    double getTotalBudgetDistributed(int familleId, Integer month, Integer year);

    double getTotalBudgetDistributedThisMonth(int familleId);
    double calculateRemainingBalanceThisMonth(int familleId);

    /** Budget distribué aux membres pour un mois/année donnés, lu depuis
     *  budget_historique (source fiable, contrairement aux revenus typés
     *  DISTRIBUTION_BUDGET qui ne sont jamais écrits par setBudget). */
    double getTotalBudgetDistributedForMonth(int familleId, int month, int year);

    /** Report de revenu non distribué du mois précédent (fast-path stocké sur
     *  le Compte s'il est estampillé au mois courant, sinon calcul à la volée). */
    double getFamilleRevenuCarryover(int familleId);

    /** Budget restant glissant = report mois précédent + revenus du mois courant
     *  − budgets distribués ce mois. C'est la valeur affichée dans la carte
     *  "Budget restant" (le revenu, lui, repart de 0 chaque mois). */
    double getBudgetRestant(int familleId);

    /** Sum of all charges for the family (all time). Used by mobile app. */
    double getTotalCharges(int familleId);

    /** Sum of all factures for the family (all time). Used by mobile app. */
    double getTotalFactures(int familleId);
}