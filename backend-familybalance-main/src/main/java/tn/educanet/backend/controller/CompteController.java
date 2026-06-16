package tn.educanet.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tn.educanet.backend.persistence.Compte;
import tn.educanet.backend.service.ChargeService;
import tn.educanet.backend.service.CompteService;
import tn.educanet.backend.service.FactureService;
import tn.educanet.backend.service.RevenuService;

@RestController
@RequestMapping("/comptes")
public class CompteController {

    @Autowired
    private CompteService compteService;

    @Autowired
    private RevenuService revenuService;

    @Autowired
    private ChargeService chargeService;

    @Autowired
    private FactureService factureService;

    @GetMapping
    public ResponseEntity<List<Compte>> findAll() {
        return ResponseEntity.ok(compteService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Compte> findById(@PathVariable int id) {
        return compteService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/famille/{familleId}")
    public ResponseEntity<Compte> findByFamilleId(@PathVariable int familleId) {
        return compteService.findByFamilleId(familleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Compte> save(@RequestBody Compte compte) {
        return ResponseEntity.ok(compteService.save(compte));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Compte> update(@PathVariable int id, @RequestBody Compte compte) {
        compte.setId(id);
        return ResponseEntity.ok(compteService.save(compte));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable int id) {
        compteService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total-revenu/{familleId}")
    public ResponseEntity<Double> getTotalRevenu(@PathVariable int familleId) {
        return ResponseEntity.ok(compteService.getTotalRevenu(familleId));
    }

    // ── FIX #8 : Ajout de @RequestParam month et year (optionnels) ──────────
    @GetMapping("/total-budget-distributed/{familleId}")
    public ResponseEntity<Double> getTotalBudgetDistributed(
            @PathVariable int familleId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(compteService.getTotalBudgetDistributed(familleId, month, year));
    }

    @GetMapping("/remaining-balance/{familleId}")
    public ResponseEntity<Double> calculateRemainingBalance(@PathVariable int familleId) {
        return ResponseEntity.ok(compteService.calculateRemainingBalance(familleId));
    }

    @GetMapping("/total-revenu-mois/{familleId}")
    public ResponseEntity<Double> getTotalRevenuMois(@PathVariable int familleId) {
        Compte compte = compteService.findByFamilleId(familleId).orElseThrow();
        return ResponseEntity.ok(revenuService.getTotalByCompteThisMonth(compte.getId()));
    }

    @GetMapping("/total-charges-mois/{familleId}")
    public ResponseEntity<Double> getTotalChargesMois(@PathVariable int familleId) {
        Compte compte = compteService.findByFamilleId(familleId).orElseThrow();
        return ResponseEntity.ok(chargeService.getTotalByCompteThisMonth(compte.getId()));
    }

    @GetMapping("/remaining-balance-mois/{familleId}")
    public ResponseEntity<Double> getRemainingBalanceMois(@PathVariable int familleId) {
        return ResponseEntity.ok(compteService.calculateRemainingBalanceThisMonth(familleId));
    }

    @GetMapping("/total-charges/{familleId}")
    public ResponseEntity<Double> getTotalCharges(@PathVariable int familleId) {
        return ResponseEntity.ok(compteService.getTotalCharges(familleId));
    }

    @GetMapping("/total-factures/{familleId}")
    public ResponseEntity<Double> getTotalFactures(@PathVariable int familleId) {
        return ResponseEntity.ok(compteService.getTotalFactures(familleId));
    }

    @GetMapping("/total-factures-mois/{familleId}")
    public ResponseEntity<Double> getTotalFacturesMois(@PathVariable int familleId) {
        Compte compte = compteService.findByFamilleId(familleId).orElseThrow();
        return ResponseEntity.ok(factureService.getTotalByCompteThisMonth(compte.getId()));
    }

    /** Budget restant glissant = report mois précédent + revenus du mois − distribué. */
    @GetMapping("/budget-restant/{familleId}")
    public ResponseEntity<Double> getBudgetRestant(@PathVariable int familleId) {
        return ResponseEntity.ok(compteService.getBudgetRestant(familleId));
    }

    /** Report de revenu non distribué hérité du mois précédent (sous-info UI). */
    @GetMapping("/revenu-carryover/{familleId}")
    public ResponseEntity<Double> getRevenuCarryover(@PathVariable int familleId) {
        return ResponseEntity.ok(compteService.getFamilleRevenuCarryover(familleId));
    }
}