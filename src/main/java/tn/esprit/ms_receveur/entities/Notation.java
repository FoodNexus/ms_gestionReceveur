package tn.esprit.ms_receveur.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Many-to-One avec Stockage
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockage_id",
            nullable = false)
    private Stockage stockage;

    private Long donId;
    private Integer note;
    private String commentaire;

    @CreationTimestamp
    private LocalDateTime dateNotation;
}