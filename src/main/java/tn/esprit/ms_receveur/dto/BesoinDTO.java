package tn.esprit.ms_receveur.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BesoinDTO {
    private Long stockageId;    // ✅ lien vers Stockage
    private String typeProduit;
    private Double quantiteKg;
    private String description;
    private LocalDate dateExpiration;
}