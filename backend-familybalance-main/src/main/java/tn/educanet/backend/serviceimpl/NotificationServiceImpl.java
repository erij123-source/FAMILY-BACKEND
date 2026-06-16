package tn.educanet.backend.serviceimpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import tn.educanet.backend.dao.CompteDao;
import tn.educanet.backend.dao.FamilleDao;
import tn.educanet.backend.dao.NotificationDao;
import tn.educanet.backend.persistence.Compte;
import tn.educanet.backend.persistence.Facture;
import tn.educanet.backend.persistence.Famille;
import tn.educanet.backend.persistence.Membre;
import tn.educanet.backend.persistence.Notification;
import tn.educanet.backend.persistence.Notification.Kind;
import tn.educanet.backend.persistence.Notification.Severity;
import tn.educanet.backend.service.ChargeService;
import tn.educanet.backend.service.FactureService;
import tn.educanet.backend.service.MembreService;
import tn.educanet.backend.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Resource
    private NotificationDao notificationDao;

    @Resource
    private FamilleDao familleDao;

    @Resource
    private CompteDao compteDao;

    @Resource
    private MembreService membreService;

    @Resource
    private ChargeService chargeService;

    @Resource
    private FactureService factureService;

    /** Rôle backend identifiant l'administrateur de la famille. */
    private static final String ROLE_ADMIN = "ADMINISTRATEUR";

    @Override
    public List<Notification> findByFamille(int familleId) {
        return notificationDao.findByFamilleIdOrderByCreatedAtDesc(familleId);
    }

    /** Listing du mois courant pour le membre — vide chaque 1er du mois car
     *  on filtre sur createdAt ≥ premier jour du mois courant. */
    @Override
    public List<Notification> findByMembreCurrentMonth(int membreId) {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return notificationDao.findByMembreSince(membreId, startOfMonth);
    }

    @Override
    public Notification add(Notification notification) {
        if (notification.getFamille() != null && notification.getFamille().getId() > 0) {
            Famille managed = familleDao.findById(notification.getFamille().getId()).orElse(null);
            if (managed != null) notification.setFamille(managed);
        }
        return notificationDao.save(notification);
    }

    @Override
    public Notification setRead(int id, boolean read) {
        return notificationDao.findById(id).map(n -> {
            n.setReadFlag(read);
            return notificationDao.save(n);
        }).orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    @Override
    public void delete(int id) {
        notificationDao.deleteById(id);
    }

    @Override
    @Transactional
    public int markAllReadForMembre(int membreId) {
        List<Notification> list = findByMembreCurrentMonth(membreId);
        int touched = 0;
        for (Notification n : list) {
            if (!n.isReadFlag()) {
                n.setReadFlag(true);
                notificationDao.save(n);
                touched++;
            }
        }
        return touched;
    }

    @Override
    @Transactional
    public int clearReadForMembre(int membreId) {
        List<Notification> list = findByMembreCurrentMonth(membreId);
        int removed = 0;
        for (Notification n : list) {
            if (n.isReadFlag()) {
                notificationDao.delete(n);
                removed++;
            }
        }
        return removed;
    }

    @Override
    public long countUnreadForMembre(int membreId) {
        // On compte uniquement les non-lues du mois courant (cohérent avec la liste).
        return findByMembreCurrentMonth(membreId).stream()
                .filter(n -> !n.isReadFlag())
                .count();
    }

    @Override
    @Transactional
    public int purgePreviousMonths() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return notificationDao.deleteAllBefore(startOfMonth);
    }

    // ════════════════════════════════════════════════════════════════════
    // Génération des notifications
    // ════════════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public int generateForFamille(int familleId) {
        Famille famille = familleDao.findById(familleId).orElse(null);
        if (famille == null) return 0;

        List<Membre> membres = membreService.findByFamilleId(famille.getId());
        Membre admin = findAdmin(membres);

        // Set des titres existants (non lus, mois courant) PAR MEMBRE pour
        // garantir l'idempotence : on ne recrée pas une notif déjà présente
        // pour le même membre + même titre.
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        java.util.Map<Integer, Set<String>> existingByMembre = new java.util.HashMap<>();
        for (Membre m : membres) {
            Set<String> titles = new HashSet<>();
            for (Notification n : notificationDao.findByMembreSince(m.getId(), startOfMonth)) {
                titles.add(n.getTitle());
            }
            existingByMembre.put(m.getId(), titles);
        }

        int created = 0;
        created += generateBudgetAlerts(famille, membres, admin, existingByMembre);
        created += generateFactureAlerts(famille, membres, admin, existingByMembre);
        return created;
    }

    // ── Budget : 1/4, 1/8, épuisé. Admin reçoit aussi "épuisé" pour autrui. ──
    private int generateBudgetAlerts(Famille famille, List<Membre> membres,
                                     Membre admin,
                                     java.util.Map<Integer, Set<String>> existingByMembre) {
        int created = 0;
        for (Membre m : membres) {
            double budget = m.getBudget();
            String name = memberName(m);
            if (budget <= 0) continue;

            double charges = chargeService.getTotalByMembreThisMonth(m.getId());
            double factures = factureService.getTotalByMembreThisMonth(m.getId());
            double spent = charges + factures;
            double remaining = budget - spent;

            // Seuils : "épuisé" prime, puis 1/8 (≤ 12.5% du budget), puis 1/4 (≤ 25%).
            if (remaining <= 0) {
                String title = "Budget épuisé — " + name;
                if (existingByMembre.get(m.getId()).add(title)) {
                    persist(famille, m, Kind.ALERT, Severity.CRITICAL, title,
                        "Le budget de " + name + " est totalement épuisé. Dépensé : "
                          + fmt(spent) + " DT sur " + fmt(budget) + " DT alloués.",
                        "savings");
                    created++;
                }
                // L'admin reçoit AUSSI cette notification, sauf si c'est lui-même.
                if (admin != null && admin.getId() != m.getId()) {
                    String adminTitle = "Budget épuisé — " + name;
                    if (existingByMembre.get(admin.getId()).add(adminTitle)) {
                        persist(famille, admin, Kind.ALERT, Severity.CRITICAL, adminTitle,
                            "Le budget de " + name + " est totalement épuisé ("
                              + fmt(spent) + " DT / " + fmt(budget) + " DT).",
                            "savings");
                        created++;
                    }
                }
            } else if (remaining <= budget / 8.0) {
                String title = "Budget critique (1/8 restant) — " + name;
                if (existingByMembre.get(m.getId()).add(title)) {
                    persist(famille, m, Kind.ALERT, Severity.WARNING, title,
                        "Il reste moins d'1/8 du budget de " + name + " ("
                          + fmt(remaining) + " DT sur " + fmt(budget) + " DT).",
                        "warning");
                    created++;
                }
                // Admin NE reçoit PAS ce seuil pour les autres membres.
            } else if (remaining <= budget / 4.0) {
                String title = "Budget faible (1/4 restant) — " + name;
                if (existingByMembre.get(m.getId()).add(title)) {
                    persist(famille, m, Kind.ALERT, Severity.WARNING, title,
                        "Il reste 1/4 du budget de " + name + " ("
                          + fmt(remaining) + " DT sur " + fmt(budget) + " DT).",
                        "warning");
                    created++;
                }
                // Admin NE reçoit PAS ce seuil pour les autres membres.
            }
        }
        return created;
    }

    // ── Factures : J-3, J-1, en retard. Chacun reçoit pour SES factures. ──
    private int generateFactureAlerts(Famille famille, List<Membre> membres,
                                       Membre admin,
                                       java.util.Map<Integer, Set<String>> existingByMembre) {
        int created = 0;
        Compte compte = compteDao.findByFamilleId(famille.getId()).orElse(null);
        if (compte == null) return 0;

        LocalDate today = LocalDate.now();
        List<Facture> factures = factureService.findByCompteId(compte.getId());
        for (Facture f : factures) {
            if (f.getStatut() == Facture.Statut.PAYEE) continue;
            LocalDate ref = f.getDueDate() != null ? f.getDueDate() : f.getIssueDate();
            if (ref == null) continue;
            long days = ChronoUnit.DAYS.between(today, ref);

            Membre owner = f.getMembre();
            if (owner == null) continue;
            Set<String> existing = existingByMembre.computeIfAbsent(owner.getId(),
                k -> new HashSet<>());

            if (days < 0) {
                // Échue / en retard.
                String title = "Facture en retard — " + safe(f.getTitle());
                if (existing.add(title)) {
                    persist(famille, owner, Kind.ALERT, Severity.CRITICAL, title,
                        safe(f.getTitle()) + " (" + fmt(f.getAmount())
                          + " DT) est en retard de " + (-days) + " jour(s).",
                        "error");
                    created++;
                }
            } else if (days == 1) {
                // J-1 : échéance demain.
                String title = "Échéance demain — " + safe(f.getTitle());
                if (existing.add(title)) {
                    persist(famille, owner, Kind.REMINDER, Severity.WARNING, title,
                        safe(f.getTitle()) + " (" + fmt(f.getAmount())
                          + " DT) arrive à échéance demain.",
                        "schedule");
                    created++;
                }
            } else if (days == 3) {
                // J-3.
                String title = "Échéance dans 3 jours — " + safe(f.getTitle());
                if (existing.add(title)) {
                    persist(famille, owner, Kind.REMINDER, Severity.INFO, title,
                        safe(f.getTitle()) + " (" + fmt(f.getAmount())
                          + " DT) arrive à échéance dans 3 jours.",
                        "schedule");
                    created++;
                }
            }
        }
        return created;
    }

    /** Trouve l'administrateur d'une famille parmi sa liste de membres. */
    private Membre findAdmin(List<Membre> membres) {
        for (Membre m : membres) {
            if (m.getRole() != null && ROLE_ADMIN.equalsIgnoreCase(m.getRole().toString())) {
                return m;
            }
        }
        return null;
    }

    private void persist(Famille famille, Membre membre, Kind kind, Severity sev,
                         String title, String message, String icon) {
        Notification n = new Notification();
        n.setFamille(famille);
        n.setMembre(membre);
        n.setKind(kind);
        n.setSeverity(sev);
        n.setTitle(title);
        n.setMessage(message);
        n.setIcon(icon);
        notificationDao.save(n);
    }

    private static String memberName(Membre m) {
        if (m.getUser() == null) return "Membre #" + m.getId();
        String f = m.getUser().getFirstName();
        String l = m.getUser().getLastName();
        String full = ((f == null ? "" : f) + " " + (l == null ? "" : l)).trim();
        return full.isEmpty() ? ("Membre #" + m.getId()) : full;
    }

    @SuppressWarnings("unused")
    private static List<Membre> singletonList(Membre m) {
        List<Membre> l = new ArrayList<>();
        l.add(m);
        return l;
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String fmt(double d) { return String.format("%.0f", d); }
}
