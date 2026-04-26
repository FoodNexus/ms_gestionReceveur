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
        if (stockageRepository.existsByUserId(userId)) {
            log.warn("Stockage déjà existant pour userId: {}", userId);
            return stockageRepository.findByUserId(userId).get();
        }

        Stockage stockage = Stockage.builder()
                .userId(userId)
                .capaciteMaxKg(300.0)
                .capaciteDisponibleKg(300.0)
                .aFrigorifique(false)
                .capaciteFrigoKg(0.0)
                .build();

        Stockage saved = stockageRepository.save(stockage);
        log.info("Stockage créé pour userId: {} avec capacité: {}kg", userId, saved.getCapaciteMaxKg());
        return saved;
    }

    public Stockage getStockageByUserId(Long userId) {
        return stockageRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Stockage non trouvé pour userId: " + userId));
    }

    @Transactional
    public Stockage updateStockage(Long userId, StockageDTO dto) {
        Stockage stockage = getStockageByUserId(userId);

        // 1. Mettre à jour la capacité maximale
        if (dto.getCapaciteMaxKg() != null) {
            Double ancienneCapacite = stockage.getCapaciteMaxKg();
            stockage.setCapaciteMaxKg(dto.getCapaciteMaxKg());
            Double espaceUtilise = ancienneCapacite - stockage.getCapaciteDisponibleKg();
            stockage.setCapaciteDisponibleKg(dto.getCapaciteMaxKg() - espaceUtilise);
        }

        // 2. Mettre à jour les informations de frigo
        if (dto.getAFrigorifique() != null) {
            stockage.setAFrigorifique(dto.getAFrigorifique());

            // ✅ AJOUT : Si le frigo est désactivé, la capacité frigo passe à 0
            if (!dto.getAFrigorifique()) {
                stockage.setCapaciteFrigoKg(0.0);
                log.info("Frigo désactivé pour userId: {}, capacité frigo mise à 0", userId);
            }
        }

        // 3. Mettre à jour la capacité frigo (seulement si frigo présent)
        if (dto.getCapaciteFrigoKg() != null && stockage.getAFrigorifique()) {
            stockage.setCapaciteFrigoKg(dto.getCapaciteFrigoKg());
        }

        // 4. Sauvegarder
        Stockage saved = stockageRepository.save(stockage);
        log.info("Stockage mis à jour pour userId: {}", userId);
        log.info("Nouvel état - aFrigorifique: {}, capaciteFrigoKg: {}",
                saved.getAFrigorifique(), saved.getCapaciteFrigoKg());

        return saved;
    }
}