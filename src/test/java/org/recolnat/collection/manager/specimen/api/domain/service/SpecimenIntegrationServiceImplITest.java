package org.recolnat.collection.manager.specimen.api.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.CollectionEvent;
import org.recolnat.collection.manager.api.domain.GeologicalContext;
import org.recolnat.collection.manager.api.domain.Identification;
import org.recolnat.collection.manager.api.domain.Location;
import org.recolnat.collection.manager.api.domain.Specimen;
import org.recolnat.collection.manager.api.domain.Taxon;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.SpecimenStatusEnum;
import org.recolnat.collection.manager.common.check.service.ControlAttribut;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.connector.api.MediathequeService;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;
import org.recolnat.collection.manager.repository.entity.LocationJPA;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.recolnat.collection.manager.service.AuthenticationService;
import org.recolnat.collection.manager.service.CollectionIdentifier;
import org.recolnat.collection.manager.service.SpecimenIntegrationService;
import org.recolnat.collection.manager.service.impl.ElasticServiceImpl;
import org.recolnat.collection.manager.service.impl.SpecimenIntegrationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.recolnat.collection.manager.api.domain.enums.LevelTypeEnum.MASTER;


@Slf4j
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class SpecimenIntegrationServiceImplITest {
    static UUID collectionId = UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331");
    static UUID specimenId = UUID.fromString("9c6ab9ea-d049-47b5-972c-18e7831bdd4e");
    static UUID specimenId2 = UUID.fromString("359eefe3-901a-4faf-bc3e-6f3fa266a465");
    static UUID specimenId3 = UUID.fromString("bf25ed41-f55c-41ec-bcb8-064268420e78");

    @MockBean
    ElasticServiceImpl elasticServiceImpl;

    @Autowired
    private SpecimenIntegrationService specimenIntegrationService;

    @Autowired
    private SpecimenJPARepository specimenJPARepository;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private ControlAttribut controlAttribut;
    @MockBean
    private MediathequeService mediathequeService;

    @Test
    @Sql(scripts = "classpath:init_data_collection_profile_test.sql")
    void getSpecimenById_should_be_ok() {
        // Given

        // When
        final var specimenFound = specimenIntegrationService.getSpecimenById(specimenId);
        log.info("specimenFound value: {}", specimenFound);
        assertThat(specimenFound).isNotNull();
        assertThat(specimenFound.getId()).isEqualTo(specimenId);
    }

    @Test
    @Sql(scripts = "classpath:init_data_collection_profile_test.sql")
    @Transactional
    void update_specimen_should_be_ok() {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("respuid").institution(1).role("ADMIN_INSTITUTION").collections(List.of(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331"))).build());

        final var spec = Specimen.builder().id(specimenId)
                .collectionId(collectionId)
                .identifications(Set.of(Identification.builder()
                                .identificationRemarks("identificationRemarks-01")
                                .taxon(List.of(Taxon.builder()
                                        .levelType(MASTER)
                                        .scientificName("scientificName").build()))
                                .currentDetermination(true).build(),
                        Identification.builder()
                                .identificationRemarks("identificationRemarks-02")
                                .taxon(List.of(Taxon.builder()
                                        .scientificName("scientificName2").levelType(MASTER).build()))
                                .currentDetermination(false).build()))
                .geologicalContext(GeologicalContext.builder()
                        .bed("Teddibert").build()).build();
        doNothing().when(elasticServiceImpl).addOrUpdateRefSpecimenElastic(any(Specimen.class), any(CollectionJPA.class));

        final var updated = specimenIntegrationService.update(specimenId, spec);

        final var specFound = specimenJPARepository.findById(specimenId);

        log.info("specimen updated: {}", specFound);
        var identifications = specFound.get().getIdentifications();
        var identification = identifications.stream().findFirst().get();
        assertThat(updated.getSpecimenId()).isNotNull();
        assertThat(identifications).hasSize(2);
        assertThat(identification.getTaxon()).hasSize(1);

    }

    @Test
    @DisplayName("JUnit test update specimen with media")
    @Sql(scripts = "classpath:init_data_collection_profile_test.sql")
    @Transactional
    void givenSpecimenWithMedia_whenUpdate_thenReturnSpecimenWithoutMedia() {
        // Given - precondition or setup
        var responseEntity = new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
        final var spec = Specimen.builder()
                .id(specimenId2)
                .medias(new ArrayList<>())
                .collectionId(collectionId)
                .identifications(Set.of(Identification.builder()
                                .taxon(List.of(Taxon.builder().levelType(MASTER)
                                        .scientificName("scientificName").build()))
                                .currentDetermination(true).build(),
                        Identification.builder().taxon(List.of(Taxon.builder()
                                        .scientificName("scientificName2")
                                        .levelType(MASTER).build()))
                                .currentDetermination(false).build()))
                .geologicalContext(GeologicalContext.builder()
                        .bed("Teddibert").build()).build();
        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("respuid").institution(1).role("ADMIN_INSTITUTION").collections(List.of(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331"))).build());

        when(mediathequeService.deletePicture("1660148777364m90qnta9kyzgZr6p")).thenReturn(responseEntity);
        doNothing().when(elasticServiceImpl).addOrUpdateRefSpecimenElastic(any(Specimen.class), any(CollectionJPA.class));

        when(mediathequeService.deletePicture("1660148777364m90qnta9kyzgZr6p")).thenReturn(responseEntity);

        final var updated = specimenIntegrationService.update(specimenId2, spec);
        final var specUpdateFound = specimenJPARepository.findById(specimenId2);
        // Then - verify the output
        assertThat(updated.getSpecimenId()).isNotNull();
        assertThat(specUpdateFound.get().getMedias()).isEmpty();
        assertThat(specUpdateFound.get().getGeologicalContext().getBed()).isEqualTo("Teddibert");
    }

    @Test
    @DisplayName("JUnit test update specimen without identifications throw exception ")
    @Sql(scripts = "classpath:init_specimen_identification_profile_test.sql")
    void givenSpecimenOK_whenUpdateSpecWithoutIdentifications_thenReturnException() {
        // Given - precondition or setup
        final var spec = Specimen.builder().id(specimenId3)
                .collectionId(collectionId)
                .identifications(Set.of(Identification.builder()
                        .taxon(List.of(Taxon.builder().levelType(MASTER)
                                .scientificName("scientificName")
                                .id(UUID.fromString("91a1bf3f-5cdd-4a9d-860a-c5c1ef22f48b"))
                                .build()))
                        .currentDetermination(true)
                        .id(UUID.fromString("91a1bf3f-5cdd-4a9d-8658-c5c1ef22f49d"))
                        .build()))
                .geologicalContext(GeologicalContext.builder()
                        .bed("Teddibert").build()).build();
        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("respuid").institution(1).role("ADMIN_INSTITUTION").collections(List.of(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331"))).build());

        CollectionManagerBusinessException exception = assertThrows(CollectionManagerBusinessException.class,
                () -> specimenIntegrationService.update(specimenId3, spec));
        // Then - verify the output
        assertThat(exception.getCode()).isEqualTo("ERR_CODE_CM");
    }

    @Test
    void test_buildLocation_should_be_null() {
        assertThat(SpecimenIntegrationServiceImpl.buildLocation(null)).isNull();
    }

    @Test
    void test_buildLocation_should_be_null2() {
        assertThat(SpecimenIntegrationServiceImpl.buildLocation(CollectionEvent.builder().eventDate("eventDate").build())).isNull();
    }

    @Test
    void test_buildLocation_should_not_be_null() {
        String continent = "continent";
        LocationJPA actual = SpecimenIntegrationServiceImpl.buildLocation(CollectionEvent.builder()
                .location(Location.builder().continent(continent).build()).eventDate("eventDate").build());
        assertThat(actual).isNotNull();
        assertThat(actual.getContinent()).isEqualTo(continent);
    }

    @Test
    @Sql(scripts = "classpath:init_data_collection_profile_test.sql")
    void givenSpecimenReviewed_whenIntegartionSpecimen_thenRetrunCollectionIdentifier() {
        // Given - precondition or setup
        doNothing().when(controlAttribut).checkUserRightsOnCollection(collectionId);
        final var spec = Specimen.builder()
                .state(SpecimenStatusEnum.REVIEW.name())
                .collectionId(collectionId)
                .identifications(Set.of(Identification.builder().taxon(List.of(Taxon.builder()
                                        .scientificName("scientificName").build()))
                                .currentDetermination(true).build(),
                        Identification.builder().taxon(List.of(Taxon.builder()
                                        .scientificName("scientificName2").build()))
                                .currentDetermination(false).build()))
                .geologicalContext(GeologicalContext.builder()
                        .bed("Teddibert").build()).build();
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("respuid").institution(1).role("ADMIN_COLLECTION").collections(List.of(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331"))).build());
        // When - action or the behaviour
        var addAsReviewed = specimenIntegrationService.addAsReviewed(spec);
        // Then - verify the output
        assertThat(addAsReviewed.getSpecimenId()).isNotNull();
    }

    @Test
    @Sql(scripts = "classpath:init_data_collection_profile_test.sql")
    void givenSpecimen_whenUpdateAsReviewed_thenReturnSpecimenReviewed() {
        // Given - precondition or setup
        final var specUpdate = Specimen.builder()
                .id(specimenId)
                .collectionId(collectionId)
                .state(SpecimenStatusEnum.REVIEW.name())
                .identifications(Set.of(Identification.builder().taxon(List.of(Taxon.builder()
                                        .levelType(MASTER)
                                        .scientificName("scientificName").build()))
                                .currentDetermination(true).build(),
                        Identification.builder().taxon(List.of(Taxon.builder()
                                        .scientificName("scientificName2").build()))
                                .currentDetermination(false).build()))
                .geologicalContext(GeologicalContext.builder()
                        .bed("Teddibert").build()).build();

        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("respuid").institution(1).role("ADMIN_COLLECTION").collections(List.of(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331"))).build());
        CollectionIdentifier updateAsReviewed = specimenIntegrationService.updateAsReviewed(specimenId, specUpdate);

        // Then - verify the output
        assertThat(updateAsReviewed).isNotNull();
    }
}
