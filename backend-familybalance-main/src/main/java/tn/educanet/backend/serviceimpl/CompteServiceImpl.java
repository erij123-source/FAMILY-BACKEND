package tn.educanet.backend.serviceimpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import tn.educanet.backend.dao.BudgetHistoriqueDao;
import tn.educanet.backend.dao.ChargeDao;
import tn.educanet.backend.dao.CompteDao;
import tn.educanet.backend.dao.FactureDao;
import tn.educanet.backend.dao.MembreDao;
import tn.educanet.backend.dao.RevenuDao;
import tn.educanet.backend.persistence.BudgetHistorique;
import tn.educanet.backend.persistence.Compte;
import tn.educanet.backend.persistence.Revenu;
import tn.educanet.backend.service.CompteService;
import tn.educanet.backend.service.RevenuService;
import tn.educanet.backend.service.ChargeService;

@Service
public class CompteServiceImpl implements CompteService {

    @Resource
    private CompteDao compteDao;

    @Resource
    private RevenuDao revenuDao;

    @Resource
    private MembreDao membreDao;

    @Resource
    private RevenuService revenuService;

    @Resource
    private ChargeService chargeService;

    @Resource
    private ChargeDao chargeDao;

    @Resource
    private FactureDao factureDao;

    @Resource
    private BudgetHistoriqueDao budgetHistoriqueDao;

    @Override
    public Compte save(Compte compte) {
        return compteDao.save(compte);
    }

    @Override
    public Optional<Compte> findById(int id) {
        return compteDao.findById(id);
    }

    @Override
    public Optional<Compte> findByFamilleId(int familleId) {
        return compteDao.findByFamilleId(familleId);
    }

    @Override
    public List<Compte> findAll() {
        return compteDao.findAll();
    }

    @Override
    public void deleteById(int id) {
        compteDao.deleteById(id);
    }

    @Override
    public double getTotalRevenu(int familleId) {
        return compteDao.findByFamilleId(familleId)
            .map(compte -> revenuDao.findByCompteId(compte.getId())
                .stream()
                .mapToDouble(r -> r.getAmount())
                .sum())
            .orElse(0.0);
    }

    @Override
    public double getTotalBudgetDistributed(int familleId) {
        return membreDao.findByFamilleId(familleId)
            .stream()
            .mapToDouble(m -> m.getBudget())
            .sum();
    }

    // ── FIX #8 : filtrage par mois/année sur les revenus de type DISTRIBUTION_BUDGET ──
    @Override
    public double getTotalBudgetDistributed(int familleId, Integer month, Integer year) {

        // Si month/year non fournis → fallback sur l'ancienne logique (somme des budgets membres)
        if (month == null || year == null) {
            return getTotalBudgetDistributed(familleId);
        }

        // Récupérer le compte de la famille
        Compte compte = compteDao.findByFamilleId(familleId).orElse(null);
        if (compte == null) return 0.0;

        // Filtrer les revenus de type DISTRIBUTION_BUDGET du mois/année demandés
        List<Revenu> revenus = revenuDao.findByCompteId(compte.getId());

        return revenus.stream()
            .filter(r -> "DISTRIBUTION_BUDGET".equals(r.getType()))
            .filter(r -> {
                // Priorité aux champs explicites mois/annee (FIX #3)
                if (r.getMois() != null && r.getAnnee() != null) {
                    return r.getMois().equals(month) && r.getAnnee().equals(year);
                }
                // Fallback sur perceptionDate
                LocalDate date = r.getPerceptionDate();
                if (date != null) {
                    return date.getMonthValue() == month && date.getYear() == year;
                }
                return false;
            })
            .mapToDouble(r -> r.getAmount())
            .sum();
    }

