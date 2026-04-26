package tn.esprit.ms_receveur.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.ms_receveur.entities.Alerte;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AlerteRepository extends JpaRepository<Alerte, Long> {

    List<Alerte> findByUserIdAndLueFalse(Long userId);
    List<Alerte> findByUserId(Long userId);

    long countByUserIdAndLueFalse(Long userId);
    long countByUserId(Long userId);
    long countByUserIdAndNiveau(Long userId, String niveau);
    long countByUserIdAndNiveauAndLueFalse(Long userId, String niveau);

    // ✅ AJOUTER CETTE MÉTHODE
    boolean existsByUserIdAndBesoinIdAndJoursRestants(Long userId, Long besoinId, Integer joursRestants);

    Optional<Alerte> findByUserIdAndBesoinIdAndJoursRestants(Long userId, Long besoinId, Integer joursRestants);

    @Modifying
    @Query("UPDATE Alerte a SET a.lue = true, a.dateLecture = :now WHERE a.id = :id")
    void marquerCommeLue(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Alerte a WHERE a.dateEnvoi < :limite")
    int deleteByDateEnvoiBefore(@Param("limite") LocalDateTime limite);
}