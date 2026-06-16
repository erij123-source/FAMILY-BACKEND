package tn.educanet.backend.serviceimpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import tn.educanet.backend.dao.AdminDao;
import tn.educanet.backend.dao.BudgetHistoriqueDao;
import tn.educanet.backend.dao.CompteDao;
import tn.educanet.backend.dao.FamilleDao;
import tn.educanet.backend.dao.MembreDao;
import tn.educanet.backend.dao.RevenuDao;
import tn.educanet.backend.dao.ChargeDao;
import tn.educanet.backend.dao.FactureDao;
import tn.educanet.backend.dao.UserDao;
import tn.educanet.backend.dao.RefreshTokenDao;
import tn.educanet.backend.persistence.Admin;
import tn.educanet.backend.persistence.BudgetHistorique;
import tn.educanet.backend.persistence.Famille;
import tn.educanet.backend.persistence.Membre;
import tn.educanet.backend.persistence.User;
import tn.educanet.backend.persistence.Compte;
import tn.educanet.backend.service.AdminService;
import tn.educanet.backend.service.CompteService;
import tn.educanet.backend.service.RevenuService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import jakarta.transaction.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource private RevenuDao           revenuDao;
    @Resource private ChargeDao           chargeDao;
    @Resource private FactureDao          factureDao;
    @Resource private UserDao             userDao;
    @Resource private RefreshTokenDao     refreshTokenDao;
    @Resource private AdminDao            adminDao;
    @Resource private FamilleDao          familleDao;
    @Resource private MembreDao           membreDao;
    @Resource private CompteDao           compteDao;
    @Resource private RevenuService       revenuService;
    @Resource private BudgetHistoriqueDao budgetHistoriqueDao;
    @Resource private CompteService       compteService;

    @Override
    public Admin save(Admin admin) { return adminDao.save(admin); }

    @Override
    public Optional<Admin> findById(int id) { return adminDao.findById(id); }

    @Override
    public Optional<Admin> findFirstByUser_Id(int userId) {
        return adminDao.findFirstByUser_Id(userId);
    }

    @Override
    public Optional<Admin> findFirstByFamille_Id(int familleId) {
        return adminDao.findFirstByFamille_Id(familleId);
    }

    @Override
    public List<Admin> findAll() { return adminDao.findAll(); }

    @Override
    public void deleteById(int id) { adminDao.deleteById(id); }

    @Override
    public Famille createFamily(String name, int creatorId) {
        Famille famille = new Famille();
        famille.setName(name);
        User creator = new User();
        creator.setId(creatorId);
        famille.setCreator(creator);
        Famille savedFamille = familleDao.save(famille);

        Admin admin = new Admin();
        admin.setUser(creator);
        admin.setFamille(savedFamille);
        adminDao.save(admin);

        Membre membre = new Membre();
        membre.setUser(creator);
        membre.setFamille(savedFamille);
        membre.setRole(Membre.Role.ADMINISTRATEUR);
        membreDao.save(membre);

        Compte compte = new Compte();
        compte.setFamille(savedFamille);
        compteDao.save(compte);

        return savedFamille;
    }

    @Override
    public void deleteFamily(int familleId) { familleDao.deleteById(familleId); }

    @Override
    public void inviteMember(int userId, int familleId) {
        Membre membre = new Membre();
        User user = new User();
        user.setId(userId);
        membre.setUser(user);
        Famille famille = new Famille();
        famille.setId(familleId);
        membre.setFamille(famille);
        membre.setRole(Membre.Role.CONSULTANT);
        membreDao.save(membre);
    }

    @Override
    @Transactional
    public void removeMember(int membreId) {
        Membre membre = membreDao.findById(membreId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Membre introuvable"));
        int userId = membre.getUser().getId();
        factureDao.deleteByMembre_Id(membreId);
        chargeDao.deleteByMembre_Id(membreId);
        revenuDao.deleteByMembre_Id(membreId);
        refreshTokenDao.deleteByUserId((long) userId);
        membreDao.deleteById(membreId);
        userDao.deleteById(userId);
    }

    @Override
    public void changeMemberRole(int membreId, Membre.Role role) {
        membreDao.findById(membreId).ifPresent(m -> {
            m.setRole(role);
            membreDao.save(m);
        });
    }

    @Override
    @Transactional
    public void setBudget(int membreId, double budget) {
        Membre membre = membreDao.findById(membreId)
            .orElseThrow(() -> new RuntimeException("Membre not found"));

        int familleId = membre.getFamille().getId();
        Compte compte = compteDao.findByFamilleId(familleId)
            .orElseThrow(() -> new RuntimeException("Compte not found"));

        double totalRevenus = revenuService.getTotalByCompteThisMonth(compte.getId());

        int mois  = LocalDate.now().getMonthValue();
        int annee = LocalDate.now().getYear();

        // ── Calcul du reste à vivre AVANT modification ───────────────────────
        // (= ancien budget − dépenses du mois). S'il est positif, il sera
        // automatiquement transféré dans `carryoverBudget` pour être ajouté
        // au nouveau budget — et affiché dans la sous-pill "report" du Dashboard.
        double chargesMois = chargeDao.findByMembreId(membreId)
            .stream()
            .filter(c -> c.getChargeDate() != null
                && c.getChargeDate().getMonthValue() == mois
                && c.getChargeDate().getYear()       == annee)
            .mapToDouble(c -> c.getAmount())
            .sum();

        // Filtre aligné sur MembreServiceImpl.sumFacturesPayeeForMonth :
        // PAYEE + issueDate du mois courant. Garantit que le calcul du
        // reste à vivre avant modification = la même valeur que celle
        // affichée par /membres/{id}/budget-summary (cohérence UI/DB).
        double facturesMois = factureDao.findByMembreId(membreId)
            .stream()
            .filter(f -> f.getStatut() == tn.educanet.backend.persistence.Facture.Statut.PAYEE)
            .filter(f -> f.getIssueDate() != null
                && f.getIssueDate().getMonthValue() == mois
                && f.getIssueDate().getYear()       == annee)
            .mapToDouble(f -> f.getAmount())
            .sum();

        // Budget effectif courant tel qu'affiché à l'utilisateur (cf.
        // MembreServiceImpl.getBudgetSummary : on lit `membre.getBudget()`
        // directement, sans contrôle de mois — le check budgetMois/Annee
        // créerait un écart entre la valeur que l'admin VOIT (ex: 1000)
        // et la valeur utilisée pour calculer le reste (0 si vieux mois).
        double ancienBudget = membre.getBudget();
        double ancienCarryoverValide = 0.0;
        if (membre.getCarryoverMois()  != null && membre.getCarryoverAnnee()  != null
                && membre.getCarryoverMois().equals(mois)
                && membre.getCarryoverAnnee().equals(annee)) {
            ancienCarryoverValide = membre.getCarryoverBudget();
        }
        double resteAVivreAvant = Math.max(0.0,
            (ancienBudget + ancienCarryoverValide) - chargesMois - facturesMois);

        // ── Validation plafond : la saisie + le reste auto-ajouté ne doivent
        //    pas dépasser le total des revenus du mois.
        double dejaDistribue = membreDao.findByFamilleId(familleId)
            .stream()
            .filter(m -> m.getId() != membreId)
            .filter(m -> m.getBudgetMois()  != null && m.getBudgetMois().equals(mois)
                      && m.getBudgetAnnee() != null && m.getBudgetAnnee().equals(annee))
            .mapToDouble(Membre::getBudget)
            .sum();

        // Le pool distribuable = report du mois précédent (revenu non distribué)
        // + revenus du mois courant. Doit rester EN PHASE avec le "budget restant"
        // affiché (getBudgetRestant) sinon l'UI proposerait un montant que le
        // backend refuse.
        double carryover = compteService.getFamilleRevenuCarryover(familleId);
        double disponible = (carryover + totalRevenus) - dejaDistribue;
        if (budget > disponible) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Il vous reste seulement " + String.format("%.2f", disponible) + " DT disponible."
            );
        }

        // ── Mettre à jour le membre ──────────────────────────────────────────
        // Budget = valeur saisie (NON fusionnée avec le reste).
        // CarryoverBudget = reste à vivre avant modification (affiché en
        // sous-pill "report du mois dernier" + utilisé pour le total réel).
        membre.setBudget(budget);
        membre.setBudgetMois(mois);
        membre.setBudgetAnnee(annee);
        membre.setCarryoverBudget(resteAVivreAvant);
        membre.setCarryoverMois(mois);
        membre.setCarryoverAnnee(annee);

        double budgetTotal = budget + resteAVivreAvant;
        double remaining   = Math.max(0, budgetTotal - chargesMois - facturesMois);
        membre.setRemainingBudget(remaining);
        membreDao.save(membre);

        // ── Sauvegarder/mettre à jour dans budget_historique ────────
        BudgetHistorique historique = budgetHistoriqueDao
            .findByMembreIdAndMoisAndAnnee(membreId, mois, annee)
            .orElse(new BudgetHistorique());

        historique.setMembre(membre);
        historique.setMois(mois);
        historique.setAnnee(annee);
        historique.setBudget(budget);
        historique.setCarryover(resteAVivreAvant);
        budgetHistoriqueDao.save(historique);
    }

    @Override
    @Transactional
    public void resetBudgetsForNewMonth() {
        List<Membre> membres = membreDao.findAll();

        int moisPrecedent   = LocalDate.now().minusMonths(1).getMonthValue();
        int anneePrecedente = LocalDate.now().minusMonths(1).getYear();
        int moisCourant     = LocalDate.now().getMonthValue();
        int anneeCourante   = LocalDate.now().getYear();

        for (Membre membre : membres) {

            // ── Idempotence : si le report de ce membre est DÉJÀ estampillé pour
            //    le mois courant, le reset a déjà tourné ce mois-ci (ou l'admin a
            //    déjà saisi un budget). On NE re-traite PAS, sinon un 2ᵉ passage
            //    remettrait le reste à vivre à 0 (ancienBudget/ancienCarry ne
            //    correspondraient plus au mois précédent). Protège le report
            //    "chaque membre garde son reste du mois dernier".
            if (membre.getCarryoverMois()  != null && membre.getCarryoverAnnee() != null
                    && membre.getCarryoverMois()  == moisCourant
                    && membre.getCarryoverAnnee() == anneeCourante) {
                continue;
            }

            double chargesMoisPrec = chargeDao.findByMembreId(membre.getId())
                .stream()
                .filter(c -> c.getChargeDate() != null
                    && c.getChargeDate().getMonthValue() == moisPrecedent
                    && c.getChargeDate().getYear()       == anneePrecedente)
                .mapToDouble(c -> c.getAmount())
                .sum();

            double facturesMoisPrec = factureDao.findByMembreId(membre.getId())
                .stream()
                .filter(f -> f.getStatut() == tn.educanet.backend.persistence.Facture.Statut.PAYEE
                    && f.getDatePaiement() != null
                    && f.getDatePaiement().getMonthValue() == moisPrecedent
                    && f.getDatePaiement().getYear()       == anneePrecedente)
                .mapToDouble(f -> f.getAmount())
                .sum();

            // Le `budget` et `carryoverBudget` actuels ne représentent une
            // réalité du mois précédent QUE s'ils ont été tagués pour ce mois.
            // Sinon (membre fraîchement créé, jamais de budget pour mois préc.) :
            // ancienBudget = 0 et ancienCarry = 0 — donc nouveauCarry = 0,
            // évitant le faux "+ X DT report du mois dernier" qui apparaissait
            // sur un compte sans historique réel.
            boolean budgetEtaitPourMoisPrec =
                    membre.getBudgetMois() != null && membre.getBudgetAnnee() != null
                 && membre.getBudgetMois().intValue()  == moisPrecedent
                 && membre.getBudgetAnnee().intValue() == anneePrecedente;
            boolean carryEtaitPourMoisPrec =
                    membre.getCarryoverMois() != null && membre.getCarryoverAnnee() != null
                 && membre.getCarryoverMois().intValue()  == moisPrecedent
                 && membre.getCarryoverAnnee().intValue() == anneePrecedente;

            double ancienBudget    = budgetEtaitPourMoisPrec ? membre.getBudget()           : 0.0;
            double ancienCarry     = carryEtaitPourMoisPrec  ? membre.getCarryoverBudget() : 0.0;
            double totalDepenses   = chargesMoisPrec + facturesMoisPrec;
            double budgetTotalPrec = ancienBudget + ancienCarry;
            double nouveauCarry    = Math.max(0, budgetTotalPrec - totalDepenses);

            membre.setBudget(0);
            membre.setCarryoverBudget(nouveauCarry);
            membre.setRemainingBudget(nouveauCarry);
            membre.setCarryoverMois(moisCourant);
            membre.setCarryoverAnnee(anneeCourante);
            membreDao.save(membre);
        }

        // ── Report FAMILLE : revenu non distribué du mois précédent ──────────
        // Analogue du report par membre, mais au niveau du compte famille :
        //   carryover = max(0, revenus(mois préc.) − budgets distribués(mois préc.))
        // Figé sur le Compte et estampillé au mois courant. La carte "Budget
        // restant" lira cette valeur ; le revenu affiché, lui, repart de 0.
        for (Compte compte : compteDao.findAll()) {
            // Idempotence : déjà figé pour le mois courant → ne pas recalculer.
            if (compte.getCarryoverMois()  != null && compte.getCarryoverAnnee() != null
                    && compte.getCarryoverMois()  == moisCourant
                    && compte.getCarryoverAnnee() == anneeCourante) {
                continue;
            }
            int fId = compte.getFamille().getId();
            double prevRevenu    = revenuService.getTotalByCompteForMonth(
                    compte.getId(), moisPrecedent, anneePrecedente);
            double prevDistribue = budgetHistoriqueDao
                    .findByMembreFamilleIdAndMoisAndAnnee(fId, moisPrecedent, anneePrecedente)
                    .stream()
                    .mapToDouble(BudgetHistorique::getBudget)
                    .sum();
            compte.setRevenuCarryover(Math.max(0.0, prevRevenu - prevDistribue));
            compte.setCarryoverMois(moisCourant);
            compte.setCarryoverAnnee(anneeCourante);
            compteDao.save(compte);
        }
    }
}