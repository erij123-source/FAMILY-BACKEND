package tn.educanet.backend.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tn.educanet.backend.persistence.Charge;

@Repository
public interface ChargeDao extends JpaRepository<Charge, Integer> {

    List<Charge> findByMembreId(int membreId);
    List<Charge> findByCompteId(int compteId);
    List<Charge> findByType(Charge.Type type);
    void deleteByMembre_Id(int membreId);

    // ← NOUVEAU : filtre par membre + mois + année via chargeDate
    @Query("SELECT c FROM Charge c WHERE c.membre.id = :membreId " +
           "AND MONTH(c.chargeDate) = :mois AND YEAR(c.chargeDate) = :annee")
    List<Charge> findByMembreIdAndMoisAndAnnee(
        @Param("membreId") int membreId,
        @Param("mois")     int mois,
        @Param("annee")    int annee);

    // Filtre par paymentDate (date de paiement réelle). Pour les charges
    // anciennes ou importées sans paymentDate, on retombe sur chargeDate
    // pour rester rétro-compatible et ne perdre aucune ligne dans les totaux.
    @Query("SELECT c FROM Charge c WHERE c.membre.id = :membreId " +
           "AND ((c.paymentDate IS NOT NULL " +
           "      AND MONTH(c.paymentDate) = :mois " +
           "      AND YEAR(c.paymentDate) = :annee) " +
           "  OR (c.paymentDate IS NULL " +
           "      AND MONTH(c.chargeDate) = :mois " +
           "      AND YEAR(c.chargeDate) = :annee))")
    List<Charge> findByMembreIdAndPaymentMoisAndAnnee(
        @Param("membreId") int membreId,
        @Param("mois")     int mois,
        @Param("annee")    int annee);
}