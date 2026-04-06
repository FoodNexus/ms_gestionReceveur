package tn.esprit.ms_receveur.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ms_receveur.entities.Besoin;
import tn.esprit.ms_receveur.entities.Stockage;
import tn.esprit.ms_receveur.enums.StatutBesoin;
import java.util.List;

public interface BesoinRepository
        extends JpaRepository<Besoin, Long> {

    // ✅ Trouver par Stockage
    List<Besoin> findByStockage(Stockage stockage);

    // ✅ Trouver par Stockage et Statut
    List<Besoin> findByStockageAndStatut(
            Stockage stockage, StatutBesoin statut);

    // ✅ Trouver par type de produit
    List<Besoin> findByTypeProduitAndStatut(
            String typeProduit, StatutBesoin statut);
}