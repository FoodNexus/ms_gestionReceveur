package tn.esprit.ms_receveur.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ms_receveur.entities.Stockage;
import java.util.Optional;

public interface StockageRepository
        extends JpaRepository<Stockage, Long> {

    Optional<Stockage> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}