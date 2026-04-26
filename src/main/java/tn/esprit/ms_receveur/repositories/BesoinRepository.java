package tn.esprit.ms_receveur.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.ms_receveur.entities.Besoin;
import tn.esprit.ms_receveur.entities.Stockage;
import tn.esprit.ms_receveur.enums.StatutBesoin;
import java.time.LocalDate;
import java.util.List;

public interface BesoinRepository extends JpaRepository<Besoin, Long> {

    // ========== RECHERCHES DE BASE ==========

    List<Besoin> findByStockage(Stockage stockage);

    List<Besoin> findByStockageAndStatut(Stockage stockage, StatutBesoin statut);

    List<Besoin> findByTypeProduitAndStatut(String typeProduit, StatutBesoin statut);

    // ✅ AJOUTER CETTE LIGNE
    List<Besoin> findByStatut(StatutBesoin statut);

    // ========== RECHERCHES PAR USER ==========

    @Query("SELECT b FROM Besoin b WHERE b.stockage.userId = :userId")
    List<Besoin> findByStockageUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(b) FROM Besoin b WHERE b.stockage.userId = :userId")
    long countByStockageUserId(@Param("userId") Long userId);

    // ========== POUR LE SCHEDULER (EXPIRATION) ==========

    @Query("SELECT b FROM Besoin b WHERE b.dateExpiration < :today AND b.statut = :statut")
    List<Besoin> findByDateExpirationBeforeAndStatut(@Param("today") LocalDate today,
                                                     @Param("statut") StatutBesoin statut);

    @Query("SELECT b FROM Besoin b WHERE b.dateExpiration BETWEEN :start AND :end AND b.statut = :statut")
    List<Besoin> findByDateExpirationBetweenAndStatut(@Param("start") LocalDate start,
                                                      @Param("end") LocalDate end,
                                                      @Param("statut") StatutBesoin statut);

    @Query("SELECT b FROM Besoin b WHERE b.dateExpiration = :date AND b.statut = :statut")
    List<Besoin> findByDateExpirationAndStatut(@Param("date") LocalDate date,
                                               @Param("statut") StatutBesoin statut);

    @Modifying
    @Query("UPDATE Besoin b SET b.statut = :nouveauStatut WHERE b.dateExpiration < :today AND b.statut = :ancienStatut")
    int updateExpiredBesoinStatus(@Param("today") LocalDate today,
                                  @Param("ancienStatut") StatutBesoin ancienStatut,
                                  @Param("nouveauStatut") StatutBesoin nouveauStatut);
}