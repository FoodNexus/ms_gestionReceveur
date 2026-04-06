package tn.esprit.ms_receveur.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.ms_receveur.dto.BesoinDTO;
import tn.esprit.ms_receveur.entities.Besoin;
import tn.esprit.ms_receveur.entities.Stockage;
import tn.esprit.ms_receveur.enums.StatutBesoin;
import tn.esprit.ms_receveur.repositories.BesoinRepository;
import tn.esprit.ms_receveur.repositories.StockageRepository;

import java.util.List;

@Service
@Slf4j
public class BesoinService {

    @Autowired
    private BesoinRepository besoinRepository;

    @Autowired
    private StockageRepository stockageRepository;

    @Autowired
    private StockageService stockageService;

    // ✅ CREATE - Créer un besoin
    @Transactional
    public Besoin creerBesoin(BesoinDTO dto) {
        // 1. Vérifier que le stockage existe
        Stockage stockage = stockageService.getStockageByUserId(dto.getStockageId());

        // 2. Vérifier que la capacité est suffisante
        if (stockage.getCapaciteDisponibleKg() < dto.getQuantiteKg()) {
            throw new RuntimeException("Capacité insuffisante pour ce besoin");
        }

        // 3. Créer le besoin
        Besoin besoin = new Besoin();
        besoin.setStockage(stockage);
        besoin.setTypeProduit(dto.getTypeProduit());
        besoin.setQuantiteKg(dto.getQuantiteKg());
        besoin.setDescription(dto.getDescription());
        besoin.setDateExpiration(dto.getDateExpiration());
        besoin.setStatut(StatutBesoin.EN_ATTENTE);

        Besoin saved = besoinRepository.save(besoin);
        log.info("Besoin créé pour userId: {}, type: {}", dto.getStockageId(), dto.getTypeProduit());

        return saved;
    }

    // ✅ READ ALL - Récupérer tous les besoins d'un utilisateur
    public List<Besoin> getMesBesoins(Long userId) {
        Stockage stockage = stockageService.getStockageByUserId(userId);
        return besoinRepository.findByStockage(stockage);
    }

    // ✅ READ ONE - Récupérer un besoin par son ID
    public Besoin getBesoinById(Long id) {
        return besoinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Besoin non trouvé avec id: " + id));
    }

    // ✅ UPDATE - Modifier un besoin
    @Transactional
    public Besoin modifierBesoin(Long id, BesoinDTO dto) {
        Besoin besoin = getBesoinById(id);

        // Vérifier que le besoin n'est pas déjà satisfait
        if (besoin.getStatut() == StatutBesoin.SATISFAIT) {
            throw new RuntimeException("Impossible de modifier un besoin déjà satisfait");
        }

        // Mettre à jour les champs
        if (dto.getTypeProduit() != null) {
            besoin.setTypeProduit(dto.getTypeProduit());
        }
        if (dto.getQuantiteKg() != null) {
            besoin.setQuantiteKg(dto.getQuantiteKg());
        }
        if (dto.getDescription() != null) {
            besoin.setDescription(dto.getDescription());
        }
        if (dto.getDateExpiration() != null) {
            besoin.setDateExpiration(dto.getDateExpiration());
        }

        return besoinRepository.save(besoin);
    }

    // ✅ DELETE - Supprimer un besoin
    @Transactional
    public void supprimerBesoin(Long id) {
        Besoin besoin = getBesoinById(id);

        // Vérifier que le besoin n'est pas déjà satisfait
        if (besoin.getStatut() == StatutBesoin.SATISFAIT) {
            throw new RuntimeException("Impossible de supprimer un besoin déjà satisfait");
        }

        besoinRepository.delete(besoin);
        log.info("Besoin supprimé avec id: {}", id);
    }

    // ✅ MARQUER COMME SATISFAIT (appelé par le module Matching d'Emna)
    @Transactional
    public void marquerBesoinSatisfait(Long id) {
        Besoin besoin = getBesoinById(id);
        besoin.setStatut(StatutBesoin.SATISFAIT);
        besoinRepository.save(besoin);
        log.info("Besoin marqué comme satisfait: {}", id);
    }

    // ✅ RÉCUPÉRER LES BESOINS ACTIFS (pour le module Matching d'Emna)
    public List<Besoin> getBesoinsActifs(Long userId) {
        Stockage stockage = stockageService.getStockageByUserId(userId);
        return besoinRepository.findByStockageAndStatut(stockage, StatutBesoin.EN_ATTENTE);
    }
}