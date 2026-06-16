package tn.educanet.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import tn.educanet.backend.persistence.Facture;
import tn.educanet.backend.service.FactureService;

import java.io.IOException;
import java.nio.file.*;

@RestController
@RequestMapping("/factures")
public class FactureController {

    @Autowired
    private FactureService factureService;

    private static final String UPLOAD_DIR =
        System.getProperty("user.home") + "/factures-uploads/";

    @GetMapping
    public ResponseEntity<List<Facture>> findAll() {
        return ResponseEntity.ok(factureService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Facture> findById(@PathVariable int id) {
        return factureService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/membre/{membreId}")
    public ResponseEntity<List<Facture>> findByMembreId(@PathVariable int membreId) {
        return ResponseEntity.ok(factureService.findByMembreId(membreId));
    }

    @GetMapping("/compte/{compteId}")
    public ResponseEntity<List<Facture>> findByCompteId(@PathVariable int compteId) {
        return ResponseEntity.ok(factureService.findByCompteId(compteId));
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Facture>> findByStatut(@PathVariable Facture.Statut statut) {
        return ResponseEntity.ok(factureService.findByStatut(statut));
    }

    @PostMapping("/add")
    public ResponseEntity<Facture> add(@RequestBody Facture facture) {
        return ResponseEntity.ok(factureService.add(facture));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<Facture> edit(
            @PathVariable int id,
            @RequestBody Facture facture) {
        return ResponseEntity.ok(factureService.edit(id, facture));
    }

    @PutMapping("/upload/{id}")
    public ResponseEntity<Facture> upload(
            @PathVariable int id,
            @RequestParam("file") MultipartFile file) throws IOException {

        Files.createDirectories(Paths.get(UPLOAD_DIR));

        String originalName = file.getOriginalFilename();
        String filename = System.currentTimeMillis() + "_" + originalName;
        Files.write(Paths.get(UPLOAD_DIR + filename), file.getBytes());

        // Stored URL points to the /factures/files/ endpoint (Angular flow).
        String fileUrl = "/factures/files/" + filename;
        return ResponseEntity.ok(factureService.upload(id, fileUrl, originalName));
    }

    /**
     * URL-based attach used by the React Native client: the mobile app
     * uploads the file to its own storage and sends the resulting URL.
     */
    @PutMapping("/upload-url/{id}")
    public ResponseEntity<Facture> uploadByUrl(
            @PathVariable int id,
            @RequestParam String fileUrl) {
        return ResponseEntity.ok(factureService.upload(id, fileUrl));
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) throws IOException {
        Path path = Paths.get(UPLOAD_DIR + filename);
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        // Détection automatique du type : jpg, png, gif, webp, pdf, etc.
        String contentType = Files.probeContentType(path);
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @PutMapping("/mark-as-paid/{id}")
    public ResponseEntity<Void> markAsPaid(@PathVariable int id) {
        factureService.markAsPaid(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/update-overdue")
    public ResponseEntity<String> updateOverdue() {
        int count = factureService.updateOverdueFactures();
        return ResponseEntity.ok(count + " facture(s) mise(s) EN_RETARD.");
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        factureService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/membre/{membreId}/total/mois")
    public ResponseEntity<Double> getTotalThisMonth(@PathVariable int membreId) {
        return ResponseEntity.ok(factureService.getTotalByMembreThisMonth(membreId));
    }

    @GetMapping("/membre/{membreId}/total/annee")
    public ResponseEntity<Double> getTotalThisYear(@PathVariable int membreId) {
        return ResponseEntity.ok(factureService.getTotalByMembreThisYear(membreId));
    }

    @GetMapping("/compte/{compteId}/total/mois")
    public ResponseEntity<Double> getTotalByCompteThisMonth(@PathVariable int compteId) {
        return ResponseEntity.ok(factureService.getTotalByCompteThisMonth(compteId));
    }

    @GetMapping("/famille/{familleId}/count")
    public ResponseEntity<Long> countByFamille(@PathVariable int familleId) {
        return ResponseEntity.ok(factureService.countByFamille(familleId));
    }
}