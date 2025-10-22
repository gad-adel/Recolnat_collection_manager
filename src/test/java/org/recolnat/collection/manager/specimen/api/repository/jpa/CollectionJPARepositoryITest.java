package org.recolnat.collection.manager.specimen.api.repository.jpa;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;
import org.recolnat.collection.manager.repository.jpa.CollectionJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(scripts = {"classpath:clean_data_collection_update_profile_test.sql", "classpath:init_data_collection_profile_test.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:clean_data_collection_update_profile_test.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles("test")
public class CollectionJPARepositoryITest {

    private static CollectionJPA collectionUt;
    @Autowired
    private CollectionJPARepository collectionJPARepository;

    @BeforeAll
    public static void init() {
        collectionUt = CollectionJPA.builder()
                .typeCollection("botanique")
                .descriptionFr("botanique")
                .descriptionEn("botanical")
                .collectionNameFr("Herbier de la Société des Lettres de l'Aveyron")
                .collectionNameEn("Herbarium of the Society of Letters of Aveyron")
                .institutionId(1).build();
    }

    @Test
    void save_collection_should_be_ok() {
        // Given
        // When
        CollectionJPA save = collectionJPARepository.save(collectionUt);
        //then
        assertThat(save.getId()).isNotNull();
    }

    @Test
    void should_find_collection_by_id() {
        // Given
        var colId = UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331");
        // When
        var foundCol = collectionJPARepository.findById(colId).orElseThrow(() -> new IllegalArgumentException("Init the data collection before test"));

        // Then
        assertThat(foundCol.getId()).isEqualTo(colId);
    }

    @Test
    void get_all_collection_is_ok() {
        // Given
        // When
        var findAll = collectionJPARepository.findAll();

        //Then
        assertThat(findAll.size()).matches(x -> x > 0);
    }

    @Test
    void create_collection_is_ok() {
        // Given
        // When
        var save = collectionJPARepository.save(collectionUt);

        //Then
        assertThat(save.getId()).isNotNull();
    }

    @Test
    void find_collection_by_instID() {
        // Given
        // When
        int institutionId = 1;
        var colInst = UUID.fromString("9a342a92-6fe8-48d3-984e-d1731c051666");
        var allcollectionJPAByInstitutionId = collectionJPARepository.findCollectionsByInstitutionId(institutionId).stream().map(CollectionJPA::getId).toList();
        var collectionJPAByInstitutionId = collectionJPARepository.findCollectionsByInstitutionId(institutionId).stream().map(CollectionJPA::getInstitutionId);

        //Then
        assertThat(collectionJPAByInstitutionId).containsOnly(institutionId);
        assertThat(colInst).isIn(allcollectionJPAByInstitutionId);
    }

}
