package tn.esprit.ms_receveur;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // ← AJOUTER CETTE LIGNE
public class MsReceveurApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsReceveurApplication.class, args);
    }
}