package tn.esprit.ms_receveur.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.ms_receveur.entities.Alerte;
import tn.esprit.ms_receveur.entities.Besoin;
import tn.esprit.ms_receveur.enums.StatutBesoin;
import tn.esprit.ms_receveur.repositories.AlerteRepository;
import tn.esprit.ms_receveur.repositories.BesoinRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlerteService {

    private final AlerteRepository alerteRepository;
    private final BesoinRepository besoinRepository;

    // ========== CRÉATION D'ALERTE ==========

    @Transactional
    public Alerte creerAlerte(Long userId, Long besoinId, String typeProduit,
                              String message, String niveau, Integer joursRestants) {

        Optional<Alerte> existante = alerteRepository
                .findByUserIdAndBesoinIdAndJoursRestants(userId, besoinId, joursRestants);

        if (existante.isPresent()) {
            log.debug("Alerte déjà existante pour besoin {} - {} jours", besoinId, joursRestants);
            return null;
        }

        Alerte alerte = new Alerte();
        alerte.setUserId(userId);
        alerte.setBesoinId(besoinId);
        alerte.setTypeProduit(typeProduit);
        alerte.setMessage(message);
        alerte.setNiveau(niveau);
        alerte.setJoursRestants(joursRestants);
        alerte.setLue(false);
        alerte.setDateEnvoi(LocalDateTime.now());

        log.info("✅ Alerte créée: {} - {} (J{})", niveau, typeProduit, joursRestants);
        return alerteRepository.save(alerte);
    }

    // ========== MÉTHODES PUBLIQUES POUR LA CRÉATION IMMÉDIATE ==========

    public String determinerNiveau(int joursRestants) {
        if (joursRestants <= 0) return "CRITICAL";
        if (joursRestants == 1) return "URGENT";
        if (joursRestants <= 3) return "WARNING";
        return "INFO";
    }

    public String genererMessage(Besoin besoin, int joursRestants) {
        if (joursRestants <= 0) {
            return String.format("Votre besoin de %s (%.1f kg) est EXPIRÉ !",
                    besoin.getTypeProduit(), besoin.getQuantiteKg());
        } else if (joursRestants == 1) {
            return String.format("URGENT ! Votre besoin de %s (%.1f kg) expire DEMAIN !",
                    besoin.getTypeProduit(), besoin.getQuantiteKg());
        } else if (joursRestants <= 3) {
            return String.format("Attention ! Votre besoin de %s (%.1f kg) expire dans %d jours",
                    besoin.getTypeProduit(), besoin.getQuantiteKg(), joursRestants);
        } else {
            return String.format("Votre besoin de %s (%.1f kg) expire dans %d jours",
                    besoin.getTypeProduit(), besoin.getQuantiteKg(), joursRestants);
        }
    }

    // ========== SCHEDULER - SCAN PÉRIODIQUE (TOUTES LES HEURES) ==========

    @Scheduled(cron = "0 0 * * * *") // Toutes les heures
    @Transactional
    public void scannerBesoinsActifs() {
        log.info("🔍 [SCHEDULER] Scan périodique des besoins actifs - {}", LocalDateTime.now());

        List<Besoin> besoinsActifs = besoinRepository.findByStatut(StatutBesoin.EN_ATTENTE);
        log.info("📊 {} besoins actifs à vérifier", besoinsActifs.size());

        int alertesCrees = 0;
        int besoinsVerifies = 0;

        for (Besoin besoin : besoinsActifs) {
            if (besoin.getDateExpiration() != null) {
                besoinsVerifies++;
                long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), besoin.getDateExpiration());

                // Alertes pour J-7, J-3, J-1
                if (joursRestants == 7 || joursRestants == 3 || joursRestants == 1) {
                    boolean alerteExiste = alerteRepository
                            .existsByUserIdAndBesoinIdAndJoursRestants(
                                    besoin.getStockage().getUserId(),
                                    besoin.getId(),
                                    (int) joursRestants);

                    if (!alerteExiste) {
                        String niveau = determinerNiveau((int) joursRestants);
                        String message = genererMessage(besoin, (int) joursRestants);

                        creerAlerte(
                                besoin.getStockage().getUserId(),
                                besoin.getId(),
                                besoin.getTypeProduit(),
                                message,
                                niveau,
                                (int) joursRestants
                        );
                        alertesCrees++;
                    }
                }
            }
        }

        log.info("✅ Scan terminé - {} besoins vérifiés, {} alertes créées", besoinsVerifies, alertesCrees);
    }

    // ========== SCHEDULER - RATTRAPAGE QUOTIDIEN (SÉCURITÉ) ==========

    @Scheduled(cron = "0 0 1 * * *") // Tous les jours à 01h00
    @Transactional
    public void rattrapageQuotidien() {
        log.info("🔄 [SCHEDULER] Rattrapage quotidien des alertes - {}", LocalDateTime.now());

        List<Besoin> tousBesoins = besoinRepository.findByStatut(StatutBesoin.EN_ATTENTE);

        int alertesCrees = 0;

        for (Besoin besoin : tousBesoins) {
            if (besoin.getDateExpiration() != null) {
                long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), besoin.getDateExpiration());

                // Vérifier tous les seuils (J-7, J-3, J-1, J-0)
                if (joursRestants >= 0 && joursRestants <= 7) {
                    // Vérifier si une alerte existe déjà pour ce seuil
                    boolean alerteExiste = alerteRepository
                            .existsByUserIdAndBesoinIdAndJoursRestants(
                                    besoin.getStockage().getUserId(),
                                    besoin.getId(),
                                    (int) joursRestants);

                    if (!alerteExiste && joursRestants <= 7) {
                        String niveau = determinerNiveau((int) joursRestants);
                        String message = genererMessage(besoin, (int) joursRestants);

                        creerAlerte(
                                besoin.getStockage().getUserId(),
                                besoin.getId(),
                                besoin.getTypeProduit(),
                                message,
                                niveau,
                                (int) joursRestants
                        );
                        alertesCrees++;
                    }
                }
            }
        }

        log.info("✅ Rattrapage terminé - {} alertes créées", alertesCrees);
    }

    // ========== MÉTHODES DE CONSULTATION ==========

    public List<Alerte> getAlertesNonLues(Long userId) {
        return alerteRepository.findByUserIdAndLueFalse(userId);
    }

    public List<Alerte> getAllAlertes(Long userId) {
        return alerteRepository.findByUserId(userId);
    }

    public long countNonLues(Long userId) {
        return alerteRepository.countByUserIdAndLueFalse(userId);
    }

    @Transactional
    public void marquerCommeLue(Long alerteId) {
        alerteRepository.marquerCommeLue(alerteId, LocalDateTime.now());
        log.info("Alerte {} marquée comme lue", alerteId);
    }

    @Transactional
    public void marquerToutLu(Long userId) {
        List<Alerte> alertes = alerteRepository.findByUserIdAndLueFalse(userId);
        alertes.forEach(a -> alerteRepository.marquerCommeLue(a.getId(), LocalDateTime.now()));
        log.info("{} alertes marquées comme lues pour userId {}", alertes.size(), userId);
    }

    public Map<String, Object> getStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", alerteRepository.countByUserId(userId));
        stats.put("nonLues", alerteRepository.countByUserIdAndLueFalse(userId));
        stats.put("info", alerteRepository.countByUserIdAndNiveau(userId, "INFO"));
        stats.put("warning", alerteRepository.countByUserIdAndNiveau(userId, "WARNING"));
        stats.put("urgent", alerteRepository.countByUserIdAndNiveau(userId, "URGENT"));
        stats.put("critical", alerteRepository.countByUserIdAndNiveau(userId, "CRITICAL"));
        return stats;
    }

    @Transactional
    public int nettoyerAnciennesAlertes() {
        LocalDateTime limite = LocalDateTime.now().minusDays(30);
        int count = alerteRepository.deleteByDateEnvoiBefore(limite);
        log.info("🗑️ {} anciennes alertes supprimées", count);
        return count;
    }
    /**
     * Génère les alertes pour tous les besoins actifs
     */
    @Transactional
    public void genererAlertesAutomatiques() {
        log.info("🔔 Génération automatique des alertes");

        List<Besoin> besoins = besoinRepository.findByStatut(StatutBesoin.EN_ATTENTE);
        int count = 0;

        for (Besoin besoin : besoins) {
            if (besoin.getDateExpiration() != null) {
                LocalDate today = LocalDate.now();
                long joursRestants = ChronoUnit.DAYS.between(today, besoin.getDateExpiration());

                if (joursRestants == 7 || joursRestants == 3 || joursRestants == 1) {
                    String niveau = determinerNiveau((int) joursRestants);
                    String message = genererMessage(besoin, (int) joursRestants);

                    Alerte alerte = creerAlerte(
                            besoin.getStockage().getUserId(),
                            besoin.getId(),
                            besoin.getTypeProduit(),
                            message,
                            niveau,
                            (int) joursRestants
                    );

                    if (alerte != null) {
                        count++;
                        log.info("Alerte créée: {} - {}", niveau, message);
                    }
                }
            }
        }

        log.info("✅ {} alertes générées", count);
    }
}