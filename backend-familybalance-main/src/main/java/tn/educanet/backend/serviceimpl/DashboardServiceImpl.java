package tn.educanet.backend.serviceimpl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import tn.educanet.backend.dao.ChargeDao;
import tn.educanet.backend.dao.CompteDao;
import tn.educanet.backend.dao.FactureDao;
import tn.educanet.backend.dao.MembreDao;
import tn.educanet.backend.dao.RevenuDao;
import tn.educanet.backend.dto.MonthlyStatsDTO;
import tn.educanet.backend.dto.PieChartDTO;
import tn.educanet.backend.persistence.Charge;
import tn.educanet.backend.persistence.Compte;
import tn.educanet.backend.persistence.Facture;
import tn.educanet.backend.persistence.Membre;
import tn.educanet.backend.persistence.Revenu;
import tn.educanet.backend.persistence.User;
import tn.educanet.backend.service.DashboardService;

/**
 * Aggregates raw entities into chart-ready DTOs.
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private CompteDao compteDao;
    @Resource
    private ChargeDao chargeDao;
    @Resource
    private FactureDao factureDao;
    @Resource
    private RevenuDao revenuDao;
    @Resource
    private MembreDao membreDao;

    private static final String[] FR_MONTHS = {
            "Jan", "Fév", "Mar", "Avr", "Mai", "Jun", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"
    };

    // Charges
    @Override
    public List<MonthlyStatsDTO> chargesMonthly(int familleId, int period) {
        return chargesMonthly(familleId, period, false, null, null);
    }

    @Override
    public List<MonthlyStatsDTO> chargesMonthly(int familleId, int period, boolean forward) {
        return chargesMonthly(familleId, period, forward, null, null);
    }

    @Override
    public List<MonthlyStatsDTO> chargesMonthly(int familleId, int period, boolean forward,
            Integer year, Integer month) {
        List<Charge> charges = chargesForFamille(familleId);
        // Plus de periodicite : on impute chaque charge au mois de chargeDate
        // (même comportement que "PONCTUELLE" dans groupByMonthRecurring).
        return groupByMonthRecurring(period, charges,
                c -> c.getChargeDate(),
                c -> "PONCTUELLE",
                c -> c.getAmount(), forward, anchor(year, month));
    }

    @Override
    public List<PieChartDTO> chargesByCategory(int familleId, int period) {
        return chargesByCategory(familleId, period, null, null);
    }

    @Override
    public List<PieChartDTO> chargesByCategory(int familleId, int period, Integer year, Integer month) {
        return groupByLabelInWindow(
                chargesForFamille(familleId), period, year, month,
                c -> c.getChargeDate(),
                c -> c.getCategorie() != null ? c.getCategorie().getName() : "Sans catégorie",
                c -> c.getAmount());
    }

    @Override
    public List<PieChartDTO> chargesByMember(int familleId, int period) {
        return chargesByMember(familleId, period, null, null);
    }

    @Override
    public List<PieChartDTO> chargesByMember(int familleId, int period, Integer year, Integer month) {
        return groupByLabelInWindow(
                chargesForFamille(familleId), period, year, month,
                c -> c.getChargeDate(),
                c -> c.getMembre() != null ? memberLabel(c.getMembre().getUser()) : "Inconnu",
                c -> c.getAmount());
    }

    // Factures
    @Override
    public List<MonthlyStatsDTO> facturesMonthly(int familleId, int period) {
        return facturesMonthly(familleId, period, false, null, null);
    }

    @Override
    public List<MonthlyStatsDTO> facturesMonthly(int familleId, int period, boolean forward) {
        return facturesMonthly(familleId, period, forward, null, null);
    }

    @Override
    public List<MonthlyStatsDTO> facturesMonthly(int familleId, int period, boolean forward,
            Integer year, Integer month) {
        return groupByMonth(period, facturesForFamille(familleId),
                f -> f.getIssueDate(), f -> f.getAmount(), forward, anchor(year, month));
    }

    @Override
    public List<PieChartDTO> facturesByCategory(int familleId, int period) {
        return facturesByCategory(familleId, period, null, null);
    }

    @Override
    public List<PieChartDTO> facturesByCategory(int familleId, int period, Integer year, Integer month) {
        return groupByLabelInWindow(
                facturesForFamille(familleId), period, year, month,
                f -> f.getIssueDate(),
                f -> f.getCategorie() != null ? f.getCategorie().getName() : "Sans catégorie",
                f -> f.getAmount());
    }

    @Override
    public List<PieChartDTO> facturesByMember(int familleId, int period) {
        return facturesByMember(familleId, period, null, null);
    }

    @Override
    public List<PieChartDTO> facturesByMember(int familleId, int period, Integer year, Integer month) {
        return groupByLabelInWindow(
                facturesForFamille(familleId), period, year, month,
                f -> f.getIssueDate(),
                f -> f.getMembre() != null ? memberLabel(f.getMembre().getUser()) : "Inconnu",
                f -> f.getAmount());
    }

    // Revenus
    @Override
    public List<MonthlyStatsDTO> revenusMonthly(int familleId, int period) {
        return revenusMonthly(familleId, period, false, null, null);
    }

    @Override
    public List<MonthlyStatsDTO> revenusMonthly(int familleId, int period, boolean forward) {
        return revenusMonthly(familleId, period, forward, null, null);
    }

    @Override
    public List<MonthlyStatsDTO> revenusMonthly(int familleId, int period, boolean forward,
            Integer year, Integer month) {
        return groupByMonthRecurring(period, revenusForFamille(familleId),
                r -> r.getPerceptionDate(),
                // FIXE = revenu récurrent mensuel (projeté chaque mois) ; VARIABLE = ponctuel.
                // On mappe vers les constantes utilisées par groupByMonthRecurring.
                r -> r.getKind() == Revenu.Kind.VARIABLE ? "PONCTUELLE" : "MENSUELLE",
                r -> r.getAmount(), forward, anchor(year, month));
    }

    @Override
    public List<PieChartDTO> revenusByCategory(int familleId, int period) {
        return revenusByCategory(familleId, period, null, null);
    }

    @Override
    public List<PieChartDTO> revenusByCategory(int familleId, int period, Integer year, Integer month) {
        return groupByLabelInWindow(
                revenusForFamille(familleId), period, year, month,
                r -> r.getPerceptionDate(),
                r -> r.getCategorie() != null ? r.getCategorie().getName() : "Sans catégorie",
                r -> r.getAmount());
    }

    @Override
    public List<PieChartDTO> revenusByMember(int familleId, int period) {
        return revenusByMember(familleId, period, null, null);
    }

    @Override
    public List<PieChartDTO> revenusByMember(int familleId, int period, Integer year, Integer month) {
        return groupByLabelInWindow(
                revenusForFamille(familleId), period, year, month,
                r -> r.getPerceptionDate(),
                r -> r.getMembre() != null ? memberLabel(r.getMembre().getUser()) : "Inconnu",
                r -> r.getAmount());
    }

    private <T> List<MonthlyStatsDTO> groupByMonthRecurring(int period,
            List<T> rows,
            Function<T, LocalDate> anchorOf,
            Function<T, String> periodOf,
            ToDoubleFunction<T> amountOf,
            boolean forward,
            YearMonth windowAnchor) {
        int p = clamp(period);
        YearMonth now = windowAnchor != null ? windowAnchor : YearMonth.now();
        YearMonth start = forward ? now : now.minusMonths(p - 1);
        YearMonth end = forward ? now.plusMonths(p - 1) : now;
        YearMonth realToday = YearMonth.now();
        YearMonth projectionEnd = forward ? end : (end.isAfter(realToday) ? realToday : end);
        Map<YearMonth, Double> totals = new LinkedHashMap<>();
        for (int i = 0; i < p; i++) {
            totals.put(start.plusMonths(i), 0.0);
        }
        for (T row : rows) {
            LocalDate anchorDate = anchorOf.apply(row);
            if (anchorDate == null) continue;
            YearMonth anchor = YearMonth.from(anchorDate);
            if (!forward && anchor.isAfter(end)) continue;

            String kind = periodOf.apply(row);
            double amount = amountOf.applyAsDouble(row);

            // Vue historique (forward=false) : aucune projection des récurrents.
            // Chaque entrée compte uniquement dans son mois réel — exactement
            // comme la page Revenus/Charges et les camemberts. La projection des
            // revenus/charges récurrents n'est conservée que pour la vue future
            // (forward=true), où il n'existe pas encore de données réelles.
            if (!forward) {
                if (!anchor.isBefore(start) && !anchor.isAfter(end)) {
                    totals.merge(anchor, amount, (a, b) -> a + b);
                }
                continue;
            }

            if ("PONCTUELLE".equals(kind)) {
                if (!anchor.isBefore(start) && !anchor.isAfter(end)) {
                    totals.merge(anchor, amount, (a, b) -> a + b);
                }
            } else if ("ANNUELLE".equals(kind)) {
                int month = anchor.getMonthValue();
                YearMonth ym = YearMonth.of(start.getYear(), month);
                if (ym.isBefore(start)) ym = ym.plusYears(1);
                if (!forward && ym.isBefore(anchor)) {
                    int yearsBehind = anchor.getYear() - ym.getYear();
                    ym = ym.plusYears(yearsBehind);
                    if (ym.isBefore(anchor)) ym = ym.plusYears(1);
                }
                while (!ym.isAfter(projectionEnd)) {
                    totals.merge(ym, amount, (a, b) -> a + b);
                    ym = ym.plusYears(1);
                }
            } else {
                YearMonth ym = anchor.isBefore(start) ? start : anchor;
                while (!ym.isAfter(projectionEnd)) {
                    totals.merge(ym, amount, (a, b) -> a + b);
                    ym = ym.plusMonths(1);
                }
            }
        }
        List<MonthlyStatsDTO> out = new ArrayList<>(totals.size());
        for (Map.Entry<YearMonth, Double> e : totals.entrySet()) {
            out.add(new MonthlyStatsDTO(FR_MONTHS[e.getKey().getMonthValue() - 1], round2(e.getValue())));
        }
        return out;
    }

    // Budget (cross-domain)
    @Override
    public List<PieChartDTO> budgetByMember(int familleId) {
        return budgetByMember(familleId, null, null);
    }

    @Override
    public List<PieChartDTO> budgetByMember(int familleId, Integer year, Integer month) {
        List<Membre> membres = membreDao.findByFamilleId(familleId);
        boolean filterByMonth = (year != null && month != null);
        List<PieChartDTO> out = new ArrayList<>(membres.size());
        for (Membre m : membres) {
            double value;
            if (filterByMonth) {
                // Each member's budget is stamped with the month/year it was
                // allocated for (budgetMois/budgetAnnee). Only count it when it
                // matches the requested month — otherwise the member had no
                // budget allocation that month.
                boolean matchesMonth = m.getBudgetMois() != null && m.getBudgetAnnee() != null
                        && m.getBudgetMois().intValue() == month.intValue()
                        && m.getBudgetAnnee().intValue() == year.intValue();
                value = matchesMonth ? m.getBudget() : 0.0;
            } else {
                value = m.getBudget();
            }
            out.add(new PieChartDTO(memberLabel(m.getUser()), round2(value)));
        }
        return out;
    }

    @Override
    public List<PieChartDTO> spendingByMember(int familleId) {
        return spendingByMember(familleId, null, null);
    }

    @Override
    public List<PieChartDTO> spendingByMember(int familleId, Integer year, Integer month) {
        List<Membre> membres = membreDao.findByFamilleId(familleId);
        YearMonth target = anchor(year, month);
        List<PieChartDTO> out = new ArrayList<>(membres.size());
        for (Membre m : membres) {
            double charges = chargeDao.findByMembreId(m.getId()).stream()
                    .filter(c -> c.getChargeDate() != null
                            && YearMonth.from(c.getChargeDate()).equals(target))
                    .mapToDouble(c -> c.getAmount()).sum();
            double factures = factureDao.findByMembreId(m.getId()).stream()
                    .filter(f -> f.getIssueDate() != null
                            && YearMonth.from(f.getIssueDate()).equals(target))
                    .mapToDouble(f -> f.getAmount()).sum();
            out.add(new PieChartDTO(memberLabel(m.getUser()), round2(charges + factures)));
        }
        return out;
    }

    private <T> List<MonthlyStatsDTO> groupByMonth(int period,
            List<T> rows,
            Function<T, LocalDate> dateOf,
            ToDoubleFunction<T> amountOf,
            boolean forward,
            YearMonth windowAnchor) {
        int p = clamp(period);
        YearMonth now = windowAnchor != null ? windowAnchor : YearMonth.now();
        YearMonth start = forward ? now : now.minusMonths(p - 1);
        YearMonth end = forward ? now.plusMonths(p - 1) : now;
        Map<YearMonth, Double> totals = new LinkedHashMap<>();
        for (int i = 0; i < p; i++) {
            totals.put(start.plusMonths(i), 0.0);
        }
        for (T row : rows) {
            LocalDate d = dateOf.apply(row);
            if (d == null) continue;
            YearMonth ym = YearMonth.from(d);
            if (ym.isBefore(start) || ym.isAfter(end)) continue;
            totals.merge(ym, amountOf.applyAsDouble(row), (a, b) -> a + b);
        }
        List<MonthlyStatsDTO> out = new ArrayList<>(totals.size());
        for (Map.Entry<YearMonth, Double> e : totals.entrySet()) {
            out.add(new MonthlyStatsDTO(FR_MONTHS[e.getKey().getMonthValue() - 1], round2(e.getValue())));
        }
        return out;
    }

    private <T> List<PieChartDTO> groupByLabelInWindow(List<T> rows,
            int period,
            Integer year, Integer month,
            Function<T, LocalDate> dateOf,
            Function<T, String> labelOf,
            ToDoubleFunction<T> amountOf) {
        YearMonth end = anchor(year, month);
        YearMonth start = end.minusMonths(clamp(period) - 1);
        Map<String, Double> totals = new LinkedHashMap<>();
        for (T row : rows) {
            LocalDate d = dateOf.apply(row);
            if (d == null) continue;
            YearMonth ym = YearMonth.from(d);
            if (ym.isBefore(start) || ym.isAfter(end)) continue;
            String label = labelOf.apply(row);
            if (label == null || label.isBlank()) label = "—";
            totals.merge(label, amountOf.applyAsDouble(row), (a, b) -> a + b);
        }
        List<PieChartDTO> out = new ArrayList<>(totals.size());
        for (Map.Entry<String, Double> e : totals.entrySet()) {
            out.add(new PieChartDTO(e.getKey(), round2(e.getValue())));
        }
        return out;
    }

    private YearMonth anchor(Integer year, Integer month) {
        if (year == null || month == null) return YearMonth.now();
        if (year < 1970 || year > 9999) return YearMonth.now();
        if (month < 1 || month > 12) return YearMonth.now();
        return YearMonth.of(year, month);
    }

    private int clamp(int period) {
        if (period <= 0) return 3;
        if (period > 24) return 24;
        return period;
    }

    private static double round2(double d) {
        return Math.round(d * 100.0) / 100.0;
    }

    private static String memberLabel(User u) {
        if (u == null) return "Inconnu";
        String f = u.getFirstName() == null ? "" : u.getFirstName().trim();
        String l = u.getLastName() == null ? "" : u.getLastName().trim();
        String full = (f + " " + l).trim();
        return full.isEmpty() ? "Inconnu" : full;
    }

    private List<Charge> chargesForFamille(int familleId) {
        Compte c = compteDao.findByFamilleId(familleId).orElse(null);
        if (c == null) return List.of();
        return chargeDao.findByCompteId(c.getId());
    }

    private List<Facture> facturesForFamille(int familleId) {
        Compte c = compteDao.findByFamilleId(familleId).orElse(null);
        if (c == null) return List.of();
        return factureDao.findByCompteId(c.getId());
    }

    private List<Revenu> revenusForFamille(int familleId) {
        Compte c = compteDao.findByFamilleId(familleId).orElse(null);
        if (c == null) return List.of();
        return revenuDao.findByCompteId(c.getId());
    }
}
