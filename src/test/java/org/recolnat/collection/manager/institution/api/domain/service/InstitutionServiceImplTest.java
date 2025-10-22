package org.recolnat.collection.manager.institution.api.domain.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recolnat.collection.manager.api.domain.MidsGroup;
import org.recolnat.collection.manager.repository.jpa.InstitutionRepositoryJPA;
import org.recolnat.collection.manager.service.impl.InstitutionServiceImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InstitutionServiceImplTest {

    @Mock
    private InstitutionRepositoryJPA institutionRepository;

    @InjectMocks
    private InstitutionServiceImpl institutionService;

    private UUID institutionId;

    @BeforeEach
    void setUp() {
        institutionId = UUID.randomUUID();
    }

    @Test
    void getInstitutionMids_shouldReturnCompleteListWithAllIndices() {
        // Given
        List<MidsGroup> groups = Arrays.asList(
                new TestMidsGroup(0, 10L),
                new TestMidsGroup(1, 20L),
                new TestMidsGroup(2, 30L),
                new TestMidsGroup(3, 40L)
        );
        when(institutionRepository.getInstitutionMids(institutionId)).thenReturn(groups);

        // When
        List<Long> result = institutionService.getInstitutionMids(institutionId);

        // Then
        assertThat(result).hasSize(4);
        assertThat(result).containsExactly(10L, 20L, 30L, 40L);
        verify(institutionRepository).getInstitutionMids(institutionId);
    }

    @Test
    void getInstitutionMids_shouldReturnZeroForMissingIndices() {
        // Given
        List<MidsGroup> groups = Arrays.asList(
                new TestMidsGroup(0, 15L),
                new TestMidsGroup(2, 25L)
        );
        when(institutionRepository.getInstitutionMids(institutionId)).thenReturn(groups);

        // When
        List<Long> result = institutionService.getInstitutionMids(institutionId);

        // Then
        assertThat(result).hasSize(4);
        assertThat(result).containsExactly(15L, 0L, 25L, 0L);
        verify(institutionRepository).getInstitutionMids(institutionId);
    }

    @Test
    void getInstitutionMids_shouldReturnAllZerosWhenRepositoryReturnsEmptyList() {
        // Given
        List<MidsGroup> groups = Collections.emptyList();
        when(institutionRepository.getInstitutionMids(institutionId)).thenReturn(groups);

        // When
        List<Long> result = institutionService.getInstitutionMids(institutionId);

        // Then
        assertThat(result).hasSize(4);
        assertThat(result).containsExactly(0L, 0L, 0L, 0L);
        verify(institutionRepository).getInstitutionMids(institutionId);
    }

    @Test
    void getInstitutionMids_shouldHandlePartialData() {
        // Given
        List<MidsGroup> groups = Arrays.asList(
                new TestMidsGroup(1, 100L),
                new TestMidsGroup(3, 200L)
        );
        when(institutionRepository.getInstitutionMids(institutionId)).thenReturn(groups);

        // When
        List<Long> result = institutionService.getInstitutionMids(institutionId);

        // Then
        assertThat(result).hasSize(4);
        assertThat(result).containsExactly(0L, 100L, 0L, 200L);
        verify(institutionRepository).getInstitutionMids(institutionId);
    }

    @Test
    void getInstitutionMids_shouldIgnoreIndicesOutsideRange() {
        // Given
        List<MidsGroup> groups = Arrays.asList(
                new TestMidsGroup(0, 10L),
                new TestMidsGroup(1, 20L),
                new TestMidsGroup(4, 50L), // Index > 3, should be ignored
                new TestMidsGroup(-1, 5L)  // Index < 0, should be ignored
        );
        when(institutionRepository.getInstitutionMids(institutionId)).thenReturn(groups);

        // When
        List<Long> result = institutionService.getInstitutionMids(institutionId);

        // Then
        assertThat(result).hasSize(4);
        assertThat(result).containsExactly(10L, 20L, 0L, 0L);
        verify(institutionRepository).getInstitutionMids(institutionId);
    }

    @Test
    void getInstitutionMids_shouldHandleNullInstitutionId() {
        // Given
        when(institutionRepository.getInstitutionMids(null)).thenReturn(Collections.emptyList());

        // When
        List<Long> result = institutionService.getInstitutionMids(null);

        // Then
        assertThat(result).hasSize(4);
        assertThat(result).containsExactly(0L, 0L, 0L, 0L);
        verify(institutionRepository).getInstitutionMids(null);
    }

    @Test
    void getInstitutionMids_shouldCallRepositoryOnce() {
        // Given
        List<MidsGroup> groups = List.of(new TestMidsGroup(0, 5L));
        when(institutionRepository.getInstitutionMids(institutionId)).thenReturn(groups);

        // When
        institutionService.getInstitutionMids(institutionId);

        // Then
        verify(institutionRepository, times(1)).getInstitutionMids(institutionId);
    }

    @Getter
    @AllArgsConstructor
    static
    class TestMidsGroup implements MidsGroup {
        private int mids;
        private long count;
    }

}
