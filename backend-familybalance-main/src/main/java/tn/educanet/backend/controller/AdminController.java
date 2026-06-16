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

import tn.educanet.backend.persistence.Admin;
import tn.educanet.backend.persistence.Famille;
import tn.educanet.backend.persistence.Membre;
import tn.educanet.backend.service.AdminService;

@RestController
@RequestMapping("/admins")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping
    public ResponseEntity<List<Admin>> findAll() {
        return ResponseEntity.ok(adminService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> findById(@PathVariable int id) {
        return adminService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Admin> findByUserId(@PathVariable int userId) {
        return adminService.findFirstByUser_Id(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/famille/{familleId}")
    public ResponseEntity<Admin> findByFamilleId(@PathVariable int familleId) {
        return adminService.findFirstByFamille_Id(familleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Admin> save(@RequestBody Admin admin) {
        return ResponseEntity.ok(adminService.save(admin));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Admin> update(@PathVariable int id, @RequestBody Admin admin) {
        admin.setId(id);
        return ResponseEntity.ok(adminService.save(admin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable int id) {
        adminService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create-family")
    public ResponseEntity<Famille> createFamily(
            @RequestParam String name,
            @RequestParam int creatorId) {
        return ResponseEntity.ok(adminService.createFamily(name, creatorId));
    }

    @DeleteMapping("/delete-family/{familleId}")
    public ResponseEntity<Void> deleteFamily(@PathVariable int familleId) {
        adminService.deleteFamily(familleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invite-member")
    public ResponseEntity<Void> inviteMember(
            @RequestParam int userId,
            @RequestParam int familleId) {
        adminService.inviteMember(userId, familleId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/remove-member/{membreId}")
    public ResponseEntity<Void> removeMember(@PathVariable int membreId) {
        adminService.removeMember(membreId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/change-role/{membreId}")
    public ResponseEntity<Void> changeMemberRole(
            @PathVariable int membreId,
            @RequestParam Membre.Role role) {
        adminService.changeMemberRole(membreId, role);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/set-budget/{membreId}")
    public ResponseEntity<Void> setBudget(
            @PathVariable int membreId,
            @RequestParam double budget) {
        adminService.setBudget(membreId, budget);
        return ResponseEntity.noContent().build();
    }

    // ── FIX #5 : Reset budgets au début d'un nouveau mois ──────────────────
    @PostMapping("/reset-budgets-new-month")
    public ResponseEntity<Void> resetBudgetsNewMonth() {
        adminService.resetBudgetsForNewMonth();
        return ResponseEntity.noContent().build();
    }
}