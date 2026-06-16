package tn.educanet.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tn.educanet.backend.dto.MonthlyStatsDTO;
import tn.educanet.backend.dto.PieChartDTO;
import tn.educanet.backend.service.DashboardService;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // Charges
    @GetMapping("/charges/monthly")
    public ResponseEntity<List<MonthlyStatsDTO>> chargesMonthly(
            @RequestParam int familleId,
            @RequestParam(defaultValue = "3") int period,
            @RequestParam(defaultValue = "false") boolean forward,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(dashboardService.chargesMonthly(familleId, period, forward, year, month));
    }

    @GetMapping("/charges/by-category")
    public ResponseEntity<List<PieChartDTO>> chargesByCategory(
            @RequestParam int familleId,
            @RequestParam(defaultValue = "3") int period,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(dashboardService.chargesByCategory(familleId, period, year, month));
    }

    @GetMapping("/charges/by-member")
    public ResponseEntity<List<PieChartDTO>> chargesByMember(
            @RequestParam int familleId,
            @RequestParam(defaultValue = "3") int period,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(dashboardService.chargesByMember(familleId, period, year, month));
    }

    // Factures
    @GetMapping("/factures/monthly")
    public ResponseEntity<List<MonthlyStatsDTO>> facturesMonthly(
            @RequestParam int familleId,
            @RequestParam(defaultValue = "3") int period,
            @RequestParam(defaultValue = "false") boolean forward,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(dashboardService.facturesMonthly(familleId, period, forward, year, month));
    }

    @GetMapping("/factures/by-category")
    public ResponseEntity<List<PieChartDTO>> facturesByCategory(
            @RequestParam int familleId,
            @RequestParam(defaultValue = "3") int period,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(dashboardService.facturesByCategory(familleId, period, year, month));
    }

    @GetMapping("/factures/by-member")
    public ResponseEntity<List<PieChartDTO>> facturesByMember(
            @RequestParam int familleId,
            @RequestParam(defaultValue = "3") int period,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(dashboardService.facturesByMember(familleId, period, year, month));
    }

    // Revenus
    @GetMapping("/revenus/monthly")
    public ResponseEntity<List<MonthlyStatsDTO>> revenusMonthly(
            @RequestParam int familleId,
            @RequestParam(defaultValue = "3") int period,
            @RequestParam(defaultValue = "false") boolean forward,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(dashboardService.revenusMonthly(familleId, period, forward, year, month));
    }

    @GetMapping("/revenus/by-category")
    public ResponseEntity<List<PieChartDTO>> revenusByCategory(
            @RequestParam int familleId,
            @RequestParam(defaultValue = "3") int period,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(dashboardService.revenusByCategory(familleId, period, year, month));
    }

    @GetMapping("/revenus/by-member")
    public ResponseEntity<List<PieChartDTO>> revenusByMember(
            @RequestParam int familleId,
            @RequestParam(defaultValue = "3") int period,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(dashboardService.revenusByMember(familleId, period, year, month));
    }

    // Budget (cross-domain)
    @GetMapping("/budget/by-member")
    public ResponseEntity<List<PieChartDTO>> budgetByMember(
            @RequestParam int familleId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(dashboardService.budgetByMember(familleId, year, month));
    }

    @GetMapping("/budget/spending-by-member")
    public ResponseEntity<List<PieChartDTO>> spendingByMember(
            @RequestParam int familleId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(dashboardService.spendingByMember(familleId, year, month));
    }
}
