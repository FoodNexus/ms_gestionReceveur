// NotationRepository.java
package tn.esprit.ms_receveur.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.ms_receveur.entities.Notation;
import tn.esprit.ms_receveur.entities.Stockage;
import java.util.List;

public interface NotationRepository extends JpaRepository<Notation, Long> {

    List<Notation> findByStockage(Stockage stockage);

    @Query("SELECT AVG(n.note) FROM Notation n WHERE n.stockage = :stockage")
    Double calculerScoreMoyen(Stockage stockage);

    long countByStockage(Stockage stockage);

    boolean existsByStockageIdAndDonId(Long stockageId, Long donId);
}