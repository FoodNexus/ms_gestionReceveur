package tn.esprit.ms_receveur.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms_receveur.entities.Alerte;
import tn.esprit.ms_receveur.services.AlerteService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/receveur/alertes")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AlerteController {

    private final AlerteService alerteService;

    @GetMapping("/non-lues/{userId}")
    public ResponseEntity<List<Alerte>> getNonLues(@PathVariable Long userId) {
        return ResponseEntity.ok(alerteService.getAlertesNonLues(userId));
    }

    @GetMapping("/all/{userId}")
    public ResponseEntity<List<Alerte>> getAll(@PathVariable Long userId) {
        return ResponseEntity.ok(alerteService.getAllAlertes(userId));
    }

    @GetMapping("/count/{userId}")
    public ResponseEntity<Long> countNonLues(@PathVariable Long userId) {
        return ResponseEntity.ok(alerteService.countNonLues(userId));
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable Long userId) {
        return ResponseEntity.ok(alerteService.getStats(userId));
    }

    @PostMapping("/lue/{id}")
    public ResponseEntity<Void> marquerLue(@PathVariable Long id) {
        alerteService.marquerCommeLue(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tout-lu/{userId}")
    public ResponseEntity<Void> marquerToutLu(@PathVariable Long userId) {
        alerteService.marquerToutLu(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generer")
    public ResponseEntity<String> genererAlertes() {
        alerteService.genererAlertesAutomatiques();
        return ResponseEntity.ok("Alertes générées");
    }

    @PostMapping("/nettoyer")
    public ResponseEntity<Integer> nettoyerAnciennes() {
        int count = alerteService.nettoyerAnciennesAlertes();
        return ResponseEntity.ok(count);
    }
}