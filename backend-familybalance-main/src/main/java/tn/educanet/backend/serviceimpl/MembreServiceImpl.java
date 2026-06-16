package tn.educanet.backend.serviceimpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.Resource;
import tn.educanet.backend.dao.BudgetHistoriqueDao;
import tn.educanet.backend.dao.ChargeDao;
import tn.educanet.backend.dao.FactureDao;
import tn.educanet.backend.dao.FamilleDao;
import tn.educanet.backend.dao.MembreDao;
import tn.educanet.backend.dao.NotificationDao;
import tn.educanet.backend.dao.RevenuDao;
import tn.educanet.backend.dao.UserDao;
import tn.educanet.backend.dto.BudgetSummaryDTO;
import tn.educanet.backend.persistence.BudgetHistorique;
import tn.educanet.backend.persistence.Charge;
import tn.educanet.backend.persistence.Facture;
import tn.educanet.backend.persistence.Famille;
import tn.educanet.backend.persistence.Membre;
import tn.educanet.backend.persistence.Notification;
import tn.educanet.backend.persistence.User;
import tn.educanet.backend.service.MembreService;

@Service
public class MembreServiceImpl implements MembreService {

    @Resource private MembreDao           membreDao;
    @Resource private UserDao             userDao;
    @Resource private FamilleDao          familleDao;
    @Resource private ChargeDao           chargeDao;
    @Resource private FactureDao          factureDao;
    @Resource private BudgetHistoriqueDao budgetHistoriqueDao;
    @Resource private RevenuDao           revenuDao;
    @Resource private NotificationDao     notificationDao;

    @Override
    public Membre save(Membre membre) { return membreDao.save(membre); }

    @Override
    public Optional<Membre> findById(int id) { return membreDao.findById(id); }

    @Override
    public List<Membre> findAll() { return membreDao.findAll(); }

    @Override
    public List<Membre> findByFamilleId(int familleId) {
        return membreDao.findByFamilleId(familleId);
    }

    @Override
    public Optional<Membre> findByUserIdAndFamilleId(int userId, int familleId) {
        return membreDao.findByUserIdAndFamilleId(userId, familleId);
    }

    @Override
    public Optional<Membre> findByUserId(int userId) {
        return membreDao.findFirstByUser_Id(userId);
    }

    /**
     * Hard-delete a member and all financial rows attached to them
     * (charges / factures / revenus). Notifications targeting the member
     * are kept but their FK is nulled out so history is preserved.
     * The family ADMINISTRATEUR (chef de famille) is protected.
     */
    @Override
    @Transactional
    public void deleteById(int id) {
        Membre membre = membreDao.findById(id).orElse(null);
        if (membre == null) return;
        if (membre.getRole() == Membre.Role.ADMINISTRATEUR) {
            throw new RuntimeException("Le chef de famille ne peut pas être supprimé.");
        }
        chargeDao.deleteAll(chargeDao.findByMembreId(id));
        factureDao.deleteAll(factureDao.findByMembreId(id));
        revenuDao.deleteAll(revenuDao.findByMembreId(id));
        for (Notification n : notificationDao.findByMembreIdOrderByCreatedAtDesc(id)) {
            n.setMembre(null);
            notificationDao.save(n);
        }
        membreDao.deleteById(id);
    }

    @Override
    public void joinGroup(int userId, int familleId) {
        User user = userDao.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Famille famille = familleDao.findById(familleId)
            .orElseThrow(() -> new RuntimeException("Famille not found"));
        Membre membre = new Membre();
        membre.setUser(user);
        membre.setFamille(famille);
        membre.setRole(Membre.Role.CONSULTANT);
        membre.setActive(true);
        membreDao.save(membre);
    }

    @Override
    public void leaveGroup(int membreId) {
        membreDao.findById(membreId).ifPresent(m -> {
            m.setActive(false);
            membreDao.save(m);
        });
    }

    @Override
    public String getRole(int membreId) {
        return membreDao.findById(membreId)
            .map(m -> m.getRole().name())
            .orElseThrow(() -> new RuntimeException("Membre not found"));
    }

