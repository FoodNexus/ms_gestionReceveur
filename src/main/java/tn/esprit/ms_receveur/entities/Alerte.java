package tn.esprit.ms_receveur.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "alertes")
@Data
public class Alerte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long besoinId;
    private String typeProduit;
    private String message;
    private String niveau; // INFO, WARNING, URGENT, CRITICAL
    private Integer joursRestants;
    private Boolean lue;
    private LocalDateTime dateEnvoi;
    private LocalDateTime dateLecture;
}