package org.recolnat.collection.manager.repository.jpa;

import org.recolnat.collection.manager.repository.entity.TaxonJPA;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TaxonJPARepository extends JpaRepository<TaxonJPA, UUID> {

    /**
     * Requête de statistique (Returns the number of distinct entities available.)
     * remarque le scientificName correspond à la concaténation du genre et de l'espèce
     *
     * @return
     */
    @Query("select count(distinct (t.scientificName)) from Taxon t")
    Integer countDistinct();

    @Query("SELECT DISTINCT t.family FROM Taxon t WHERE LOWER(t.family) LIKE LOWER(:query) ORDER BY t.family")
    List<String> findFamiliesStartingWith(String query, Pageable pageable);

    @Query("SELECT DISTINCT t.genus FROM Taxon t WHERE LOWER(t.genus) LIKE LOWER(:query)"
            + " AND (:family = '' OR t.family = :family) ORDER BY t.genus")
    List<String> findGenusStartingWithAndFilteredByFamily(String query, String family, Pageable pageable);

    @Query("SELECT DISTINCT t.specificEpithet FROM Taxon t WHERE LOWER(t.specificEpithet) LIKE LOWER(:query)"
            + " AND (:genus = '' OR t.genus = :genus) ORDER BY t.specificEpithet")
    List<String> findSpecificEpithetsStartingWithAndFilteredBy(String query, String genus, Pageable pageable);
}