    @Override
    public void changeRole(int membreId, Membre.Role role) {
        membreDao.findById(membreId).ifPresent(m -> {
            m.setRole(role);
            membreDao.save(m);
        });
    }

    @Override
    public double calculateRemainingBudget(int membreId, double expenses) {
        return membreDao.findById(membreId).map(m -> {
            double remaining = m.getBudget() + m.getCarryoverBudget() - expenses;
            m.setRemainingBudget(Math.max(0, remaining));
            membreDao.save(m);
            return m.getRemainingBudget();
        }).orElseThrow(() -> new RuntimeException("Membre not found"));
    }

    @Override
    public void applyCarryover(int membreId) {
        LocalDate today = LocalDate.now();
        if (today.getDayOfMonth() != 1) {
            throw new RuntimeException(
                "Le carryover s'applique uniquement le 1er du mois (aujourd'hui : " + today + ")."
            );
        }
        membreDao.findById(membreId).ifPresent(m -> {
            double remaining = m.getRemainingBudget();
            m.setCarryoverBudget(remaining > 0 ? remaining : 0.0);
            m.setCarryoverMois(today.getMonthValue());
            m.setCarryoverAnnee(today.getYear());
            m.setBudget(0.0);
            membreDao.save(m);
        });
    }

    // ── getBudgetSnapshot — lit depuis budget_historique ──────────────────
    @Override
    public BudgetSnapshot getBudgetSnapshot(int membreId, int mois, int annee) {
        Membre m = membreDao.findById(membreId)
            .orElseThrow(() -> new RuntimeException("Membre not found: " + membreId));

        // 1. Lire budget et carryover depuis budget_historique
        double budget    = 0.0;
        double carryover = 0.0;

        Optional<BudgetHistorique> historiqueOpt =
            budgetHistoriqueDao.findByMembreIdAndMoisAndAnnee(membreId, mois, annee);

        if (historiqueOpt.isPresent()) {
            // Données historiques trouvées pour ce mois exact
            budget    = historiqueOpt.get().getBudget();
            carryover = historiqueOpt.get().getCarryover();
        } else {
            // Fallback : si c'est le mois courant, lire depuis le membre directement
            int moisCourant  = LocalDate.now().getMonthValue();
            int anneeCourante = LocalDate.now().getYear();
            if (mois == moisCourant && annee == anneeCourante) {
                if (m.getBudgetMois()  != null && m.getBudgetAnnee()  != null
                        && m.getBudgetMois().equals(mois)
                        && m.getBudgetAnnee().equals(annee)) {
                    budget = m.getBudget();
                }
                if (m.getCarryoverMois()  != null && m.getCarryoverAnnee()  != null
                        && m.getCarryoverMois().equals(mois)
                        && m.getCarryoverAnnee().equals(annee)) {
                    carryover = m.getCarryoverBudget();
                }
            }
        }

        // 2. Dépenses réelles filtrées par DATE DE PAIEMENT (cash sorti
        // effectivement ce mois-là). Une charge "pour août" payée en mai
        // pèse sur le budget de mai, pas d'août — c'est ce qui détermine
        // le reste à vivre vu par l'utilisateur.
        double charges = chargeDao
            .findByMembreIdAndPaymentMoisAndAnnee(membreId, mois, annee)
            .stream().mapToDouble(c -> c.getAmount()).sum();

        double factures = factureDao
            .findByMembreIdAndPaymentMoisAndAnnee(membreId, mois, annee)
            .stream().mapToDouble(f -> f.getAmount()).sum();

        double spent = charges + factures;
        double reste = Math.max(0.0, budget + carryover - spent);

        String firstName = m.getUser() != null && m.getUser().getFirstName() != null
            ? m.getUser().getFirstName() : "";
        String lastName  = m.getUser() != null && m.getUser().getLastName()  != null
            ? m.getUser().getLastName()  : "";

        BudgetSnapshot snap = new BudgetSnapshot();
        snap.membreId   = m.getId();
        snap.nomComplet = (firstName + " " + lastName).trim();
        snap.budget     = budget;
        snap.carryover  = carryover;
        snap.spent      = spent;
        snap.reste      = reste;
        snap.mois       = mois;
        snap.annee      = annee;
        return snap;
    }
    @Override
    public List<BudgetHistoriqueItem> getBudgetHistoriqueParFamille(
            int familleId, int month, int year) {

        // 1. Lignes déjà persistées dans budget_historique pour ce mois exact.
        List<BudgetHistorique> historiques =
            budgetHistoriqueDao.findByMembreFamilleIdAndMoisAndAnnee(familleId, month, year);

        Map<Integer, BudgetHistoriqueItem> byMembre = new LinkedHashMap<>();
        for (BudgetHistorique h : historiques) {
            Membre m = h.getMembre();
            BudgetHistoriqueItem item = new BudgetHistoriqueItem();
            item.membreId   = m.getId();
            item.nomComplet = nomComplet(m);
            item.budget     = h.getBudget();
            item.carryover  = h.getCarryover();
            item.mois       = h.getMois();
            item.annee      = h.getAnnee();
            byMembre.put(m.getId(), item);
        }

        // 2. Fallback mois courant — identique à getBudgetSnapshot : un budget
        //    distribué ce mois-ci ne vit que sur l'entité Membre tant que le
        //    rollover mensuel ne l'a pas écrit dans budget_historique. Sans ce
        //    fallback, le donut "Répartition par Membre" reste vide tout le mois
        //    alors que les budgets sont clairement alloués (cf. la 2ᵉ carte qui,
        //    elle, a déjà ce fallback).
        LocalDate now = LocalDate.now();
        if (month == now.getMonthValue() && year == now.getYear()) {
            for (Membre m : membreDao.findByFamilleId(familleId)) {
                if (byMembre.containsKey(m.getId())) continue; // historique gagne

                double budget = 0.0;
                if (m.getBudgetMois() != null && m.getBudgetAnnee() != null
                        && m.getBudgetMois().equals(month)
                        && m.getBudgetAnnee().equals(year)) {
                    budget = m.getBudget();
                }
                if (budget <= 0.0) continue; // le donut n'affiche que les budgets distribués

                double carryover = 0.0;
                if (m.getCarryoverMois() != null && m.getCarryoverAnnee() != null
                        && m.getCarryoverMois().equals(month)
                        && m.getCarryoverAnnee().equals(year)) {
                    carryover = m.getCarryoverBudget();
                }

                BudgetHistoriqueItem item = new BudgetHistoriqueItem();
                item.membreId   = m.getId();
                item.nomComplet = nomComplet(m);
                item.budget     = budget;
                item.carryover  = carryover;
                item.mois       = month;
                item.annee      = year;
                byMembre.put(m.getId(), item);
            }
        }

        return new ArrayList<>(byMembre.values());
    }

