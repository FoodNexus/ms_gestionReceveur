package tn.esprit.ms_receveur.runners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.ms_receveur.services.CleanupSchedulerService;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupRunner implements ApplicationRunner {

    private final CleanupSchedulerService cleanupSchedulerService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("========================================");
        log.info("🚀 Nettoyage initial au démarrage");
        log.info("========================================");
        cleanupSchedulerService.marquerBesoinsExpires();
        log.info("✅ Nettoyage initial terminé");
    }
}