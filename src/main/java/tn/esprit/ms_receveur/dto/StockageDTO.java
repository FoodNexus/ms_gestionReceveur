// StockageDTO.java - Version corrigée
package tn.esprit.ms_receveur.dto;

import lombok.Data;

@Data
public class StockageDTO {
    private Double capaciteMaxKg;      // ← Changé : capaciteTotaleKg → capaciteMaxKg
    private Boolean aFrigorifique;
    private Double capaciteFrigoKg;
}