    /** Prénom + nom de l'utilisateur lié au membre, vide si absent. */
    private String nomComplet(Membre m) {
        String firstName = m.getUser() != null && m.getUser().getFirstName() != null
            ? m.getUser().getFirstName() : "";
        String lastName  = m.getUser() != null && m.getUser().getLastName()  != null
            ? m.getUser().getLastName()  : "";
        return (firstName + " " + lastName).trim();
    }

    // ── Mobile (React Native) endpoints ────────────────────────────────────

    @Override
    @Transactional
    public Membre setBudget(int membreId, double budget) {
        Membre membre = membreDao.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre not found"));

        LocalDate now = LocalDate.now();
        int moisCourant   = now.getMonthValue();
        int anneeCourante = now.getYear();

        // ── 1. Calcul du RESTE À VIVRE AVANT modification ─────────────────
        // Référence : BudgetSummary affiché à l'utilisateur juste avant.
        //   ancien_total = ancien_budget + ancien_carryover_valide
        //   ancien_reste = ancien_total − dépenses_du_mois
        // Ce reste devient le NOUVEAU carryoverBudget (= "report du mois
        // dernier" affiché en sous-pill partout sur le dashboard).
        double depenses =
                sumChargesForMonth(membreId, moisCourant, anneeCourante)
                + sumFacturesPayeeForMonth(membreId, moisCourant, anneeCourante);

        double ancienBudget = membre.getBudget();
        double ancienCarryoverValide = 0.0;
        Integer cMois  = membre.getCarryoverMois();
        Integer cAnnee = membre.getCarryoverAnnee();
        if (cMois != null && cMois.intValue() == moisCourant
                && cAnnee != null && cAnnee.intValue() == anneeCourante) {
            ancienCarryoverValide = membre.getCarryoverBudget();
        }
        double ancienResteAVivre = Math.max(0.0,
                (ancienBudget + ancienCarryoverValide) - depenses);

        // ── 2. Persistance : budget = saisie, carryoverBudget = ancien reste
        membre.setBudget(budget);
        membre.setBudgetMois(moisCourant);
        membre.setBudgetAnnee(anneeCourante);
        membre.setCarryoverBudget(ancienResteAVivre);
        membre.setCarryoverMois(moisCourant);
        membre.setCarryoverAnnee(anneeCourante);

        // ── 3. Reste à vivre recalculé sur le nouveau total ─────────────
        double nouveauTotal     = budget + ancienResteAVivre;
        double nouveauReste     = Math.max(0.0, nouveauTotal - depenses);
        membre.setRemainingBudget(nouveauReste);

        return membreDao.save(membre);
    }

