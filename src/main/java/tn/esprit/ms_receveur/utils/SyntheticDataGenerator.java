// SyntheticDataGenerator.java
package tn.esprit.ms_receveur.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tn.esprit.ms_receveur.entities.Besoin;
import tn.esprit.ms_receveur.entities.Stockage;
import tn.esprit.ms_receveur.enums.StatutBesoin;
import tn.esprit.ms_receveur.repositories.BesoinRepository;
import tn.esprit.ms_receveur.repositories.StockageRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Component
@Slf4j
public class SyntheticDataGenerator {

    private final Random random = new Random();
    private final String[] produits = {"Lait", "Pâtes", "Légumes", "Pain", "Œufs", "Fruits"};
    private final double[] quantitesBase = {50, 80, 60, 30, 20, 40};

    private final BesoinRepository besoinRepository;
    private final StockageRepository stockageRepository;

    public SyntheticDataGenerator(BesoinRepository besoinRepository, StockageRepository stockageRepository) {
        this.besoinRepository = besoinRepository;
        this.stockageRepository = stockageRepository;
    }

    /**
     * Génère des données synthétiques pour les tests
     */
    public void genererDonneesTest(Long userId, int nbBesoins) {
        // Vérifier si le stockage existe
        Stockage stockage = stockageRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Stockage non trouvé"));

        for (int i = 0; i < nbBesoins; i++) {
            int produitIndex = random.nextInt(produits.length);

            Besoin besoin = new Besoin();
            besoin.setStockage(stockage);
            besoin.setTypeProduit(produits[produitIndex]);
            besoin.setQuantiteKg(quantitesBase[produitIndex] + random.nextDouble() * 30);
            besoin.setDescription("Besoin généré automatiquement pour test");
            besoin.setDateExpiration(LocalDate.now().plusDays(random.nextInt(30) + 7));
            besoin.setStatut(StatutBesoin.EN_ATTENTE);
            besoin.setDateCreation(LocalDateTime.now().minusDays(random.nextInt(365)));

            besoinRepository.save(besoin);
        }

        log.info("Généré {} besoins synthétiques pour userId {}", nbBesoins, userId);
    }
}