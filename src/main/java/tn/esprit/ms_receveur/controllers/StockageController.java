// StockageController.java
package tn.esprit.ms_receveur.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms_receveur.dto.StockageDTO;
import tn.esprit.ms_receveur.entities.Stockage;
import tn.esprit.ms_receveur.services.StockageService;

@RestController
@RequestMapping("/internal/stockages")
public class StockageController {

    @Autowired
    private StockageService stockageService;

    // Endpoint INTERNE appelé par ms_gestionUser
    @PostMapping("/init/{userId}")
    public ResponseEntity<Stockage> initStockage(@PathVariable Long userId) {
        Stockage stockage = stockageService.creerStockageParDefaut(userId);
        return ResponseEntity.ok(stockage);
    }
    @PostMapping("/test/init/{userId}")
    public ResponseEntity<Stockage> testInitStockage(@PathVariable Long userId) {
        Stockage stockage = stockageService.creerStockageParDefaut(userId);
        return ResponseEntity.ok(stockage);
    }
    @GetMapping("/{userId}")
    public ResponseEntity<Stockage> getStockage(@PathVariable Long userId) {
        Stockage stockage = stockageService.getStockageByUserId(userId);
        return ResponseEntity.ok(stockage);
    }
    // StockageController.java - Ajoutez cette méthode

    @PutMapping("/{userId}")
    public ResponseEntity<Stockage> updateStockage(
            @PathVariable Long userId,
            @RequestBody StockageDTO stockageDTO) {
        Stockage stockage = stockageService.updateStockage(userId, stockageDTO);
        return ResponseEntity.ok(stockage);
    }
}