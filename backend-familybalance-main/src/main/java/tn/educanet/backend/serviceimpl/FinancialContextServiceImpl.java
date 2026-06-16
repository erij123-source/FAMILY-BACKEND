package tn.educanet.backend.serviceimpl;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import tn.educanet.backend.persistence.Compte;
import tn.educanet.backend.persistence.Facture;
import tn.educanet.backend.persistence.Membre;
import tn.educanet.backend.persistence.User;
import tn.educanet.backend.service.ChargeService;
import tn.educanet.backend.service.CompteService;
import tn.educanet.backend.service.FactureService;
import tn.educanet.backend.service.FinancialContextService;
import tn.educanet.backend.service.MembreService;
import tn.educanet.backend.service.RevenuService;
import tn.educanet.backend.service.UserService;

/**
 * Assembles a real, per-user financial summary from the existing domain
 * services. It prefers the familleId/membreId sent by the frontend (reliable
 * even when the short-lived JWT has expired), and falls back to resolving the
 * authenticated principal from the JWT-populated SecurityContext.
 *
 * When nothing can be resolved it returns an explicit "no data" block so the
 * model is told NOT to invent numbers — never an empty string.
 */
@Service
public class FinancialContextServiceImpl implements FinancialContextService {

    private static final String NO_DATA =
            "=== DONNEES FINANCIERES ===\n"
            + "AUCUNE DONNEE DISPONIBLE : l'utilisateur n'a pas pu etre identifie "
            + "(non connecte, session invalide, ou compte sans donnees).\n"
            + "N'invente AUCUN chiffre. Invite l'utilisateur a se connecter et a saisir ses donnees dans l'application.\n"
            + "=== FIN DES DONNEES ===";

    @Autowired
    private UserService userService;
    @Autowired
    private MembreService membreService;
    @Autowired
    private CompteService compteService;
    @Autowired
    private ChargeService chargeService;
    @Autowired
    private RevenuService revenuService;
    @Autowired
    private FactureService factureService;

