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

import tn.educanet.backend.dto.BudgetSummaryDTO;
import tn.educanet.backend.persistence.Membre;
import tn.educanet.backend.service.MembreService;

@RestController
@RequestMapping("/membres")
public class MembreController {

	@Autowired
	private MembreService membreService;

	@GetMapping
	public ResponseEntity<List<Membre>> findAll() {
		return ResponseEntity.ok(membreService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Membre> findById(@PathVariable int id) {
		return membreService.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/famille/{familleId}")
	public ResponseEntity<List<Membre>> findByFamilleId(@PathVariable int familleId) {
		return ResponseEntity.ok(membreService.findByFamilleId(familleId));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<Membre> findByUserId(@PathVariable int userId) {
		return membreService.findByUserId(userId)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Membre> save(@RequestBody Membre membre) {
		return ResponseEntity.ok(membreService.save(membre));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Membre> update(@PathVariable int id, @RequestBody Membre membre) {
		membre.setId(id);
		return ResponseEntity.ok(membreService.save(membre));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable int id) {
		membreService.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/join-group")
	public ResponseEntity<Void> joinGroup(
			@RequestParam int userId,
			@RequestParam int familleId) {
		membreService.joinGroup(userId, familleId);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/leave-group/{membreId}")
	public ResponseEntity<Void> leaveGroup(@PathVariable int membreId) {
		membreService.leaveGroup(membreId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/role/{membreId}")
	public ResponseEntity<String> getRole(@PathVariable int membreId) {
		return ResponseEntity.ok(membreService.getRole(membreId));
	}

	@PutMapping("/change-role/{membreId}")
	public ResponseEntity<Void> changeRole(
			@PathVariable int membreId,
			@RequestParam Membre.Role role) {
		membreService.changeRole(membreId, role);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/calculate-remaining/{membreId}")
	public ResponseEntity<Double> calculateRemainingBudget(
			@PathVariable int membreId,
			@RequestParam double expenses) {
		return ResponseEntity.ok(membreService.calculateRemainingBudget(membreId, expenses));
	}

	/**
	 * À appeler le 1er de chaque mois.
	 * → carryoverBudget = remainingBudget du mois précédent
	 * → budget = 0
	 * → remainingBudget = 0
	 * 400 si ce n'est pas le 1er du mois.
	 */
	@PutMapping("/apply-carryover/{membreId}")
	public ResponseEntity<?> applyCarryover(@PathVariable int membreId) {
		try {
			membreService.applyCarryover(membreId);
			return ResponseEntity.noContent().build();
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	/**
	 * GET /membres/{id}/budget-snapshot?month=3&year=2025
	 * Retourne budget + carryover + dépenses réelles + reste pour un mois donné.
	 */
	@GetMapping("/{id}/budget-snapshot")
	public ResponseEntity<?> getBudgetSnapshot(
	        @PathVariable int id,
	        @RequestParam int month,
	        @RequestParam int year) {
	    try {
	        return ResponseEntity.ok(membreService.getBudgetSnapshot(id, month, year));
	    } catch (RuntimeException e) {
	        return ResponseEntity.notFound().build();
	    }
	}
	
	/**
	 * GET /membres/famille/{familleId}/budget-historique?month=5&year=2026
	 */
	@GetMapping("/famille/{familleId}/budget-historique")
	public ResponseEntity<?> getBudgetHistoriqueParFamille(
	        @PathVariable int familleId,
	        @RequestParam int month,
	        @RequestParam int year) {
	    try {
	        return ResponseEntity.ok(
	            membreService.getBudgetHistoriqueParFamille(familleId, month, year)
	        );
	    } catch (RuntimeException e) {
	        return ResponseEntity.notFound().build();
	    }
	}

	/**
	 * Mobile endpoint — sets the monthly budget for a member.
	 * Same effect as AdminController.setBudget but exposed under /membres
	 * for clients that already have a membreId.
	 */
	@PutMapping("/set-budget/{membreId}")
	public ResponseEntity<Membre> setBudget(
	        @PathVariable int membreId,
	        @RequestParam double budget) {
	    return ResponseEntity.ok(membreService.setBudget(membreId, budget));
	}

	/** Mobile endpoint — live monthly budget snapshot for the hero card. */
	@GetMapping("/{membreId}/budget-summary")
	public ResponseEntity<BudgetSummaryDTO> getBudgetSummary(@PathVariable int membreId) {
	    return ResponseEntity.ok(membreService.getBudgetSummary(membreId));
	}
}