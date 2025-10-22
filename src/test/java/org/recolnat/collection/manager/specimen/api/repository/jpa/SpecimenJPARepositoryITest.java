package org.recolnat.collection.manager.specimen.api.repository.jpa;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.NormalCheck;
import org.recolnat.collection.manager.api.domain.Specimen;
import org.recolnat.collection.manager.repository.entity.*;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@ActiveProfiles("test")
@Slf4j
public class SpecimenJPARepositoryITest {
    private static Validator validator;
    // Constantes correspondant aux données DANS LES SCRIPTS SQL
    private final UUID COLLECTION_ID = UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331");
    private final UUID EXISTING_SPECIMEN_ID = UUID.fromString("70029074-fde4-4f85-b3fe-9e25e7bfd9ea");
    private final String EXISTING_CATALOG_NUMBER = "12345"; // Doit correspondre à init_data...
    private final String NON_EXISTING_CATALOG_NUMBER = "99999"; // Un numéro qui n'est PAS dans init_data...
    private final UUID OTHER_COLLECTION_ID = UUID.fromString("aaaaaaaa-f202-4c10-9037-2e2406ce7331"); // Un ID de collection différent
    @Autowired
    private SpecimenJPARepository specimenJPARepository;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void save_should_be_ok() {
        assertThat(
                specimenJPARepository
                        .save(
                                SpecimenJPA.builder()
                                        .createdAt(LocalDateTime.now())
                                        .catalogNumber("CHE029238")
                                        .collectionEvent(
                                                CollectionEventJPA.builder()
                                                        .noCollectionInformation(true)
                                                        .maximumDepthInMeters(Double.parseDouble("-11"))
                                                        .build())
                                        .identifications(Set.of(IdentificationJPA.builder().currentDetermination(true).build()))
                                        .literatures(Set.of(LiteratureJPA.builder().authors("authors").build()))
                                        .medias(Set.of(MediaJPA.builder().contributor("contributor").build()))
                                        .other(OtherJPA.builder().linkOther("linkOther").build())
                                        .build())
                        .getId())
                .isNotNull();
    }

    @Test
    void save_should_be_ok_validate() {
        var violations = validator.validate(Specimen.builder().build(), NormalCheck.class);
        assertThat(violations).isNotEmpty();
        Arrays.asList(violations).stream().peek(v -> log.info(" " + v));
    }
}
