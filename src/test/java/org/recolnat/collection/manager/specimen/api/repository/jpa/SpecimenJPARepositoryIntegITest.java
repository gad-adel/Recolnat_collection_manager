package org.recolnat.collection.manager.specimen.api.repository.jpa;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;
import org.recolnat.collection.manager.repository.entity.GeologicalContextJPA;
import org.recolnat.collection.manager.repository.entity.IdentificationJPA;
import org.recolnat.collection.manager.repository.entity.SpecimenJPA;
import org.recolnat.collection.manager.repository.entity.TaxonJPA;
import org.recolnat.collection.manager.repository.jpa.CollectionJPARepository;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("test")
@DataJpaTest
@Slf4j
public class SpecimenJPARepositoryIntegITest {
    TaxonJPA taxonJPOne, taxonJPTwo;
    @Autowired
    private CollectionJPARepository collectionJPARepository;
    @Autowired
    private SpecimenJPARepository specimenJPARepository;

    @BeforeEach
    void init_taxon() {
        taxonJPOne = TaxonJPA.builder()
                .scientificName("Dendroxena quadrimaculata")
                .scientificNameAuthorship("(Scopoli, 1771)")
                .kingdom("Animalia Linnaeus, 1758")
                .phylum("Arthropoda Latreille, 1829")
                .vernacularName("Silphe à quatre points")
                .taxonRemarks("Observé le Mardi 10 Mai à Paris 14").build();
        taxonJPTwo = TaxonJPA.builder()
                .scientificName("Dendroxena sexcarinata")
                .scientificNameAuthorship("Silphinae Latreille, 1806")
                .kingdom("Animalia")
                .phylum("Arthropoda")
                .vernacularName("Silphe à quatre points")
                .taxonRemarks("Observé le Mardi 10 Mai à Paris 14").build();

    }

    @Test
    void save_should_be_ok() {

        SpecimenJPA specimen = SpecimenJPA.builder()
                .createdAt(LocalDateTime.now())
                .catalogNumber("CHE029238")
                .geologicalContext(GeologicalContextJPA.builder().bed("bed").build()).build();

        CollectionJPA col = CollectionJPA.builder().collectionNameEn("collectionNameEn").build();

        specimen.setCollection(col);

        final var colSaved = collectionJPARepository.save(col);

        log.info("===============display collection saved : {}", colSaved.getId());

        assertThat(colSaved.getId()).isNotNull();
    }

    @Test
    void givenSpecimenJPAWithIden_whenSave_thenReturnSpecimenJPA() {
        // Given - precondition or setup
        var taxonJPThree = TaxonJPA.builder()
                .scientificName("Xylodrepa sexcarinata")
                .scientificNameAuthorship("Xylodrepa sexcarinata Motschulsky, 1860")
                .kingdom("Animalia")
                .phylum("Arthropoda")
                .vernacularName("Silphe à quatre points")
                .taxonRemarks("Observé le Mardi 10 Mai à Paris 14").build();

        var identificationJPAOne = IdentificationJPA.builder()
                .dateIdentified(LocalDate.now())
                .currentDetermination(false)
                .taxon(List.of(taxonJPOne, taxonJPTwo)).build();
        var identificationJPATwo = IdentificationJPA.builder()
                .dateIdentified(LocalDate.now())
                .currentDetermination(false)
                .taxon(List.of(taxonJPThree)).build();
        var specimen = SpecimenJPA.builder()
                .createdAt(LocalDateTime.now())
                .catalogNumber("CHE029238")
                .identifications(Set.of(identificationJPAOne, identificationJPATwo))
                .geologicalContext(GeologicalContextJPA.builder().bed("bed").build()).build();
        // When - action or the behaviour
        SpecimenJPA save = specimenJPARepository.save(specimen);
        // Then - verify the output
        var identifications = save.getIdentifications();
        var taxons = identifications.stream().map(IdentificationJPA::getTaxon)
                .collect(Collectors.toUnmodifiableList());
        assertThat(save.getId()).isNotNull();
        assertThat(taxons).hasSize(2);
    }

    @Test
    void givenSpecimenJPAWithIden_whenUpdate_thenReturnSpecimenJPA() {
        // Given - precondition or setup
        var taxons = new ArrayList<TaxonJPA>();
        Set<IdentificationJPA> identificationJPAs = new HashSet<>();
        taxons.add(taxonJPOne);
        var identificationJPAOne = IdentificationJPA.builder()
                .dateIdentified(LocalDate.now())
                .currentDetermination(false)
                .taxon(taxons).build();
        identificationJPAs.add(identificationJPAOne);
        var specimen = SpecimenJPA.builder()
                .createdAt(LocalDateTime.now())
                .catalogNumber("CHE029238")
                .identifications(identificationJPAs)
                .geologicalContext(GeologicalContextJPA.builder().bed("bed").build()).build();
        SpecimenJPA specimenJPASave = specimenJPARepository.save(specimen);
        // When - action or the behaviour
        SpecimenJPA specimenJPAUpdate = specimenJPARepository.findById(specimenJPASave.getId())
                .orElseThrow(() -> new RuntimeException("Specimen not found {}"));
        var identificationJPAUpdate = specimenJPAUpdate.getIdentifications().stream().findFirst().get();
        taxons.add(taxonJPTwo);
        identificationJPAUpdate.setTaxon(taxons);
        specimenJPARepository.save(specimenJPAUpdate);
        // Then - verify the output
        assertThat(identificationJPAUpdate.getTaxon()).hasSize(2);
    }
}
