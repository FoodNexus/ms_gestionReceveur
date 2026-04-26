// DataAnalysisService.java (version modifiée)
package tn.esprit.ms_receveur.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.ms_receveur.entities.Besoin;
import tn.esprit.ms_receveur.entities.Stockage;
import tn.esprit.ms_receveur.repositories.BesoinRepository;
import tn.esprit.ms_receveur.repositories.StockageRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataAnalysisService {

    private final BesoinRepository besoinRepository;
    private final StockageRepository stockageRepository;  // ← AJOUTER

    public DataAnalysisService(BesoinRepository besoinRepository,
                               StockageRepository stockageRepository) {  // ← MODIFIER
        this.besoinRepository = besoinRepository;
        this.stockageRepository = stockageRepository;
    }

    /**
     * Analyse l'historique des besoins d'une association
     */
    public Map<String, Object> analyserHistorique(Long userId) {
        // 1. Récupérer le stockage de l'utilisateur
        Stockage stockage = stockageRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Stockage non trouvé pour userId: " + userId));

        // 2. Récupérer les besoins via le stockage
        List<Besoin> historique = besoinRepository.findByStockage(stockage);  // ← Utiliser findByStockage

        Map<String, Object> analyse = new HashMap<>();

        // 1. Nombre total de besoins
        analyse.put("totalBesoins", historique.size());

        // 2. Produits les plus demandés
        Map<String, Long> produitsFrequence = historique.stream()
                .collect(Collectors.groupingBy(Besoin::getTypeProduit, Collectors.counting()));
        analyse.put("produitsFrequence", produitsFrequence);

        // 3. Quantité moyenne par produit
        Map<String, Double> quantitesMoyennes = historique.stream()
                .collect(Collectors.groupingBy(
                        Besoin::getTypeProduit,
                        Collectors.averagingDouble(Besoin::getQuantiteKg)
                ));
        analyse.put("quantitesMoyennes", quantitesMoyennes);

        // 4. Fréquence des besoins (jours entre deux besoins)
        double frequenceMoyenne = calculerFrequenceMoyenne(historique);
        analyse.put("frequenceMoyenneJours", frequenceMoyenne);

        // 5. Saisonnalité (besoins par mois)
        Map<Integer, Long> besoinsParMois = historique.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getDateCreation().getMonthValue(),
                        Collectors.counting()
                ));
        analyse.put("besoinsParMois", besoinsParMois);

        log.info("Analyse historique pour userId {}: {} besoins", userId, historique.size());
        return analyse;
    }

    private double calculerFrequenceMoyenne(List<Besoin> besoins) {
        if (besoins.size() < 2) return 14; // 14 jours par défaut

        List<LocalDateTime> dates = besoins.stream()
                .map(Besoin::getDateCreation)
                .sorted()
                .collect(Collectors.toList());

        long totalJours = 0;
        for (int i = 1; i < dates.size(); i++) {
            totalJours += ChronoUnit.DAYS.between(dates.get(i-1), dates.get(i));
        }

        return (double) totalJours / (dates.size() - 1);
    }
}