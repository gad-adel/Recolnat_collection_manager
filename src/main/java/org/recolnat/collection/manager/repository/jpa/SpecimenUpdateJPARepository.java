package org.recolnat.collection.manager.repository.jpa;

import org.recolnat.collection.manager.repository.entity.SpecimenUpdateJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SpecimenUpdateJPARepository extends JpaRepository<SpecimenUpdateJPA, UUID> {

    @Query
    List<SpecimenUpdateJPA> findAllByImportJPA_Id(UUID importJPAId);
}
