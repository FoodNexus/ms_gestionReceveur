package tn.esprit.ms_receveur.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms_receveur.dto.BesoinDTO;
import tn.esprit.ms_receveur.entities.Besoin;
import tn.esprit.ms_receveur.services.BesoinService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/receveur/besoins")
public class BesoinController {

    @Autowired
    private BesoinService besoinService;

    // ✅ Créer un besoin (UNE SEULE MÉTHODE POST)
    @PostMapping
    public ResponseEntity<Besoin> creerBesoin(@Valid @RequestBody BesoinDTO dto) {
        Besoin besoin = besoinService.creerBesoin(dto);
        return new ResponseEntity<>(besoin, HttpStatus.CREATED);
    }

    // ✅ Lister mes besoins
    @GetMapping("/utilisateur/{userId}")
    public ResponseEntity<List<Besoin>> getMesBesoins(@PathVariable Long userId) {
        List<Besoin> besoins = besoinService.getMesBesoins(userId);
        return ResponseEntity.ok(besoins);
    }

    // ✅ Récupérer un besoin par ID
    @GetMapping("/{id}")
    public ResponseEntity<Besoin> getBesoinById(@PathVariable Long id) {
        Besoin besoin = besoinService.getBesoinById(id);
        return ResponseEntity.ok(besoin);
    }

    // ✅ Modifier un besoin
    @PutMapping("/{id}")
    public ResponseEntity<Besoin> modifierBesoin(
            @PathVariable Long id,
            @Valid @RequestBody BesoinDTO dto) {
        Besoin besoin = besoinService.modifierBesoin(id, dto);
        return ResponseEntity.ok(besoin);
    }

    // ✅ Supprimer un besoin
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> supprimerBesoin(@PathVariable Long id) {
        besoinService.supprimerBesoin(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Besoin supprimé avec succès");
        return ResponseEntity.ok(response);
    }
}