// NotationController.java
package tn.esprit.ms_receveur.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms_receveur.dto.NotationDTO;
import tn.esprit.ms_receveur.entities.Notation;
import tn.esprit.ms_receveur.services.NotationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/receveur/notations")
public class NotationController {

    @Autowired
    private NotationService notationService;

    // ✅ Ajouter une notation
    @PostMapping
    public ResponseEntity<Notation> ajouterNotation(@Valid @RequestBody NotationDTO dto) {
        Notation notation = notationService.ajouterNotation(dto);
        return new ResponseEntity<>(notation, HttpStatus.CREATED);
    }

    // ✅ Lister mes notations
    @GetMapping("/utilisateur/{userId}")
    public ResponseEntity<List<Notation>> getMesNotations(@PathVariable Long userId) {
        List<Notation> notations = notationService.getMesNotations(userId);
        return ResponseEntity.ok(notations);
    }

    // ✅ Récupérer une notation par ID
    @GetMapping("/{id}")
    public ResponseEntity<Notation> getNotationById(@PathVariable Long id) {
        Notation notation = notationService.getNotationById(id);
        return ResponseEntity.ok(notation);
    }

    // ✅ Obtenir le score moyen
    @GetMapping("/score-moyen/{userId}")
    public ResponseEntity<Map<String, Object>> getScoreMoyen(@PathVariable Long userId) {
        Double score = notationService.getScoreMoyen(userId);
        Long nombre = notationService.getNombreNotations(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("scoreMoyen", score);
        response.put("nombreNotations", nombre);

        return ResponseEntity.ok(response);
    }
}