package org.recolnat.collection.manager.repository.jpa;

import org.recolnat.collection.manager.repository.entity.ImportJPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ImportJPARepository extends JpaRepository<ImportJPA, UUID> {

    @Query(value = """
                select * from import where status = 'PENDING' order by timestamp limit 1 for update skip locked;
            """, nativeQuery = true)
    Optional<ImportJPA> findFirstPending();

    @Query(value = """
                select i
                from import i
                join fetch i.files
                where i.institutionId = :institutionId
                order by i.timestamp desc
            """)
    Page<ImportJPA> findAllByInstitutionId(@Param("institutionId") UUID institutionId, PageRequest pageRequest);

    int countByInstitutionId(UUID institutionId);
}
