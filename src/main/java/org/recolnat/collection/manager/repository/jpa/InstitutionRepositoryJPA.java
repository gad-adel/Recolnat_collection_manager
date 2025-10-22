package org.recolnat.collection.manager.repository.jpa;

import jakarta.transaction.Transactional;
import org.recolnat.collection.manager.api.domain.InstitutionProjection;
import org.recolnat.collection.manager.api.domain.InstitutionStatisticProjection;
import org.recolnat.collection.manager.api.domain.MidsGroup;
import org.recolnat.collection.manager.api.domain.enums.PartnerType;
import org.recolnat.collection.manager.repository.entity.InstitutionJPA;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface InstitutionRepositoryJPA extends JpaRepository<InstitutionJPA, Integer> {

    @Query(value = """
              select
                isim.id AS id,
                isim.institution_id AS institutionId,
                isim.institution_name AS institutionName,
                isim.computerized_specimens AS computerizedSpecimens,
                isim.specimens_count AS specimensCount,
                isim.types_count AS typesCount,
                isim.taxons_count AS taxonsCount,
                isim.last_update AS lastUpdate
              from institution_statistic_index_mview isim
              where isim.institution_id = :institutionId
            """, nativeQuery = true)
    InstitutionStatisticProjection getInstitutionStatistic(@Param("institutionId") UUID institutionId);

    Optional<InstitutionJPA> findInstitutionByCodeIgnoreCase(String code);

    Optional<InstitutionJPA> findInstitutionByInstitutionId(UUID code);

    @Query("from institution where institutionId IN :ids")
    List<InstitutionJPA> findInstitutionByInstitutionIds(@Param("ids") List<UUID> ids);

    Page<InstitutionJPA> findAllByDataChangeTsBetween(LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable);

    Page<InstitutionJPA> findAllByDataChangeTsBefore(LocalDateTime dateFrom, Pageable pageable);

    Page<InstitutionJPA> findAllByPartnerType(Pageable pageable, PartnerType partnerType);

    @Query(value = "select i.institutionId as id, i.name as name from institution i order by i.name")
    List<InstitutionProjection> findAllOptions();

    @Query("from institution where code IN :codes")
    List<InstitutionJPA> findInstitutionsByCodesIgnoreCase(@Param("codes") List<String> code);

    /**
     * requete de statitstique ( Returns the number of distinct entities available.)
     *
     * @return
     */
    @Query("select count(distinct t.id) from institution t")
    Integer countDistinct();

    @Modifying
    @Transactional
    @Query(value = """
            REFRESH MATERIALIZED view institution_statistic_index_mview
            """, nativeQuery = true)
    void refreshMaterializedView();

    @Query(value = """
                select mids, count(*) as count
                from specimen s
                join collection c on s.fk_id_collection = c.id
                and c.fk_institution_id = :institutionId
                and s.state = 'VALID'
                group by mids
                order by mids;
            """, nativeQuery = true)
    List<MidsGroup> getInstitutionMids(@Param("institutionId") UUID institutionId);
}
