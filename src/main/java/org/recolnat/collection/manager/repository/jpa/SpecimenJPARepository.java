package org.recolnat.collection.manager.repository.jpa;

import org.recolnat.collection.manager.api.domain.NominativeCollectionDashboardProjection;
import org.recolnat.collection.manager.api.domain.enums.SpecimenStatusEnum;
import org.recolnat.collection.manager.repository.entity.SpecimenJPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpecimenJPARepository extends JpaRepository<SpecimenJPA, UUID>, JpaSpecificationExecutor<SpecimenJPA> {

    Optional<SpecimenJPA> findByIdAndCollectionId(UUID id, UUID collectionId);

    @Query(
            value = """
                    select s.* from specimen s
                    join collection c on s.fk_id_collection = c.id
                    where c.fk_institution_id = :institutionId
                    and c.collection_name_fr = :collectionName
                    and s.catalog_number = :catalogNumber
                    """,
            nativeQuery = true
    )
    List<SpecimenJPA> findSpecimens(UUID institutionId, String collectionName, String catalogNumber);

    @Query(
            value = """
                    select count(s.*) > 0 from specimen s
                    join collection c on s.fk_id_collection = c.id
                    where c.fk_institution_id = :institutionId
                    and c.collection_name_fr = :collectionName
                    and s.catalog_number = :catalogNumber
                    """,
            nativeQuery = true
    )
    boolean countSpecimens(UUID institutionId, String collectionName, String catalogNumber);

    @Query("select s from specimen s inner join s.collection c where c.institutionId = ?1")
    @EntityGraph(type = EntityGraphType.FETCH, value = "spec-entity-graph")
    Page<SpecimenJPA> findByInstitutionId(Integer institutionId, Pageable pageable);

    @Query("select s from specimen s inner join collection c where collection in (select c from collection c where c.institutionId = :institution) and s.catalogNumber = :catalogNumber")
    Page<SpecimenJPA> filterByCatalogNumber(@Param("catalogNumber") String catalogNumber, @Param("institution") Integer institution, Pageable pageable);

    @Modifying
    @Query("delete from specimen s where s.id = ?1")
    void deleteSpecimen(UUID specimenId);

    /**
     * @param fromDate
     * @param toDate
     * @param state
     * @param page
     * @return
     */
    Page<SpecimenJPA> findAllByDataChangeTsBetweenAndStateIs(LocalDateTime fromDate, LocalDateTime toDate, SpecimenStatusEnum state, Pageable page);

    /**
     * @param fromDate
     * @param state
     * @param pageable
     * @return
     */
    Page<SpecimenJPA> findAllByDataChangeTsBeforeAndStateIs(LocalDateTime fromDate, SpecimenStatusEnum state, Pageable pageable);

    /**
     * requete executee pour le detail public
     *
     * @param id
     * @return
     */
    @Query(value = """
              select s from specimen s where s.id = :id and s.state = 'VALID'
            """)
    @EntityGraph(type = EntityGraphType.FETCH, value = "spec-entity-graph-All")
    Optional<SpecimenJPA> findSpecimenById(@Param("id") UUID id);

    /**
     * requete de statitstique ( Returns the number of distinct entities available.)
     *
     * @return
     */
    @Query("select count(distinct t.id) from specimen t where t.state = 'VALID'")
    Integer countDistinct();

    @Query("select distinct s.nominativeCollection from specimen s where upper(s.nominativeCollection) like upper(:query) order by s.nominativeCollection")
    List<String> findNominativeCollectionsStartingWith(String query, PageRequest of);

    @Query(value = """
            select distinct s.nominative_collection
            from specimen s
            join collection c on c.id = s.fk_id_collection
            and c.fk_institution_id = :institutionId
            where s.nominative_collection is not null
            order by s.nominative_collection
            """, nativeQuery = true)
    List<String> findDistinctNominativeCollectionsByInstitutionId(@Param("institutionId") UUID institutionId);

    @Query(value = """
            select distinct m.storage_name
            from specimen s
            join management m on m.id = s.fk_management_id
            where upper(m.storage_name) like upper(:query) order by m.storage_name""", nativeQuery = true)
    List<String> findStorageNamesContains(String query, PageRequest of);


    @Query(value = """
            select s.nominative_collection as name, count(s.nominative_collection) as specimenCount
            from specimen s
            join collection c on c.id = s.fk_id_collection
            where c.fk_institution_id = :institutionId
            and upper(nominative_collection) like upper(:nominativeCollection)
            group by s.nominative_collection
            """, nativeQuery = true)
    Page<NominativeCollectionDashboardProjection> findNominativeCollectionsByInstitutionId(@Param("institutionId") UUID institutionId,
                                                                                           @Param("nominativeCollection") String nominativeCollection,
                                                                                           PageRequest of);

    @Query(value = """
                select count(distinct nominative_collection)
                    from specimen s
                        join collection c on c.id = s.fk_id_collection
                        where c.fk_institution_id = :institutionId
                                and upper(nominative_collection) like upper(:nominativeCollection)
            """, nativeQuery = true)
    Long countNominativeCollectionByInstitutionId(@Param("institutionId") UUID institutionId, @Param("nominativeCollection") String nominativeCollection);

    @Query(value = "select count(s.*) > 0 from specimen s where s.fk_id_collection = :collectionId", nativeQuery = true)
    boolean existsForCollection(UUID collectionId);

    @Query(value = "select s.id from specimen s where s.fk_id_collection = :collectionId", nativeQuery = true)
    List<String> findAllIdsByCollectionId(UUID collectionId);

    @Query(value = """
            SELECT count(*) > 0
            FROM specimen s
            WHERE s.fk_id_collection = :collectionId
            AND s.catalog_number = :catalogNumber
            AND (:specimenId IS NULL OR CAST(s.id AS TEXT) != :specimenId)
            """, nativeQuery = true)
    boolean alreadyExists(@Param("collectionId") UUID collectionId, @Param("catalogNumber") String catalogNumber, @Param("specimenId") String specimenId);

    @Query(value = """
                select count(s.*)
                from specimen s
                join collection c on c.id = s.fk_id_collection and c.fk_institution_id = ?1
                where exists
                (
                 select from json_array_elements(cast((?2) as json)) j
                 where j ->> 0 = c.collection_name_fr
                   and j ->> 1 = s.catalog_number
                )
            """, nativeQuery = true)
    int countSpecimen(UUID institutionId, String identifiers);

    @Query(value = """
            select distinct * from specimen s
            join specimen_update su on s.id = su.fk_specimen_id
            where su.fk_import_id = :id
            """, nativeQuery = true)
    List<SpecimenJPA> findSpecimenByImportId(UUID id);
}
