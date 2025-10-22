package org.recolnat.collection.manager.institution.api.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.recolnat.collection.manager.api.domain.Collection;
import org.recolnat.collection.manager.api.domain.Institution;
import org.recolnat.collection.manager.api.domain.InstitutionDetail;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.LanguageEnum;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceDBTest;
import org.recolnat.collection.manager.service.CollectionRetrieveService;
import org.recolnat.collection.manager.service.impl.InstitutionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/***
 * test dans base postgresprofile= int (integration)
 */
@ActiveProfiles("int")
@Sql(scripts = {"classpath:init_data_institution.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InstitutionServiceImplITest extends AbstractResourceDBTest {

    @Autowired
    private InstitutionServiceImpl sut;

    @MockBean
    private CollectionRetrieveService apiClient;

    @Test
    void retreiveInstitution_should_be_ok() {
        // Given
        int id = 2;
        // When
        when(apiClient.retreiveCollectionsByInstitution(id, LanguageEnum.FR.name()))
                .thenReturn(List.of(Collection.builder().collectionName("collection_code").build()));

        Institution institution = sut.getInstitutionById(id, LanguageEnum.FR.name());
        // Then
        assertThat(institution.getId()).isEqualTo(2);
        assertThat(institution.getName()).isEqualTo("Conservatoire et jardins botaniques de Nancy");
    }

    @Test
    void retreiveInstitutionbyUUID_should_be_ok() {
        // Given
        UUID id = UUID.fromString("1d5e16d0-4564-4ef4-93ab-ec434a23ae75");
        // When
        when(apiClient.retreiveCollectionsByInstitution(anyInt(), eq(LanguageEnum.FR.name())))
                .thenReturn(List.of(Collection.builder().collectionName("collection_code").build()));
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("admin").institution(1).role("ADMIN").build());

        InstitutionDetail institution = sut.getInstitutionByUUID(id, LanguageEnum.FR.name());
        // Then
        assertThat(institution.getName()).isEqualTo("Société nationale des sciences naturelles et mathématiques de Cherbourg(CHE)");
    }

}
