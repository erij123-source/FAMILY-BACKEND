package tn.educanet.backend.service;

import java.util.List;
import java.util.Optional;

import tn.educanet.backend.persistence.Facture;

public interface FactureService {

    Optional<Facture> findById(int id);
    List<Facture> findAll();
    List<Facture> findByMembreId(int membreId);
    List<Facture> findByCompteId(int compteId);
    List<Facture> findByStatut(Facture.Statut statut);
    Facture add(Facture facture);
    Facture edit(int id, Facture facture);
    void delete(int id);
    Facture upload(int id, String fileUrl);
    /** Variante incluant le nom de fichier original (affichage UI). */
    Facture upload(int id, String fileUrl, String fileName);
    void markAsPaid(int id);

    int updateOverdueFactures();
    
    double getTotalByMembreThisMonth(int membreId);
    double getTotalByMembreThisYear(int membreId);
    double getTotalByCompteThisMonth(int compteId);

    /** Total factures count for a family (mobile dashboard). */
    long countByFamille(int familleId);
}