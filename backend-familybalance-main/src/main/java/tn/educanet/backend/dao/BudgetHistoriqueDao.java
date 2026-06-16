package tn.educanet.backend.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tn.educanet.backend.persistence.BudgetHistorique;

@Repository
public interface BudgetHistoriqueDao extends JpaRepository<BudgetHistorique, Integer> {

    // Trouver le snapshot d'un membre pour un mois/année précis
    Optional<BudgetHistorique> findByMembreIdAndMoisAndAnnee(int membreId, int mois, int annee);

    // Tous les snapshots d'un membre (pour afficher l'historique complet)
    List<BudgetHistorique> findByMembreId(int membreId);

    // Tous les snapshots d'une famille pour un mois/année (via membre)
    List<BudgetHistorique> findByMembreFamilleIdAndMoisAndAnnee(int familleId, int mois, int annee);
}