    @Override
    public String build(Integer reqFamilleId, Integer reqMembreId, String reqUsername) {
        try {
            Integer familleId = reqFamilleId;
            Membre membre = null;

            if (familleId != null) {
                // Identifiers came from the frontend session (preferred path).
                if (reqMembreId != null) {
                    membre = membreService.findById(reqMembreId).orElse(null);
                }
            } else {
                // Resolve from the username sent by the frontend, else the JWT principal.
                String username = (reqUsername != null && !reqUsername.isBlank())
                        ? reqUsername : principalUsername();
                if (username == null) {
                    System.out.println("[FinancialContext] no familleId, no username, no principal -> no data");
                    return NO_DATA;
                }
                User user = userService.getByUsername(username).orElse(null);
                if (user == null) {
                    System.out.println("[FinancialContext] user not found for username=" + username + " -> no data");
                    return NO_DATA;
                }
                membre = membreService.findByUserId(user.getId()).orElse(null);
                if (membre == null || membre.getFamille() == null) {
                    System.out.println("[FinancialContext] no membre/famille for userId=" + user.getId() + " -> no data");
                    return NO_DATA;
                }
                familleId = membre.getFamille().getId();
            }

            Compte compte = compteService.findByFamilleId(familleId).orElse(null);
            if (compte == null) {
                System.out.println("[FinancialContext] no compte for familleId=" + familleId + " -> no data");
                return NO_DATA;
            }
            int compteId = compte.getId();
            String cur = compte.getCurrency() != null ? compte.getCurrency() : "TND";
            String familleName = compte.getFamille() != null && compte.getFamille().getName() != null
                    ? compte.getFamille().getName() : "ma famille";

            LocalDate today = LocalDate.now();
            String moisNom = today.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);

            // ── Compte familial ──
            double soldeActuel = compte.getCurrentBalance();
            double soldeRestant = compte.getRemainingBalance();
            double budgetRestant = compteService.getBudgetRestant(familleId);

            // ── Ce mois-ci (famille) ──
            double revenusMois = revenuService.getTotalByCompteThisMonth(compteId);
            double chargesMois = chargeService.getTotalByCompteThisMonth(compteId);
            double facturesPayeesMois = factureService.getTotalByCompteThisMonth(compteId);
            double soldeMois = compteService.calculateRemainingBalanceThisMonth(familleId);
            double resteAVivreMois = revenusMois - chargesMois;

            // ── Cumul ──
            double totalRevenu = compteService.getTotalRevenu(familleId);
            double totalCharges = compteService.getTotalCharges(familleId);
            double totalFactures = compteService.getTotalFactures(familleId);

            // ── Factures en attente ──
            List<Facture> factures = factureService.findByCompteId(compteId);
            int countPending = 0;
            double sumPending = 0;
            StringBuilder facturesList = new StringBuilder();
            for (Facture f : factures) {
                if (f.getStatut() != Facture.Statut.PAYEE) {
                    countPending++;
                    sumPending += f.getAmount();
                    if (countPending <= 8) {
                        facturesList.append(String.format(Locale.FRENCH,
                                "    - %s : %.2f %s (echeance : %s, statut : %s)%n",
                                f.getTitle(), f.getAmount(), cur,
                                f.getDueDate() != null ? f.getDueDate().toString() : "n/a",
                                f.getStatut()));
                    }
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== DONNEES FINANCIERES REELLES DE L'UTILISATEUR (source : FamilyBalance) ===\n");
            sb.append(String.format("Famille : %s | Devise : %s | Date du jour : %s | Mois courant : %s %d%n",
                    familleName, cur, today, moisNom, today.getYear()));

            sb.append("\n-- Compte familial --\n");
            sb.append(String.format(Locale.FRENCH, "Solde actuel : %.2f %s%n", soldeActuel, cur));
            sb.append(String.format(Locale.FRENCH, "Solde restant (revenus - budgets distribues) : %.2f %s%n", soldeRestant, cur));
            sb.append(String.format(Locale.FRENCH, "Budget restant ce mois : %.2f %s%n", budgetRestant, cur));

            sb.append("\n-- Ce mois-ci (toute la famille) --\n");
            sb.append(String.format(Locale.FRENCH, "Revenus du mois : %.2f %s%n", revenusMois, cur));
            sb.append(String.format(Locale.FRENCH, "Charges du mois : %.2f %s%n", chargesMois, cur));
            sb.append(String.format(Locale.FRENCH, "Factures payees ce mois : %.2f %s%n", facturesPayeesMois, cur));
            sb.append(String.format(Locale.FRENCH, "Reste a vivre estime ce mois (revenus - charges) : %.2f %s%n", resteAVivreMois, cur));
            sb.append(String.format(Locale.FRENCH, "Solde restant estime ce mois : %.2f %s%n", soldeMois, cur));

            sb.append("\n-- Cumul depuis le debut --\n");
            sb.append(String.format(Locale.FRENCH, "Revenus totaux : %.2f %s%n", totalRevenu, cur));
            sb.append(String.format(Locale.FRENCH, "Charges totales : %.2f %s%n", totalCharges, cur));
            sb.append(String.format(Locale.FRENCH, "Factures totales : %.2f %s%n", totalFactures, cur));

            // ── Section personnelle (si le membre est connu) ──
            if (membre != null) {
                int membreId = membre.getId();
                double mesRevenusMois = revenuService.getTotalByMembreThisMonth(membreId);
                double mesChargesMois = chargeService.getTotalByMembreThisMonth(membreId);
                String prenom = (membre.getUser() != null && membre.getUser().getFirstName() != null
                        && !membre.getUser().getFirstName().isBlank())
                        ? membre.getUser().getFirstName() : "l'utilisateur";
                sb.append("\n-- Mes donnees personnelles (").append(prenom)
                  .append(", role ").append(membre.getRole()).append(") --\n");
                sb.append(String.format(Locale.FRENCH, "Mon budget : %.2f %s | Budget restant : %.2f %s%n",
                        membre.getBudget(), cur, membre.getRemainingBudget(), cur));
                sb.append(String.format(Locale.FRENCH, "Mes revenus ce mois : %.2f %s%n", mesRevenusMois, cur));
                sb.append(String.format(Locale.FRENCH, "Mes depenses ce mois : %.2f %s%n", mesChargesMois, cur));
            }

            sb.append("\n-- Factures en attente (non payees / en retard) --\n");
            sb.append(String.format(Locale.FRENCH, "%d facture(s) en attente pour un total de %.2f %s%n", countPending, sumPending, cur));
            if (facturesList.length() > 0) {
                sb.append(facturesList);
            }
            sb.append("=== FIN DES DONNEES ===");

            System.out.println("[FinancialContext] built for familleId=" + familleId + " (compteId=" + compteId
                    + ", membre=" + (membre != null ? membre.getId() : "n/a") + ")");
            return sb.toString();
        } catch (Exception e) {
            System.err.println("[FinancialContext] build failed: " + e.getMessage());
            return NO_DATA;
        }
    }

    private String principalUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return auth.getName();
    }

}
