// StockageService.java
package tn.esprit.ms_receveur.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.ms_receveur.dto.StockageDTO;
import tn.esprit.ms_receveur.entities.Stockage;
import tn.esprit.ms_receveur.repositories.StockageRepository;

@Service
@Slf4j
public class StockageService {

    @Autowired
    private StockageRepository stockageRepository;

    // Création automatique du stockage avec valeurs par défaut
    @Transactional
    public Stockage creerStockageParDefaut(Long userId) {
        // Vérifier si le stockage n'existe pas déjà
        if (stockageRepository.existsByUserId(userId)) {
            log.warn("Stockage déjà existant pour userId: {}", userId);
            return stockageRepository.findByUserId(userId).get();
        }

        // Création avec valeurs par défaut
        Stockage stockage = Stockage.builder()
                .userId(userId)
                .capaciteMaxKg(300.0)           // 300kg par défaut
                .capaciteDisponibleKg(300.0)    // identique à la capacité max
                .aFrigorifique(false)           // pas de frigo par défaut
                .capaciteFrigoKg(0.0)           // pas de capacité frigo
                .build();

        Stockage saved = stockageRepository.save(stockage);
        log.info("Stockage créé pour userId: {} avec capacité: {}kg", userId, saved.getCapaciteMaxKg());

        return saved;
    }
    public Stockage getStockageByUserId(Long userId) {
        return stockageRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Stockage non trouvé pour userId: " + userId));
    }
    // StockageService.java - Ajoutez cette méthode

    @Transactional
    public Stockage updateStockage(Long userId, StockageDTO dto) {
        // 1. Récupérer le stockage existant
        Stockage stockage = getStockageByUserId(userId);

        // 2. Mettre à jour la capacité maximale
        if (dto.getCapaciteMaxKg() != null) {
            Double ancienneCapacite = stockage.getCapaciteMaxKg();
            stockage.setCapaciteMaxKg(dto.getCapaciteMaxKg());

            // Ajuster la capacité disponible si nécessaire
            Double espaceUtilise = ancienneCapacite - stockage.getCapaciteDisponibleKg();
            stockage.setCapaciteDisponibleKg(dto.getCapaciteMaxKg() - espaceUtilise);
        }

        // 3. Mettre à jour les informations de frigo
        if (dto.getAFrigorifique() != null) {
            stockage.setAFrigorifique(dto.getAFrigorifique());
        }

        if (dto.getCapaciteFrigoKg() != null) {
            stockage.setCapaciteFrigoKg(dto.getCapaciteFrigoKg());
        }

        // 4. Sauvegarder
        log.info("Stockage mis à jour pour userId: {}", userId);
        return stockageRepository.save(stockage);
    }
}