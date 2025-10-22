package org.recolnat.collection.manager.repository.jpa;


import org.recolnat.collection.manager.repository.entity.LiteratureJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LiteratureJPARepository extends JpaRepository<LiteratureJPA, UUID> {

    @Query(value = """
                select l.* from literature l where l.fk_id_specimen = :uuid
            """, nativeQuery = true)
    List<LiteratureJPA> findAllBySpecimenId(@Param("uuid") UUID uuid);
}
