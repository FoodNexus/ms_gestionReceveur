package tn.esprit.ms_receveur.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms_receveur.services.CleanupSchedulerService;

@RestController
@RequestMapping("/api/receveur/cleanup")
@RequiredArgsConstructor
public class CleanupController {

    private final CleanupSchedulerService cleanupService;

    @PostMapping("/run")
    public ResponseEntity<String> runCleanup() {
        cleanupService.marquerBesoinsExpires();
        return ResponseEntity.ok("✅ Nettoyage des besoins expirés exécuté");
    }

    @PostMapping("/delete-old")
    public ResponseEntity<String> deleteOld() {
        cleanupService.supprimerBesoinsAnciens();
        return ResponseEntity.ok("✅ Suppression des anciens besoins exécutée");
    }

    @PostMapping("/manual")
    public ResponseEntity<CleanupSchedulerService.CleanupResult> manualCleanup() {
        var result = cleanupService.nettoyageManuel();
        return ResponseEntity.ok(result);
    }
}