package tn.esprit.ms_receveur.dto;

import lombok.Data;

import java.util.List;

@Data
public class PredictionResultDTO {
    private List<BesoinPredictionDTO> predictions;
    private String niveauConfiance;  // FAIBLE, MOYEN, ELEVE
    private double gainEstimeKg;     // Gaspillage évité estimé
}