    @Override
    @Transactional
    public BudgetSummaryDTO getBudgetSummary(int membreId) {
        Membre membre = membreDao.findById(membreId)
                .orElseThrow(() -> new RuntimeException("Membre not found"));

        LocalDate now = LocalDate.now();
        int moisCourant   = now.getMonthValue();
        int anneeCourante = now.getYear();

        double depensesMois =
                sumChargesForMonth(membreId, moisCourant, anneeCourante)
                + sumFacturesPayeeForMonth(membreId, moisCourant, anneeCourante);

        // ── Budget gestionnaire (column `budget`) : montant alloué ce mois.
        double base = membre.getBudget();

        // ── Report du mois dernier : lu directement depuis la colonne
        // persistante `carryover_budget` (set par resetBudgetsForNewMonth
        // au passage au mois suivant). On valide que le carryover stocké
        // appartient bien au mois courant — sinon c'est une valeur stale
        // d'un mois précédent et on l'ignore (renvoie 0).
        double carryover = 0.0;
        Integer cMois  = membre.getCarryoverMois();
        Integer cAnnee = membre.getCarryoverAnnee();
        boolean carryValide = (cMois  != null && cMois.intValue()  == moisCourant)
                           && (cAnnee != null && cAnnee.intValue() == anneeCourante);
        if (carryValide) {
            carryover = membre.getCarryoverBudget();
        }

        double effective  = base + carryover;
        double rawRest    = effective - depensesMois;
        double restAvivre = Math.max(0, rawRest);
        boolean exhausted = effective > 0 && rawRest <= 0;

        // On met à jour uniquement remainingBudget — la valeur carryover
        // n'est PAS recalculée ici : elle est figée par resetBudgetsForNewMonth.
        membre.setRemainingBudget(rawRest);
        membreDao.save(membre);

        return new BudgetSummaryDTO(base, carryover, effective, depensesMois, restAvivre, exhausted);
    }

    @Override
    public void ensureCanSpend(int membreId) {
        BudgetSummaryDTO summary = getBudgetSummary(membreId);
        if (summary.exhausted) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Budget épuisé pour ce mois : impossible d'ajouter une nouvelle opération.");
        }
    }

    private double sumChargesForMonth(int membreId, int month, int year) {
        return chargeDao.findByMembreId(membreId).stream()
                .filter(c -> c.getChargeDate() != null
                        && c.getChargeDate().getMonthValue() == month
                        && c.getChargeDate().getYear() == year)
                .mapToDouble(Charge::getAmount)
                .sum();
    }

    private double sumFacturesPayeeForMonth(int membreId, int month, int year) {
        return factureDao.findByMembreId(membreId).stream()
                .filter(f -> f.getStatut() == Facture.Statut.PAYEE)
                .filter(f -> f.getIssueDate() != null
                        && f.getIssueDate().getMonthValue() == month
                        && f.getIssueDate().getYear() == year)
                .mapToDouble(Facture::getAmount)
                .sum();
    }
}