package tn.educanet.backend.service;

import java.util.List;

import tn.educanet.backend.dto.MonthlyStatsDTO;
import tn.educanet.backend.dto.PieChartDTO;

/**
 * Read-only aggregation layer that powers the Statistiques pages.
 *
 * Every method takes the {@code familleId} (so we never leak data across
 * accounts) plus a {@code period} expressed in months — typically 3, 6 or
 * 12 to match the toggle in the UI.
 *
 * The {@code year} / {@code month} pair is optional: when both are
 * provided they replace "now" as the anchor of the rolling window.
 */
public interface DashboardService {

    // Charges
    List<MonthlyStatsDTO> chargesMonthly(int familleId, int period);
    List<MonthlyStatsDTO> chargesMonthly(int familleId, int period, boolean forward);
    List<MonthlyStatsDTO> chargesMonthly(int familleId, int period, boolean forward, Integer year, Integer month);
    List<PieChartDTO>     chargesByCategory(int familleId, int period);
    List<PieChartDTO>     chargesByCategory(int familleId, int period, Integer year, Integer month);
    List<PieChartDTO>     chargesByMember(int familleId, int period);
    List<PieChartDTO>     chargesByMember(int familleId, int period, Integer year, Integer month);

    // Factures
    List<MonthlyStatsDTO> facturesMonthly(int familleId, int period);
    List<MonthlyStatsDTO> facturesMonthly(int familleId, int period, boolean forward);
    List<MonthlyStatsDTO> facturesMonthly(int familleId, int period, boolean forward, Integer year, Integer month);
    List<PieChartDTO>     facturesByCategory(int familleId, int period);
    List<PieChartDTO>     facturesByCategory(int familleId, int period, Integer year, Integer month);
    List<PieChartDTO>     facturesByMember(int familleId, int period);
    List<PieChartDTO>     facturesByMember(int familleId, int period, Integer year, Integer month);

    // Revenus
    List<MonthlyStatsDTO> revenusMonthly(int familleId, int period);
    List<MonthlyStatsDTO> revenusMonthly(int familleId, int period, boolean forward);
    List<MonthlyStatsDTO> revenusMonthly(int familleId, int period, boolean forward, Integer year, Integer month);
    List<PieChartDTO>     revenusByCategory(int familleId, int period);
    List<PieChartDTO>     revenusByCategory(int familleId, int period, Integer year, Integer month);
    List<PieChartDTO>     revenusByMember(int familleId, int period);
    List<PieChartDTO>     revenusByMember(int familleId, int period, Integer year, Integer month);

    // Budget (cross-domain)
    List<PieChartDTO> budgetByMember(int familleId);
    List<PieChartDTO> budgetByMember(int familleId, Integer year, Integer month);
    List<PieChartDTO> spendingByMember(int familleId);
    List<PieChartDTO> spendingByMember(int familleId, Integer year, Integer month);
}
