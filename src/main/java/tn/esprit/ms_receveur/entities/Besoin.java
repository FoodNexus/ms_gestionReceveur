package tn.esprit.ms_receveur.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tn.esprit.ms_receveur.enums.StatutBesoin;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "besoins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Besoin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Many-to-One avec Stockage
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockage_id",
            nullable = false)
    private Stockage stockage;

    private String typeProduit;
    private Double quantiteKg;
    private String description;
    private LocalDate dateExpiration;

    @Enumerated(EnumType.STRING)
    private StatutBesoin statut;

    @CreationTimestamp
    private LocalDateTime dateCreation;
}