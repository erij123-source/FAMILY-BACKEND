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

import tn.educanet.backend.persistence.Notification;
import tn.educanet.backend.service.NotificationService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // ── Listings ──────────────────────────────────────────────────────────
    /** Listing global de la famille (kept for back-compat / admin only). */
    @GetMapping("/famille/{familleId}")
    public ResponseEntity<List<Notification>> findByFamille(@PathVariable int familleId) {
        return ResponseEntity.ok(notificationService.findByFamille(familleId));
    }

    /** Endpoint principal — listing du mois courant pour un membre donné.
     *  L'historique est vidé chaque 1er du mois (cf. scheduler + filtre). */
    @GetMapping("/membre/{membreId}")
    public ResponseEntity<List<Notification>> findByMembre(@PathVariable int membreId) {
        return ResponseEntity.ok(notificationService.findByMembreCurrentMonth(membreId));
    }

    @GetMapping("/membre/{membreId}/unread-count")
    public ResponseEntity<Long> countUnreadForMembre(@PathVariable int membreId) {
        return ResponseEntity.ok(notificationService.countUnreadForMembre(membreId));
    }

    @PostMapping
    public ResponseEntity<Notification> add(@RequestBody Notification notification) {
        return ResponseEntity.ok(notificationService.add(notification));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> setRead(
            @PathVariable int id,
            @RequestParam(defaultValue = "true") boolean read) {
        return ResponseEntity.ok(notificationService.setRead(id, read));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** "Tout marquer lu" — scope membre. */
    @PutMapping("/membre/{membreId}/mark-all-read")
    public ResponseEntity<Integer> markAllReadForMembre(@PathVariable int membreId) {
        return ResponseEntity.ok(notificationService.markAllReadForMembre(membreId));
    }

    /** "Supprimer les lues" — scope membre. */
    @DeleteMapping("/membre/{membreId}/clear-read")
    public ResponseEntity<Integer> clearReadForMembre(@PathVariable int membreId) {
        return ResponseEntity.ok(notificationService.clearReadForMembre(membreId));
    }

    /** Génération idempotente — déclenchée par le front au reload / focus. */
    @PostMapping("/famille/{familleId}/generate")
    public ResponseEntity<Integer> generate(@PathVariable int familleId) {
        return ResponseEntity.ok(notificationService.generateForFamille(familleId));
    }
}
