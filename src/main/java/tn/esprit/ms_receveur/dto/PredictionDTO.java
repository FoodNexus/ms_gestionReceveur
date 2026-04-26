// PredictionDTO.java
package tn.esprit.ms_receveur.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PredictionDTO {
    private Long userId;
    private int horizonJours;  // 7, 14, 30 jours
}

