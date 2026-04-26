package tn.esprit.ms_receveur.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms_receveur.services.IARecommendationService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/receveur/ia")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200") // Autorise Angular
public class IAController {

    private final IARecommendationService iaService;

    @GetMapping("/recommend")
    public ResponseEntity<IARecommendationService.RecommendationResult> recommend(
            @RequestParam int nbPersonnes,
            @RequestParam String region,
            @RequestParam(defaultValue = "Mixte") String tranchesAge,
            @RequestParam(defaultValue = "Mixte") String repartitionSexe) {

        return ResponseEntity.ok(iaService.getRecommendations(nbPersonnes, region, tranchesAge, repartitionSexe));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("iaAvailable", iaService.isIAAvailable());
        return ResponseEntity.ok(status);
    }
}
