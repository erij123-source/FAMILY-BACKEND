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

import tn.educanet.backend.persistence.Charge;
import tn.educanet.backend.service.ChargeService;

@RestController
@RequestMapping("/charges")
public class ChargeController {

    @Autowired
    private ChargeService chargeService;

    @GetMapping
    public ResponseEntity<List<Charge>> findAll() {
        return ResponseEntity.ok(chargeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Charge> findById(@PathVariable int id) {
        return chargeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/membre/{membreId}")
    public ResponseEntity<List<Charge>> findByMembreId(@PathVariable int membreId) {
        return ResponseEntity.ok(chargeService.findByMembreId(membreId));
    }

    @GetMapping("/compte/{compteId}")
    public ResponseEntity<List<Charge>> findByCompteId(@PathVariable int compteId) {
        return ResponseEntity.ok(chargeService.findByCompteId(compteId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Charge>> findByType(@PathVariable Charge.Type type) {
        return ResponseEntity.ok(chargeService.findByType(type));
    }

    @GetMapping("/check-budget-limit")
    public ResponseEntity<Boolean> checkBudgetLimit(
            @RequestParam int compteId,
            @RequestParam double montant) {
        return ResponseEntity.ok(chargeService.checkBudgetLimit(compteId, montant));
    }

    @GetMapping("/membre/{membreId}/total/mois")
    public ResponseEntity<Double> getTotalThisMonth(@PathVariable int membreId) {
        return ResponseEntity.ok(chargeService.getTotalByMembreThisMonth(membreId));
    }

    @GetMapping("/membre/{membreId}/total/annee")
    public ResponseEntity<Double> getTotalThisYear(@PathVariable int membreId) {
        return ResponseEntity.ok(chargeService.getTotalByMembreThisYear(membreId));
    }

    @PostMapping("/add")
    public ResponseEntity<Charge> add(@RequestBody Charge charge) {
        return ResponseEntity.ok(chargeService.add(charge));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<Charge> edit(@PathVariable int id, @RequestBody Charge charge) {
        return ResponseEntity.ok(chargeService.edit(id, charge));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        chargeService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/compte/{compteId}/total/mois")
    public ResponseEntity<Double> getTotalByCompteThisMonth(@PathVariable int compteId) {
        return ResponseEntity.ok(chargeService.getTotalByCompteThisMonth(compteId));
    }

}