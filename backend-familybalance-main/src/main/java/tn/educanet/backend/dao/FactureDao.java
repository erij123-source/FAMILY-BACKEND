package tn.educanet.backend.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tn.educanet.backend.persistence.Facture;

public interface FactureDao extends JpaRepository<Facture, Integer> {

    List<Facture> findByMembreId(int membreId);
    List<Facture> findByCompteId(int compteId);
    List<Facture> findByStatut(Facture.Statut statut);
    List<Facture> findByStatutAndDueDateBefore(Facture.Statut statut, LocalDate date);
    void deleteByMembre_Id(int membreId);

    // ← NOUVEAU : filtre par membre + mois + année via issueDate
    @Query("SELECT f FROM Facture f WHERE f.membre.id = :membreId " +
           "AND MONTH(f.issueDate) = :mois AND YEAR(f.issueDate) = :annee")
    List<Facture> findByMembreIdAndMoisAndAnnee(
        @Param("membreId") int membreId,
        @Param("mois")     int mois,
        @Param("annee")    int annee);

    // Filtre par datePaiement (date réelle où l'argent sort). Seules les
    // factures effectivement payées ce mois-là entrent dans le calcul du
    // reste à vivre — une NON_PAYEE n'a pas encore consommé le budget.
    @Query("SELECT f FROM Facture f WHERE f.membre.id = :membreId " +
           "AND f.datePaiement IS NOT NULL " +
           "AND MONTH(f.datePaiement) = :mois " +
           "AND YEAR(f.datePaiement) = :annee")
    List<Facture> findByMembreIdAndPaymentMoisAndAnnee(
        @Param("membreId") int membreId,
        @Param("mois")     int mois,
        @Param("annee")    int annee);

    long countByCompte_Famille_Id(int familleId);
}