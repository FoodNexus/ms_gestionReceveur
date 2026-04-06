// NotationDTO.java
package tn.esprit.ms_receveur.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NotationDTO {

    @NotNull(message = "stockageId est requis")
    private Long stockageId;

    @NotNull(message = "donId est requis")
    private Long donId;

    @Min(value = 1, message = "La note doit être entre 1 et 5")
    @Max(value = 5, message = "La note doit être entre 1 et 5")
    private Integer note;

    private String commentaire;
}