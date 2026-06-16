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

import tn.educanet.backend.persistence.Compte;
import tn.educanet.backend.persistence.Famille;
import tn.educanet.backend.persistence.Membre;
import tn.educanet.backend.service.FamilleService;

@RestController
@RequestMapping("/familles")
public class FamilleController {

	@Autowired
	private FamilleService familleService;

	@GetMapping
	public ResponseEntity<List<Famille>> findAll() {
		return ResponseEntity.ok(familleService.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Famille> findById(@PathVariable int id) {
		return familleService.findById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/creator/{creatorId}")
	public ResponseEntity<List<Famille>> findByCreatorId(@PathVariable int creatorId) {
		return ResponseEntity.ok(familleService.findByCreatorId(creatorId));
	}

	@PostMapping
	public ResponseEntity<Famille> save(@RequestBody Famille famille) {
		return ResponseEntity.ok(familleService.save(famille));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Famille> update(@PathVariable int id, @RequestBody Famille famille) {
		famille.setId(id);
		return ResponseEntity.ok(familleService.save(famille));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable int id) {
		familleService.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/nom/{familleId}")
	public ResponseEntity<String> getNom(@PathVariable int familleId) {
		return ResponseEntity.ok(familleService.getNom(familleId));
	}

	@GetMapping("/membres/{familleId}")
	public ResponseEntity<List<Membre>> getListeMembres(@PathVariable int familleId) {
		return ResponseEntity.ok(familleService.getListeMembres(familleId));
	}

	@GetMapping("/compte/{familleId}")
	public ResponseEntity<Compte> getCompte(@PathVariable int familleId) {
		return ResponseEntity.ok(familleService.getCompte(familleId));
	}

}