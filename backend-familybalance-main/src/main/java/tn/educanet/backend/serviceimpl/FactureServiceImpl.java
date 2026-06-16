package tn.educanet.backend.serviceimpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import tn.educanet.backend.dao.FactureDao;
import tn.educanet.backend.persistence.Facture;
import tn.educanet.backend.service.FactureService;
import tn.educanet.backend.service.MembreService;

@Service
public class FactureServiceImpl implements FactureService {

    @Resource
    private FactureDao factureDao;

    @Resource
    private MembreService membreService;

    @Override
    public Optional<Facture> findById(int id) {
        return factureDao.findById(id);
    }

    @Override
    public List<Facture> findAll() {
        return factureDao.findAll();
    }

    @Override
    public List<Facture> findByMembreId(int membreId) {
        return factureDao.findByMembreId(membreId);
    }

    @Override
    public List<Facture> findByCompteId(int compteId) {
        return factureDao.findByCompteId(compteId);
    }

    @Override
    public List<Facture> findByStatut(Facture.Statut statut) {
        return factureDao.findByStatut(statut);
    }

    @Override
    public Facture add(Facture facture) {
        if (facture.getMembre() != null && facture.getMembre().getId() > 0) {
            membreService.ensureCanSpend(facture.getMembre().getId());
        }
        if (facture.getStatut() == Facture.Statut.NON_PAYEE
                && facture.getDueDate() != null
                && facture.getDueDate().isBefore(LocalDate.now())) {
            facture.setStatut(Facture.Statut.EN_RETARD);
        }
        if (facture.getStatut() == Facture.Statut.PAYEE
                && facture.getDatePaiement() == null) {
            facture.setDatePaiement(LocalDate.now());
        }
        return factureDao.save(facture);
    }

    @Override
    public Facture edit(int id, Facture facture) {
        facture.setId(id);
        // Recalculer EN_RETARD si besoin
        if (facture.getStatut() == Facture.Statut.NON_PAYEE
                && facture.getDueDate() != null
                && facture.getDueDate().isBefore(LocalDate.now())) {
            facture.setStatut(Facture.Statut.EN_RETARD);
        }
        // ✅ Si éditée et passée à PAYEE → enregistrer datePaiement = aujourd'hui
        if (facture.getStatut() == Facture.Statut.PAYEE
                && facture.getDatePaiement() == null) {
            facture.setDatePaiement(LocalDate.now());
        }
        return factureDao.save(facture);
    }

    @Override
    public void delete(int id) {
        factureDao.deleteById(id);
    }

    @Override
    public Facture upload(int id, String fileUrl) {
        return upload(id, fileUrl, null);
    }

    @Override
    public Facture upload(int id, String fileUrl, String fileName) {
        return factureDao.findById(id).map(facture -> {
            facture.setFileUrl(fileUrl);
            if (fileName != null) {
                facture.setFileName(fileName);
            }
            return factureDao.save(facture);
        }).orElseThrow(() -> new RuntimeException("Facture not found"));
    }

    @Override
    public void markAsPaid(int id) {
        factureDao.findById(id).ifPresent(facture -> {
            facture.setStatut(Facture.Statut.PAYEE);
            facture.setDatePaiement(LocalDate.now()); // ✅ enregistre la date réelle du paiement
            factureDao.save(facture);
        });
    }

    /**
     * Met à jour en base toutes les factures NON_PAYEE dont la dueDate
     * est dépassée → EN_RETARD.
     */
    @Override
    public int updateOverdueFactures() {
        List<Facture> overdue = factureDao.findByStatutAndDueDateBefore(
                Facture.Statut.NON_PAYEE, LocalDate.now());
        overdue.forEach(f -> f.setStatut(Facture.Statut.EN_RETARD));
        factureDao.saveAll(overdue);
        return overdue.size();
    }

    // ✅ CORRIGÉ — filtre sur datePaiement (date réelle du paiement)
    @Override
    public double getTotalByMembreThisMonth(int membreId) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear  = LocalDate.now().getYear();
        return factureDao.findByMembreId(membreId)
                .stream()
                .filter(f -> f.getStatut() == Facture.Statut.PAYEE)
                .filter(f -> f.getDatePaiement() != null
                        && f.getDatePaiement().getMonthValue() == currentMonth
                        && f.getDatePaiement().getYear() == currentYear)
                .mapToDouble(Facture::getAmount)
                .sum();
    }

    // ✅ CORRIGÉ — filtre sur datePaiement
    @Override
    public double getTotalByMembreThisYear(int membreId) {
        int currentYear = LocalDate.now().getYear();
        return factureDao.findByMembreId(membreId)
                .stream()
                .filter(f -> f.getStatut() == Facture.Statut.PAYEE)
                .filter(f -> f.getDatePaiement() != null
                        && f.getDatePaiement().getYear() == currentYear)
                .mapToDouble(Facture::getAmount)
                .sum();
    }

    // ✅ CORRIGÉ — filtre sur datePaiement (au lieu de dueDate)
    @Override
    public double getTotalByCompteThisMonth(int compteId) {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear  = LocalDate.now().getYear();
        return factureDao.findByCompteId(compteId)
                .stream()
                .filter(f -> f.getStatut() == Facture.Statut.PAYEE)
                .filter(f -> f.getDatePaiement() != null
                        && f.getDatePaiement().getMonthValue() == currentMonth
                        && f.getDatePaiement().getYear() == currentYear)
                .mapToDouble(Facture::getAmount)
                .sum();
    }

    @Override
    public long countByFamille(int familleId) {
        return factureDao.countByCompte_Famille_Id(familleId);
    }
}