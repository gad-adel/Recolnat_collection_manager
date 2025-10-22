package org.recolnat.collection.manager.specimen.api.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recolnat.collection.manager.repository.jpa.CollectionEventJPARepository;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.recolnat.collection.manager.service.impl.SpecimenIntegrationServiceImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(MockitoExtension.class)
public class SpecimenServiceITest {

    @Mock
    private SpecimenJPARepository specimenJPARepository;


    @Mock
    private CollectionEventJPARepository collectionEventJPARepository;

    @InjectMocks
    private SpecimenIntegrationServiceImpl specimenIntegrationService;

    static Stream<String> provideQueryCases() {
        return Stream.of("", "   ", "\n", "\r", null);
    }

    @Test
    void autocompleteNominativeCollections() {
        int size = 3;

        List<String> mockCollections = Arrays.asList("Collection 1", "Collection 2", "Collection 3");

        when(specimenJPARepository.findNominativeCollectionsStartingWith("Coll%", PageRequest.of(0, size))).thenReturn(mockCollections);
        List<String> result = specimenIntegrationService.getNominativeCollections("Coll", size);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(mockCollections, result);

        verify(specimenJPARepository).findNominativeCollectionsStartingWith("Coll%", PageRequest.of(0, size));
    }

    @Test
    void autocompleteCountries() {
        int size = 3;

        List<String> mockFamilies = Arrays.asList("Armenia", "Austria", "Albania");

        when(collectionEventJPARepository.findCountriesStartingWith("A%", PageRequest.of(0, size))).thenReturn(mockFamilies);
        List<String> result = specimenIntegrationService.getCountriesByPrefix("A", size);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(mockFamilies, result);

        verify(collectionEventJPARepository).findCountriesStartingWith("A%", PageRequest.of(0, size));
    }

    @ParameterizedTest()
    @MethodSource("provideQueryCases")
    void autocompleteCountries_queryEmpty(String query) {
        int size = 3;

        List<String> result = specimenIntegrationService.getCountriesByPrefix(query, size);
        assertNotNull(result);
        assertTrue(result.isEmpty(), "The result should be an empty list.");
        verify(collectionEventJPARepository, never()).findCountriesStartingWith(query, PageRequest.of(0, size));
    }

    @Test
    void autocompleteContinents() {
        int size = 3;

        List<String> mockFamilies = Arrays.asList("America", "Africa", "Australia");

        when(collectionEventJPARepository.findContinentsStartingWith("A%", PageRequest.of(0, size))).thenReturn(mockFamilies);
        List<String> result = specimenIntegrationService.getContinentsByPrefix("A", size);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(mockFamilies, result);

        verify(collectionEventJPARepository).findContinentsStartingWith("A%", PageRequest.of(0, size));
    }

    @ParameterizedTest()
    @MethodSource("provideQueryCases")
    void autocompleteContinents_queryEmpty(String query) {
        int size = 3;

        List<String> result = specimenIntegrationService.getContinentsByPrefix(query, size);
        assertNotNull(result);
        assertTrue(result.isEmpty(), "The result should be an empty list.");
        verify(collectionEventJPARepository, never()).findContinentsStartingWith(query, PageRequest.of(0, size));
    }

    @Test
    void autocompleteCollectors() {
        int size = 3;

        List<String> mockFamilies = Arrays.asList("Philip, P.", "Patrick, L.", "Pierre, A.");

        when(collectionEventJPARepository.findRecordersStartingWith("P%", PageRequest.of(0, size))).thenReturn(mockFamilies);
        List<String> result = specimenIntegrationService.getRecordersByPrefix("P", size);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(mockFamilies, result);

        verify(collectionEventJPARepository).findRecordersStartingWith("P%", PageRequest.of(0, size));
    }

    @ParameterizedTest()
    @MethodSource("provideQueryCases")
    void autocompleteCollectors_queryEmpty(String query) {
        int size = 3;

        List<String> result = specimenIntegrationService.getRecordersByPrefix(query, size);
        assertNotNull(result);
        assertTrue(result.isEmpty(), "The result should be an empty list.");
        verify(collectionEventJPARepository, never()).findRecordersStartingWith(query, PageRequest.of(0, size));
    }
}
