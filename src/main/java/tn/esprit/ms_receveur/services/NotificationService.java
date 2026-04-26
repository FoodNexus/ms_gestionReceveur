// NotificationService.java
package tn.esprit.ms_receveur.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.ms_receveur.entities.Besoin;
import tn.esprit.ms_receveur.entities.Stockage;

@Service
@Slf4j
public class NotificationService {

    public void alerterExpirationProche(Besoin besoin, int jours) {
        // Simulation d'envoi de notification
        String message = String.format(
                "🔔 ALERTE: Votre besoin de %s (%.1f kg) expire dans %d jours (le %s)",
                besoin.getTypeProduit(), besoin.getQuantiteKg(),
                jours, besoin.getDateExpiration()
        );

        // À implémenter: email, SMS, notification push
        log.info("📧 [NOTIFICATION] {}", message);
    }

    public void envoyerNotificationExpiration(int count) {
        String message = String.format(
                "📊 %d besoins ont expiré automatiquement aujourd'hui.",
                count
        );
        log.info("📧 [NOTIFICATION ADMIN] {}", message);
    }
}