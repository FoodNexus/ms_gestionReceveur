// PredictionController.java
package tn.esprit.ms_receveur.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms_receveur.dto.PredictionDTO;
import tn.esprit.ms_receveur.dto.PredictionResultDTO;
import tn.esprit.ms_receveur.services.PredictionService;
import tn.esprit.ms_receveur.utils.SyntheticDataGenerator;

import java.util.List;

@RestController
@RequestMapping("/api/receveur/prediction")
@Slf4j
public class PredictionController {

    private final PredictionService predictionService;
    private final SyntheticDataGenerator dataGenerator;

    public PredictionController(PredictionService predictionService,
                                SyntheticDataGenerator dataGenerator) {
        this.predictionService = predictionService;
        this.dataGenerator = dataGenerator;
    }

    /**
     * Prédire les besoins futurs
     */
    @PostMapping("/predict")
    public ResponseEntity<PredictionResultDTO> predireBesoins(@RequestBody PredictionDTO dto) {
        log.info("Requête de prédiction pour userId: {}, horizon: {} jours",
                dto.getUserId(), dto.getHorizonJours());

        PredictionResultDTO result = predictionService.predireBesoins(
                dto.getUserId(),
                dto.getHorizonJours()
        );

        return ResponseEntity.ok(result);
    }

    /**
     * Générer des données de test (pour démo)
     */
    @PostMapping("/generate-test-data/{userId}")
    public ResponseEntity<String> genererDonneesTest(@PathVariable Long userId,
                                                     @RequestParam(defaultValue = "50") int nbBesoins) {
        dataGenerator.genererDonneesTest(userId, nbBesoins);
        return ResponseEntity.ok("Généré " + nbBesoins + " besoins pour userId " + userId);
    }

    /**
     * Obtenir des recommandations basées sur les prédictions
     */
    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<List<String>> getRecommendations(@PathVariable Long userId) {
        List<String> recommendations = List.of(
                "📦 Prévoyez un besoin de lait dans les prochains jours",
                "🥖 La demande de pain augmente en fin de semaine",
                "📊 Votre taux de satisfaction est excellent, continuez ainsi !"
        );
        return ResponseEntity.ok(recommendations);
    }
}