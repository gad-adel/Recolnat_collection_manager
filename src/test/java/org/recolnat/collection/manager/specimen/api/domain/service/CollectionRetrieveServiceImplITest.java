package org.recolnat.collection.manager.specimen.api.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceDBTest;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.service.CollectionRetrieveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@SpringBootTest
@ActiveProfiles("int")
@Sql(scripts = {"classpath:clean_data_collection.sql", "classpath:init_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CollectionRetrieveServiceImplITest extends AbstractResourceDBTest {

    @Autowired
    private CollectionRetrieveService collectionRetrieveService;


    @Test
    void getAllCollection_should_be_ok() {
        // Given
        var institutionId = 1;
        // When
        var retreiveAllCollections = collectionRetrieveService.retreiveCollectionsByInstitution(institutionId, "fr");
        log.info("All collections  {}");
        // then
        assertThat(retreiveAllCollections).isNotEmpty();
    }

    @Test
    void givenCollection_whenFindCollectionById_thenThrowException() {
        // Given
        var collectionId = UUID.fromString(COLLECTION_ID_KO);
        // When
        var responseException = assertThrows(CollectionManagerBusinessException.class, () -> collectionRetrieveService
                .findCollectionById(collectionId));

        //Then
        assertThat(responseException.getCode()).isEqualTo("ERR_CODE_CM");
    }
}
