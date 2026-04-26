// PredictionService.java
package tn.esprit.ms_receveur.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.ms_receveur.dto.BesoinPredictionDTO;
import tn.esprit.ms_receveur.dto.PredictionResultDTO;
import tn.esprit.ms_receveur.entities.Besoin;
import tn.esprit.ms_receveur.entities.Stockage;
import tn.esprit.ms_receveur.repositories.BesoinRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PredictionService {

    private final BesoinRepository besoinRepository;
    private final StockageService stockageService;
    private final DataAnalysisService dataAnalysisService;

    public PredictionService(BesoinRepository besoinRepository,
                             StockageService stockageService,
                             DataAnalysisService dataAnalysisService) {
        this.besoinRepository = besoinRepository;
        this.stockageService = stockageService;
        this.dataAnalysisService = dataAnalysisService;
    }

    /**
     * Prédit les besoins futurs d'une association
     */
    public PredictionResultDTO predireBesoins(Long userId, int horizonJours) {
        log.info("Prédiction des besoins pour userId: {}, horizon: {} jours", userId, horizonJours);

        // 1. Récupérer l'historique
        List<Besoin> historique = besoinRepository.findByStockageUserId(userId);

        // 2. Si peu de données, utiliser les règles par défaut
        if (historique.size() < 10) {
            return predireAvecReglesDefaut(userId, horizonJours);
        }

        // 3. Analyser l'historique
        Map<String, Object> analyse = dataAnalysisService.analyserHistorique(userId);
        Map<String, Double> quantitesMoyennes = (Map<String, Double>) analyse.get("quantitesMoyennes");

        // 4. Récupérer le stockage pour vérifier la capacité
        Stockage stockage = stockageService.getStockageByUserId(userId);

        // 5. Générer les prédictions
        List<BesoinPredictionDTO> predictions = new ArrayList<>();

        for (Map.Entry<String, Double> entry : quantitesMoyennes.entrySet()) {
            String produit = entry.getKey();
            double quantiteMoyenne = entry.getValue();

            // Calculer la date du prochain besoin
            LocalDate derniereDate = getDerniereDateBesoin(historique, produit);
            double frequence = getFrequenceBesoin(historique, produit);
            LocalDate datePrevue = derniereDate.plusDays((long) frequence);

            // Vérifier si la date est dans l'horizon
            if (datePrevue.isBefore(LocalDate.now().plusDays(horizonJours))) {

                // Ajuster selon la capacité disponible
                double quantiteMax = Math.min(quantiteMoyenne, stockage.getCapaciteDisponibleKg() * 0.7);

                // Ajuster selon la saison
                double coefficientSaison = getCoefficientSaison();
                quantiteMax = quantiteMax * coefficientSaison;

                // Déterminer l'urgence
                String urgence = determinerUrgence(datePrevue);

                predictions.add(new BesoinPredictionDTO(
                        produit,
                        Math.round(quantiteMax * 10) / 10.0,
                        datePrevue,
                        urgence,
                        getRaison(produit, quantiteMoyenne, frequence)
                ));
            }
        }

        // 6. Calculer le gain estimé
        double gainEstime = calculerGainEstime(predictions);

        // 7. Déterminer le niveau de confiance
        String niveauConfiance = historique.size() > 50 ? "ELEVE" : (historique.size() > 20 ? "MOYEN" : "FAIBLE");

        PredictionResultDTO result = new PredictionResultDTO();
        result.setPredictions(predictions);
        result.setNiveauConfiance(niveauConfiance);
        result.setGainEstimeKg(gainEstime);

        return result;
    }

    /**
     * Mode dégradé : règles par défaut (quand peu de données)
     */
    private PredictionResultDTO predireAvecReglesDefaut(Long userId, int horizonJours) {
        log.info("Utilisation des règles par défaut pour userId: {}", userId);

        // Produits de base par défaut
        String[] produitsDefaut = {"Lait", "Pain", "Légumes", "Pâtes"};
        double[] quantitesDefaut = {30, 20, 50, 40};

        List<BesoinPredictionDTO> predictions = new ArrayList<>();

        for (int i = 0; i < produitsDefaut.length; i++) {
            predictions.add(new BesoinPredictionDTO(
                    produitsDefaut[i],
                    quantitesDefaut[i],
                    LocalDate.now().plusDays(7),
                    "NORMAL",
                    "Basé sur les besoins standards des associations"
            ));
        }

        PredictionResultDTO result = new PredictionResultDTO();
        result.setPredictions(predictions);
        result.setNiveauConfiance("FAIBLE");
        result.setGainEstimeKg(calculerGainEstime(predictions));

        return result;
    }

    private LocalDate getDerniereDateBesoin(List<Besoin> historique, String produit) {
        return historique.stream()
                .filter(b -> b.getTypeProduit().equals(produit))
                .map(Besoin::getDateCreation)
                .map(LocalDateTime::toLocalDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now().minusDays(30));
    }

    private double getFrequenceBesoin(List<Besoin> historique, String produit) {
        List<Besoin> besoinsProduit = historique.stream()
                .filter(b -> b.getTypeProduit().equals(produit))
                .sorted(Comparator.comparing(Besoin::getDateCreation))
                .collect(Collectors.toList());

        if (besoinsProduit.size() < 2) return 14; // 14 jours par défaut

        long totalJours = 0;
        for (int i = 1; i < besoinsProduit.size(); i++) {
            totalJours += ChronoUnit.DAYS.between(
                    besoinsProduit.get(i-1).getDateCreation(),
                    besoinsProduit.get(i).getDateCreation()
            );
        }

        return (double) totalJours / (besoinsProduit.size() - 1);
    }

    private double getCoefficientSaison() {
        int mois = LocalDate.now().getMonthValue();
        // Été : plus de besoins, Hiver : moins
        if (mois >= 6 && mois <= 8) return 1.2;
        if (mois >= 11 || mois <= 1) return 0.9;
        return 1.0;
    }

    private String determinerUrgence(LocalDate datePrevue) {
        long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), datePrevue);
        if (joursRestants <= 3) return "CRITIQUE";
        if (joursRestants <= 7) return "URGENT";
        return "NORMAL";
    }

    private String getRaison(String produit, double moyenne, double frequence) {
        if (frequence < 7) {
            return "Besoin fréquent (tous les " + (int) frequence + " jours)";
        }
        return "Besoin régulier basé sur l'historique (" + (int) moyenne + " kg en moyenne)";
    }

    private double calculerGainEstime(List<BesoinPredictionDTO> predictions) {
        // Estimation : chaque besoin prédit évite 15% de gaspillage
        double totalKg = predictions.stream()
                .mapToDouble(BesoinPredictionDTO::getQuantitePrediteKg)
                .sum();
        return Math.round(totalKg * 0.15 * 10) / 10.0;
    }
}