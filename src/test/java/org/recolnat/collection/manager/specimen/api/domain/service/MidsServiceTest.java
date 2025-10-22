package org.recolnat.collection.manager.specimen.api.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recolnat.collection.manager.api.domain.CollectionEvent;
import org.recolnat.collection.manager.api.domain.Identification;
import org.recolnat.collection.manager.api.domain.Location;
import org.recolnat.collection.manager.api.domain.Media;
import org.recolnat.collection.manager.api.domain.Specimen;
import org.recolnat.collection.manager.api.domain.Taxon;
import org.recolnat.collection.manager.service.impl.MidsServiceImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
public class MidsServiceTest {

    @InjectMocks
    private MidsServiceImpl service;

    @Nested
    class Mids {

        private static Stream<Arguments> provideSpecimenTestData() {
            return Stream.of(
                    Arguments.of("12345", "INST123", new UUID(0, 1), true, "Should return true when catalogNumber, institutionId and collectionId are present"),
                    Arguments.of("12345", null, new UUID(0, 1), false, "Should return false when collectionId is missing"),
                    Arguments.of(null, "INST123", new UUID(0, 1), false, "Should return false when catalogNumber is missing"),
                    Arguments.of("12345", "INST123", null, false, "Should return false when collectionId is missing"),
                    Arguments.of(null, null, null, false, "Should return false when both catalogNumber and institutionId are missing")
            );
        }

        private static Stream<Arguments> provideMids2TestData() {
            return Stream.of(
                    // Cas de succès - tous les critères présents
                    Arguments.of(true, true, true, true, true, true, true, true,
                            "Should return true when all criteria are met"),

                    // Cas d'échec - CollectionEvent manquant
                    Arguments.of(false, false, false, false, false, false, false, false,
                            "Should return false when CollectionEvent is null"),

                    // Cas d'échec - chaque critère manquant individuellement
                    Arguments.of(true, false, true, true, true, true, true, false,
                            "Should return false when qualitative location is missing"),
                    Arguments.of(true, true, false, true, true, true, true, false,
                            "Should return false when quantitative location is missing"),
                    Arguments.of(true, true, true, false, true, true, true, false,
                            "Should return false when collecting agent is missing"),
                    Arguments.of(true, true, true, true, false, true, true, false,
                            "Should return false when date collected is missing"),
                    Arguments.of(true, true, true, true, true, false, true, false,
                            "Should return false when collecting number is missing"),
                    Arguments.of(true, true, true, true, true, true, false, false,
                            "Should return false when media is missing"),

                    // Cas d'échec - combinaisons multiples manquantes
                    Arguments.of(true, false, false, true, true, true, true, false,
                            "Should return false when both location types are missing"),
                    Arguments.of(true, true, true, false, false, false, true, false,
                            "Should return false when agent, date and number are missing"),
                    Arguments.of(true, false, false, false, false, false, false, false,
                            "Should return false when most criteria are missing")
            );
        }

        @ParameterizedTest
        @MethodSource("provideSpecimenTestData")
        @DisplayName("Test isMids0Validated with different specimen configurations")
        public void testIsMids0Validated(String catalogNumber, String institutionId, UUID collectionId, boolean expectedResult, String testDescription) {
            // Given
            Specimen specimen = Specimen.builder()
                    .catalogNumber(catalogNumber)
                    .institutionId(institutionId)
                    .collectionId(collectionId)
                    .build();

            // When
            boolean result = service.isMids0Validated(specimen).isEmpty();

            // Then
            assertEquals(expectedResult, result, testDescription);
        }

        @Test
        public void testIsMids1Validated_AllFieldsPresent() {
            Taxon taxon = Taxon.builder()
                    .id(new UUID(0, 1))
                    .scientificName("Scientific Name")
                    .build();

            Identification identification = Identification.builder()
                    .taxon(List.of(taxon))
                    .build();

            Specimen specimen = Specimen.builder()
                    .identifications(Set.of(identification))
                    .modifiedAt(LocalDateTime.now())
                    .build();

            assertTrue(service.isMids1Validated(specimen).isEmpty());
        }

        @Test
        public void testIsMids1Validated_NameMissing() {
            Taxon taxon = Taxon.builder()
                    .id(new UUID(0, 1))
                    .build();

            Identification identification = Identification.builder()
                    .taxon(List.of(taxon))
                    .build();

            Specimen specimen = Specimen.builder()
                    .identifications(Set.of(identification))
                    .modifiedAt(LocalDateTime.now())
                    .build();

            assertFalse(service.isMids1Validated(specimen).isEmpty());
        }

        @ParameterizedTest
        @MethodSource("provideMids2TestData")
        @DisplayName("Test isMids2Validated with different specimen configurations")
        public void testIsMids2Validated(
                boolean hasCollectionEvent,
                boolean hasQualitativeLocation,
                boolean hasQuantitativeLocation,
                boolean hasCollectingAgent,
                boolean hasDateCollected,
                boolean hasCollectingNumber,
                boolean hasMedia,
                boolean expectedResult,
                String testDescription) {

            // Given
            Specimen specimen = createSpecimen(
                    hasCollectionEvent,
                    hasQualitativeLocation,
                    hasQuantitativeLocation,
                    hasCollectingAgent,
                    hasDateCollected,
                    hasCollectingNumber,
                    hasMedia
            );

            // When
            boolean result = service.isMids2Validated(specimen).isEmpty();

            // Then
            assertEquals(expectedResult, result, testDescription);
        }

        private Specimen createSpecimen(
                boolean hasCollectionEvent,
                boolean hasQualitativeLocation,
                boolean hasQuantitativeLocation,
                boolean hasCollectingAgent,
                boolean hasDateCollected,
                boolean hasCollectingNumber,
                boolean hasMedia) {

            Specimen.SpecimenBuilder<?, ?> specimenBuilder = Specimen.builder();

            if (!hasCollectionEvent) {
                return specimenBuilder.collectionEvent(null).build();
            }

            // Création de l'événement de collecte
            CollectionEvent.CollectionEventBuilder<?, ?> eventBuilder = CollectionEvent.builder();

            // Location qualitative
            Location location = null;
            if (hasQualitativeLocation) {
                location = Location.builder()
                        .locality("Sample Locality") // Un seul champ suffit pour avoir une location qualitative
                        .build();
            }
            eventBuilder.location(location);

            // Verbatim locality (alternative pour location qualitative)
            if (hasQualitativeLocation && location == null) {
                eventBuilder.verbatimLocality("Sample Verbatim Locality");
            }

            // Location quantitative
            if (hasQuantitativeLocation) {
                eventBuilder.decimalLatitude(45.5236).decimalLongitude(-122.6750);
            }

            // Collecting agent
            if (hasCollectingAgent) {
                eventBuilder.recordedBy("John Doe");
            }

            // Date collected
            if (hasDateCollected) {
                eventBuilder.eventDate("2023-06-15");
            }

            // Field number
            if (hasCollectingNumber) {
                eventBuilder.fieldNumber("FN-2023-001");
            }

            CollectionEvent event = eventBuilder.build();

            // Médias
            List<Media> mediaList;
            if (hasMedia) {
                Media media = Media.builder()
                        .mediaUrl("https://example.com/image.jpg")
                        .build();
                mediaList = Collections.singletonList(media);
            } else {
                mediaList = Collections.emptyList();
            }

            return specimenBuilder
                    .collectionEvent(event)
                    .medias(mediaList)
                    .build();
        }
    }
}
