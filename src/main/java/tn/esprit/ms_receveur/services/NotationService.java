// NotationService.java
package tn.esprit.ms_receveur.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.ms_receveur.dto.NotationDTO;
import tn.esprit.ms_receveur.entities.Notation;
import tn.esprit.ms_receveur.entities.Stockage;
import tn.esprit.ms_receveur.repositories.NotationRepository;
import tn.esprit.ms_receveur.repositories.StockageRepository;
import java.util.List;

@Service
@Slf4j
public class NotationService {

    @Autowired
    private NotationRepository notationRepository;

    @Autowired
    private StockageRepository stockageRepository;

    @Autowired
    private StockageService stockageService;

    // ✅ CREATE - Ajouter une notation
    @Transactional
    public Notation ajouterNotation(NotationDTO dto) {
        // 1. Vérifier que la note est valide
        if (dto.getNote() < 1 || dto.getNote() > 5) {
            throw new RuntimeException("La note doit être entre 1 et 5");
        }

        // 2. Vérifier que le stockage existe
        Stockage stockage = stockageService.getStockageByUserId(dto.getStockageId());

        // 3. Vérifier que ce don n'a pas déjà été noté
        if (notationRepository.existsByStockageIdAndDonId(dto.getStockageId(), dto.getDonId())) {
            throw new RuntimeException("Ce don a déjà été noté");
        }

        // 4. Créer la notation
        Notation notation = Notation.builder()
                .stockage(stockage)
                .donId(dto.getDonId())
                .note(dto.getNote())
                .commentaire(dto.getCommentaire())
                .build();

        Notation saved = notationRepository.save(notation);
        log.info("Notation ajoutée pour userId: {}, donId: {}, note: {}",
                dto.getStockageId(), dto.getDonId(), dto.getNote());

        return saved;
    }

    // ✅ READ ALL - Récupérer toutes les notations d'un utilisateur
    public List<Notation> getMesNotations(Long userId) {
        Stockage stockage = stockageService.getStockageByUserId(userId);
        return notationRepository.findByStockage(stockage);
    }

    // ✅ READ ONE - Récupérer une notation par son ID
    public Notation getNotationById(Long id) {
        return notationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notation non trouvée avec id: " + id));
    }

    // ✅ SCORE MOYEN - Calculer le score moyen d'une association
    public Double getScoreMoyen(Long userId) {
        Stockage stockage = stockageService.getStockageByUserId(userId);
        Double score = notationRepository.calculerScoreMoyen(stockage);
        return score != null ? score : 0.0;
    }

    // ✅ NOMBRE DE NOTATIONS
    public Long getNombreNotations(Long userId) {
        Stockage stockage = stockageService.getStockageByUserId(userId);
        return notationRepository.countByStockage(stockage);
    }
}