    @Override
    public double calculateRemainingBalance(int familleId) {
        double totalRevenu = getTotalRevenu(familleId);
        double totalBudget = getTotalBudgetDistributed(familleId);
        double remaining   = totalRevenu - totalBudget;

        compteDao.findByFamilleId(familleId).ifPresent(compte -> {
            compte.setRemainingBalance(remaining);
            compte.setLastUpdate(java.time.LocalDateTime.now());
            compteDao.save(compte);
        });

        return remaining;
    }

    @Override
    public double getTotalBudgetDistributedThisMonth(int familleId) {
        // Utilise la nouvelle méthode avec le mois courant
        LocalDate now = LocalDate.now();
        return getTotalBudgetDistributed(familleId, now.getMonthValue(), now.getYear());
    }

    @Override
    public double calculateRemainingBalanceThisMonth(int familleId) {
        Compte compte = compteDao.findByFamilleId(familleId).orElseThrow();
        double revenusMois = revenuService.getTotalByCompteThisMonth(compte.getId());
        double chargesMois = chargeService.getTotalByCompteThisMonth(compte.getId());
        return Math.max(0, revenusMois - chargesMois);
    }

    @Override
    public double getTotalCharges(int familleId) {
        return compteDao.findByFamilleId(familleId)
                .map(compte -> chargeDao.findByCompteId(compte.getId())
                        .stream()
                        .mapToDouble(c -> c.getAmount())
                        .sum())
                .orElse(0.0);
    }

    @Override
    public double getTotalFactures(int familleId) {
        return compteDao.findByFamilleId(familleId)
                .map(compte -> factureDao.findByCompteId(compte.getId())
                        .stream()
                        .mapToDouble(f -> f.getAmount())
                        .sum())
                .orElse(0.0);
    }

    // ── Report mensuel du revenu non distribué (famille) ────────────────────
    @Override
    public double getTotalBudgetDistributedForMonth(int familleId, int month, int year) {
        // Source fiable = budget_historique (alimenté par AdminServiceImpl.setBudget).
        // NB : ne PAS utiliser getTotalBudgetDistributed(familleId, month, year) qui
        // filtre des revenus typés DISTRIBUTION_BUDGET jamais écrits → renverrait 0.
        return budgetHistoriqueDao.findByMembreFamilleIdAndMoisAndAnnee(familleId, month, year)
                .stream()
                .mapToDouble(BudgetHistorique::getBudget)
                .sum();
    }

    @Override
    public double getFamilleRevenuCarryover(int familleId) {
        Compte compte = compteDao.findByFamilleId(familleId).orElse(null);
        if (compte == null) return 0.0;
        LocalDate now = LocalDate.now();
        // Fast-path : report déjà figé pour le mois courant lors du reset.
        if (compte.getCarryoverMois() != null && compte.getCarryoverAnnee() != null
                && compte.getCarryoverMois() == now.getMonthValue()
                && compte.getCarryoverAnnee() == now.getYear()) {
            return compte.getRevenuCarryover() == null ? 0.0 : compte.getRevenuCarryover();
        }
        // Sinon (reset pas encore exécuté ce mois-ci) : calcul à la volée sur le
        // mois précédent — correct même avant le premier reset, et auto-réparant.
        LocalDate prev = now.minusMonths(1);
        int pm = prev.getMonthValue();
        int py = prev.getYear();
        double prevRevenu    = revenuService.getTotalByCompteForMonth(compte.getId(), pm, py);
        double prevDistribue = getTotalBudgetDistributedForMonth(familleId, pm, py);
        return Math.max(0.0, prevRevenu - prevDistribue);
    }

    @Override
    public double getBudgetRestant(int familleId) {
        Compte compte = compteDao.findByFamilleId(familleId).orElse(null);
        if (compte == null) return 0.0;
        LocalDate now = LocalDate.now();
        double carryover = getFamilleRevenuCarryover(familleId);
        double revenuMois = revenuService.getTotalByCompteThisMonth(compte.getId());
        double distribueMois = getTotalBudgetDistributedForMonth(familleId, now.getMonthValue(), now.getYear());
        return Math.max(0.0, carryover + revenuMois - distribueMois);
    }
}