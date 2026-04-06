// Stockage.java
package tn.esprit.ms_receveur.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "stockages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stockage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Double capaciteMaxKg;

    @Column(nullable = false)
    private Double capaciteDisponibleKg;

    @Column(nullable = false)
    private Boolean aFrigorifique;

    private Double capaciteFrigoKg;

    @CreationTimestamp
    private LocalDateTime dateCreation;
}