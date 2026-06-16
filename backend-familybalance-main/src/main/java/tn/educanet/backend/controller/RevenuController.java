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
import org.springframework.web.bind.annotation.RestController;

import tn.educanet.backend.dto.RevenuDTO;
import tn.educanet.backend.persistence.Revenu;
import tn.educanet.backend.service.RevenuService;
import java.util.stream.Collectors; 


@RestController
@RequestMapping("/revenus")
public class RevenuController {

    @Autowired
    private RevenuService revenuService;

    @GetMapping
    public ResponseEntity<List<Revenu>> findAll() {
        return ResponseEntity.ok(revenuService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Revenu> findById(@PathVariable int id) {
        return revenuService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/membre/{membreId}")
    public ResponseEntity<List<Revenu>> findByMembreId(@PathVariable int membreId) {
        return ResponseEntity.ok(revenuService.findByMembreId(membreId));
    }

    @GetMapping("/compte/{compteId}")
    public ResponseEntity<List<RevenuDTO>> findByCompte(@PathVariable int compteId) {
        List<Revenu> revenus = revenuService.findByCompteId(compteId);
        List<RevenuDTO> dtos = revenus.stream()
            .map(RevenuDTO::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/total-amount/{compteId}")
    public ResponseEntity<Double> getTotalAmount(@PathVariable int compteId) {
        return ResponseEntity.ok(revenuService.getTotalAmount(compteId));
    }

    @PostMapping("/ajouter")
    public ResponseEntity<Revenu> ajouter(@RequestBody Revenu revenu) {
        return ResponseEntity.ok(revenuService.ajouter(revenu));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<Revenu> edit(
            @PathVariable int id,
            @RequestBody Revenu revenu) {
        return ResponseEntity.ok(revenuService.edit(id, revenu));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        revenuService.delete(id);
        return ResponseEntity.noContent().build();
    }
    /** Total revenus du membre pour le mois en cours */
    @GetMapping("/membre/{membreId}/total/mois")
    public ResponseEntity<Double> getTotalThisMonth(@PathVariable int membreId) {
        return ResponseEntity.ok(revenuService.getTotalByMembreThisMonth(membreId));
    }

    /** Total revenus du membre pour l'année en cours */
    @GetMapping("/membre/{membreId}/total/annee")
    public ResponseEntity<Double> getTotalThisYear(@PathVariable int membreId) {
        return ResponseEntity.ok(revenuService.getTotalByMembreThisYear(membreId));
    }
    
    @GetMapping("/compte/{compteId}/total/mois")
    public ResponseEntity<Double> getTotalByCompteThisMonth(@PathVariable int compteId) {
        return ResponseEntity.ok(revenuService.getTotalByCompteThisMonth(compteId));
    }

}