package tn.esprit.ms_receveur.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.ms_receveur.entities.Besoin;
import tn.esprit.ms_receveur.enums.StatutBesoin;
import tn.esprit.ms_receveur.repositories.BesoinRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupSchedulerService {

    private final BesoinRepository besoinRepository;
    private final NotificationService notificationService;

    /**
     * 1️⃣ Marquer les besoins expirés (tous les jours à 01h00)
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void marquerBesoinsExpires() {
        log.info("🔄 [SCHEDULER] Démarrage du marquage des besoins expirés");
        long startTime = System.currentTimeMillis();

        LocalDate today = LocalDate.now();

        int updatedCount = besoinRepository.updateExpiredBesoinStatus(
                today,
                StatutBesoin.EN_ATTENTE,
                StatutBesoin.EXPIRE
        );

        long endTime = System.currentTimeMillis();
        log.info("✅ [SCHEDULER] Marquage terminé: {} besoins expirés en {} ms",
                updatedCount, (endTime - startTime));

        if (updatedCount > 0) {
            notificationService.envoyerNotificationExpiration(updatedCount);
        }
    }

    /**
     * 2️⃣ Supprimer les besoins expirés depuis plus de 7 jours (tous les dimanches à 02h00)
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    @Transactional
    public void supprimerBesoinsAnciens() {
        log.info("🔄 [SCHEDULER] Démarrage de la suppression des anciens besoins");
        long startTime = System.currentTimeMillis();

        LocalDate limite = LocalDate.now().minusDays(7);
        List<Besoin> besoinsASupprimer = besoinRepository
                .findByDateExpirationBeforeAndStatut(limite, StatutBesoin.EXPIRE);

        int count = besoinsASupprimer.size();

        if (count > 0) {
            besoinRepository.deleteAll(besoinsASupprimer);
            log.info("✅ [SCHEDULER] Suppression terminée: {} besoins supprimés", count);
        } else {
            log.info("✅ [SCHEDULER] Aucun besoin à supprimer");
        }

        long endTime = System.currentTimeMillis();
        log.info("⏱️ [SCHEDULER] Suppression terminée en {} ms", (endTime - startTime));
    }

    /**
     * 3️⃣ Alerter les associations des besoins proches de l'expiration (tous les jours à 08h00)
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void alerterBesoinsProchesExpiration() {
        log.info("🔔 [SCHEDULER] Vérification des besoins proches d'expiration");

        LocalDate today = LocalDate.now();
        LocalDate dans3Jours = today.plusDays(3);
        LocalDate dans7Jours = today.plusDays(7);

        List<Besoin> urgents = besoinRepository
                .findByDateExpirationBetweenAndStatut(today, dans3Jours, StatutBesoin.EN_ATTENTE);

        for (Besoin besoin : urgents) {
            log.warn("⚠️ [URGENT] Besoin #{} - {} - Expire le {}",
                    besoin.getId(), besoin.getTypeProduit(), besoin.getDateExpiration());
            notificationService.alerterExpirationProche(besoin, 3);
        }

        List<Besoin> proches = besoinRepository
                .findByDateExpirationBetweenAndStatut(dans3Jours.plusDays(1), dans7Jours, StatutBesoin.EN_ATTENTE);

        for (Besoin besoin : proches) {
            log.info("📅 [INFO] Besoin #{} - {} - Expire le {}",
                    besoin.getId(), besoin.getTypeProduit(), besoin.getDateExpiration());
            notificationService.alerterExpirationProche(besoin, 7);
        }

        log.info("✅ [SCHEDULER] Alertes envoyées: {} urgentes, {} proches",
                urgents.size(), proches.size());
    }

    /**
     * 4️⃣ Nettoyage manuel (endpoint pour admin)
     */
    @Transactional
    public CleanupResult nettoyageManuel() {
        log.info("🔄 [MANUEL] Nettoyage manuel déclenché");

        LocalDate today = LocalDate.now();
        LocalDate limite = today.minusDays(30);

        int marques = besoinRepository.updateExpiredBesoinStatus(today, StatutBesoin.EN_ATTENTE, StatutBesoin.EXPIRE);

        List<Besoin> aSupprimer = besoinRepository.findByDateExpirationBeforeAndStatut(limite, StatutBesoin.EXPIRE);
        int supprimes = aSupprimer.size();
        besoinRepository.deleteAll(aSupprimer);

        return new CleanupResult(marques, supprimes, LocalDateTime.now());
    }

    public record CleanupResult(int marques, int supprimes, LocalDateTime dateExecution) {}
}