package org.recolnat.collection.manager.repository.jpa;

import org.recolnat.collection.manager.repository.entity.GeologicalContextJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface GeologicalContextJPARepository extends JpaRepository<GeologicalContextJPA,UUID> {
    @Modifying
    @Query("delete from geologicalContext gc where gc.id = ?1")
    void removeById(UUID geologicalContextJPAId);
}
