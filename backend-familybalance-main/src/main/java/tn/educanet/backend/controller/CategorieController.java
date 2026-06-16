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

import tn.educanet.backend.persistence.Categorie;
import tn.educanet.backend.service.CategorieService;

@RestController
@RequestMapping("/categories")
public class CategorieController {

    @Autowired
    private CategorieService categorieService;

    @GetMapping
    public ResponseEntity<List<Categorie>> findAll() {
        return ResponseEntity.ok(categorieService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categorie> findById(@PathVariable int id) {
        return categorieService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Categorie>> findAllByType(@PathVariable Categorie.Type type) {
        return ResponseEntity.ok(categorieService.findAllByType(type));
    }

    @GetMapping("/default")
    public ResponseEntity<List<Categorie>> findDefaultCategories() {
        return ResponseEntity.ok(categorieService.findDefaultCategories());
    }

    @GetMapping("/default/type/{type}")
    public ResponseEntity<List<Categorie>> findDefaultByType(@PathVariable Categorie.Type type) {
        return ResponseEntity.ok(categorieService.findDefaultCategoriesByType(type));
    }

    @GetMapping("/custom/user/{userId}")
    public ResponseEntity<List<Categorie>> findCustomByUser(@PathVariable int userId) {
        return ResponseEntity.ok(categorieService.findCustomCategoriesByUser(userId));
    }

    @PostMapping("/custom/{userId}")
    public ResponseEntity<Categorie> addCustomCategory(
            @PathVariable int userId,
            @RequestBody Categorie categorie) {
        return ResponseEntity.ok(categorieService.addCustomCategory(categorie, userId));
    }

    @PostMapping
    public ResponseEntity<Categorie> save(@RequestBody Categorie categorie) {
        return ResponseEntity.ok(categorieService.save(categorie));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categorie> update(@PathVariable int id, @RequestBody Categorie categorie) {
        categorie.setId(id);
        return ResponseEntity.ok(categorieService.save(categorie));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable int id) {
        categorieService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}