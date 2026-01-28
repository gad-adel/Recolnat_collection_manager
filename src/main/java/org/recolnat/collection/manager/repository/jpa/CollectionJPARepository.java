package org.recolnat.collection.manager.repository.jpa;

import org.recolnat.collection.manager.api.domain.CollectionDashboardProjection;
import org.recolnat.collection.manager.api.domain.CollectionProjection;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionJPARepository extends JpaRepository<CollectionJPA, UUID> {

    List<CollectionJPA> findCollectionsByInstitutionId(Integer institutionId);

    @Query(value = """
            select id from collection c where c.fk_institution_id = :institutionId and c.collection_name_fr = :collectionName
            """, nativeQuery = true)
    UUID findCollectionIdByInstitutionIdAndCollectionName(UUID institutionId, String collectionName);

    boolean existsByInstitutionInstitutionIdAndCollectionNameFr(UUID institutionInstitutionId, String collectionNameFr);

    @Query(value = """
            select c.id as id, c.collectionNameFr as nameFr, c.collectionNameEn as nameEn, count(s.id) as specimenCount, c.typeCollection as type
            from collection c
            left join specimen s on s.collection.id = c.id
            where c.institution.institutionId = :institutionId
            and upper(c.collectionNameFr) like upper(:searchTerm)
            group by c.id, c.collectionNameFr, c.collectionNameEn
            """)
    Page<CollectionDashboardProjection> findCollectionsByInstitutionId(@Param("institutionId") UUID institutionId, @Param("searchTerm") String searchTerm,
                                                                       Pageable pageable);

    @Query(value = """
            select count(c.id)
            from collection c
            where c.institution.institutionId = :institutionId
            and upper(c.collectionNameFr) like upper(:searchTerm)
            """)
    Long countCollectionByInstitutionId(@Param("institutionId") UUID institutionId, @Param("searchTerm") String searchTerm);

    Optional<CollectionJPA> findByCollectionNameFrAndInstitution_InstitutionId(String collectionNameFr, UUID instId);

    List<CollectionJPA> findByIdIn(List<UUID> ids);

    @Query(value = """
            select c.id as id, c.collectionNameFr as nameFr, c.collectionNameEn as nameEn, c.typeCollection as type, c.collectionCode as code
            from collection c
            order by c.collectionNameFr""")
    List<CollectionProjection> findAllOptions();

    @Query(value = """
            select c.id as id, c.collectionNameFr as nameFr, c.collectionNameEn as nameEn, c.typeCollection as type, c.collectionCode as code
            from collection c
            where c.institution.institutionId = :institutionId
            order by c.collectionNameFr""")
    List<CollectionProjection> findAllOptionsByInstitutionId(@Param("institutionId") UUID institutionId);


}
