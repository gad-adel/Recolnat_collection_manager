package org.recolnat.collection.manager.repository.jpa;

import org.recolnat.collection.manager.repository.entity.CollectionEventJPA;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CollectionEventJPARepository extends JpaRepository<CollectionEventJPA, UUID> {

    @Modifying
    @Query("delete from collectionEvent ce where ce.id = ?1")
    void removeById(UUID collectionEventJPAId);

    @Query("SELECT DISTINCT c.location.country FROM collectionEvent c WHERE LOWER(c.location.country) LIKE LOWER(:query) ORDER BY c.location.country ASC")
    List<String> findCountriesStartingWith(String query, Pageable pageable);

    @Query("SELECT DISTINCT c.location.continent FROM collectionEvent c WHERE LOWER(c.location.continent) LIKE LOWER(:query) ORDER BY c.location.continent ASC")
    List<String> findContinentsStartingWith(String query, Pageable pageable);

    @Query("SELECT DISTINCT c.recordedBy FROM collectionEvent c WHERE LOWER(c.recordedBy) LIKE LOWER(:query) ORDER BY c.recordedBy ASC")
    List<String> findRecordersStartingWith(String query, Pageable pageable);
}
