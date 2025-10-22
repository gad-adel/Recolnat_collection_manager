package org.recolnat.collection.manager.taxon.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recolnat.collection.manager.repository.jpa.TaxonJPARepository;
import org.recolnat.collection.manager.service.impl.TaxonServiceImpl;
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
class TaxonServiceTest {

    @Mock
    private TaxonJPARepository taxonJPARepository;

    @InjectMocks
    private TaxonServiceImpl taxonService;

    static Stream<String> provideQueryCases() {
        return Stream.of(null, "", "   ", "\n", "\r");
    }

    @Test
    void autocompleteFamilyTaxons() {
        int size = 3;

        List<String> mockFamilies = Arrays.asList("Rosaceae", "Rosales", "Rosidae");

        when(taxonJPARepository.findFamiliesStartingWith("Ros%", PageRequest.of(0, size))).thenReturn(mockFamilies);
        List<String> result = taxonService.getFamilyListByPrefix("Ros", size);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(mockFamilies, result);

        verify(taxonJPARepository).findFamiliesStartingWith("Ros%", PageRequest.of(0, size));
    }

    @ParameterizedTest()
    @MethodSource("provideQueryCases")
    void autocompleteFamilyTaxons_queryEmpty(String query) {
        int size = 3;

        List<String> result = taxonService.getFamilyListByPrefix(query, size);
        assertNotNull(result);
        assertTrue(result.isEmpty(), "The result should be an empty list.");
        verify(taxonJPARepository, never()).findFamiliesStartingWith(query, PageRequest.of(0, size));
    }

    @Test
    void autocompleteGenusTaxons() {
        int size = 3;

        List<String> mockFamilies = Arrays.asList("Rosaceae", "Rosales", "Rosidae");

        when(taxonJPARepository.findGenusStartingWithAndFilteredByFamily("Ros%", "", PageRequest.of(0, size))).thenReturn(mockFamilies);
        List<String> result = taxonService.getGenusListByPrefixAndFamily("Ros", "", size);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(mockFamilies, result);

        verify(taxonJPARepository).findGenusStartingWithAndFilteredByFamily("Ros%", "", PageRequest.of(0, size));
    }

    @ParameterizedTest()
    @MethodSource("provideQueryCases")
    void autocompletGenusTaxons_queryEmpty(String query) {
        int size = 3;

        List<String> result = taxonService.getGenusListByPrefixAndFamily(query, "", size);
        assertNotNull(result);
        assertTrue(result.isEmpty(), "The result should be an empty list.");
        verify(taxonJPARepository, never()).findGenusStartingWithAndFilteredByFamily(query, "", PageRequest.of(0, size));
    }

    @Test
    void autocompleteSpecificEpithetTaxons() {
        int size = 3;

        List<String> mockFamilies = Arrays.asList("Rosaceae", "Rosales", "Rosidae");

        when(taxonJPARepository.findSpecificEpithetsStartingWithAndFilteredBy("Ros%", "", PageRequest.of(0, size))).thenReturn(mockFamilies);
        List<String> result = taxonService.getSpecificEpithetByPrefix("Ros", "", size);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(mockFamilies, result);

        verify(taxonJPARepository).findSpecificEpithetsStartingWithAndFilteredBy("Ros%", "", PageRequest.of(0, size));
    }

    @ParameterizedTest()
    @MethodSource("provideQueryCases")
    void autocompleteSpecificEpithetTaxons_queryEmpty(String query) {
        int size = 3;

        List<String> result = taxonService.getSpecificEpithetByPrefix(query, "", size);
        assertNotNull(result);
        assertTrue(result.isEmpty(), "The result should be an empty list.");
        verify(taxonJPARepository, never()).findSpecificEpithetsStartingWithAndFilteredBy(query, "", PageRequest.of(0, size));
    }
}
