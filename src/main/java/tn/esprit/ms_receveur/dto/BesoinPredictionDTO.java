// BesoinPredictionDTO.java
package tn.esprit.ms_receveur.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor  // ← Ajoutez cette annotation
public class BesoinPredictionDTO {
    private String typeProduit;
    private double quantitePrediteKg;
    private LocalDate datePrevue;
    private String niveauUrgence;
    private String raison;
}