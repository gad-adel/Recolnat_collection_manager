package org.recolnat.collection.manager.specimen.api.map;

import io.recolnat.model.IdentificationDTO;
import io.recolnat.model.SpecimenIntegrationRequestDTO;
import io.recolnat.model.TaxonDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recolnat.collection.manager.api.domain.Identification;
import org.recolnat.collection.manager.api.domain.Specimen;
import org.recolnat.collection.manager.common.mapper.CollectionEventMapper;
import org.recolnat.collection.manager.common.mapper.CollectionEventMapperImpl;
import org.recolnat.collection.manager.common.mapper.CollectionIdentifierMapper;
import org.recolnat.collection.manager.common.mapper.CollectionIdentifierMapperImpl;
import org.recolnat.collection.manager.common.mapper.GeologicalContextMapper;
import org.recolnat.collection.manager.common.mapper.GeologicalContextMapperImpl;
import org.recolnat.collection.manager.common.mapper.IdentificationMapper;
import org.recolnat.collection.manager.common.mapper.IdentificationMapperImpl;
import org.recolnat.collection.manager.common.mapper.SpecimenMapperImpl;
import org.recolnat.collection.manager.repository.entity.CollectionJPA;
import org.recolnat.collection.manager.repository.entity.SpecimenJPA;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class SpecimenJPAMapperTest {

    @InjectMocks
    private SpecimenMapperImpl specimenJPAMapper;

    @SuppressWarnings("unused")
    @Spy
    private IdentificationMapper identificationMapper = new IdentificationMapperImpl();

    @SuppressWarnings("unused")
    @Spy
    private GeologicalContextMapper geologicalContextMapper = new GeologicalContextMapperImpl();

    @SuppressWarnings("unused")
    @Spy
    private CollectionEventMapper collectionEventMapper = new CollectionEventMapperImpl();

    @SuppressWarnings("unused")
    @Spy
    private CollectionIdentifierMapper collectionIdentifierMapper = new CollectionIdentifierMapperImpl();

    @Test
    void mapToSpecimenJpa_is_ok() {
        // Given
        var uid = "guest_1";
        Specimen specimen = Specimen.builder()
                .catalogNumber("CHE029238").build();

        // When
        SpecimenJPA specimenJPA = specimenJPAMapper.mapToSpecimenJpa(specimen, uid);

        // Then
        assertThat(specimenJPA.getCreatedBy()).isEqualTo(uid);
    }


    @Test
    void mapToSpecimenDomain_is_ok() {
        // Given
        var col = CollectionJPA.builder()
                .id(UUID.fromString("f5fd933b-daf2-465e-b323-0a20635b9ba6"))
                .collectionNameFr("botanique")
                .build();
        SpecimenJPA specimenJpa = SpecimenJPA.builder()
                .collection(col)
                .catalogNumber("CHE029238").build();
        // When
        Specimen mapJpaToSpecimen = specimenJPAMapper.mapJpaToSpecimen(specimenJpa);

        // Then
        assertThat(mapJpaToSpecimen).isNotNull();
    }

    @Test
    void mapDtoToSpecimenDomain_is_ok() {
        // Given
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        var identification = new IdentificationDTO()
                .dateIdentified(LocalDate.of(2022, 5, 10).format(pattern))
                .currentDetermination(true)
                .taxon(List.of(new TaxonDTO()
                        .scientificName("Dendroxena quadrimaculata")
                        .scientificNameAuthorship("(Scopoli, 1771)")
                        .kingdom("Animalia Linnaeus, 1758")
                        .phylum("Arthropoda Latreille, 1829")
                        .vernacularName("Silphe à quatre points")
                        .taxonRemarks("Observé le Mardi 10 Mai à Paris 14")));
        var specimenDto = new SpecimenIntegrationRequestDTO()
                .catalogNumber(null)
                .identifications(List.of(identification));

        // When

        Specimen specimenDomain = specimenJPAMapper.mapDtoToSpecimen(specimenDto);

        // Then
        assertThat(specimenDomain.getIdentifications()).hasSize(1);
        Identification ident = specimenDomain.getIdentifications().iterator().next();
        assertThat(ident.getDateIdentified()).isEqualTo(LocalDate.of(2022, 5, 10));
    }

}
