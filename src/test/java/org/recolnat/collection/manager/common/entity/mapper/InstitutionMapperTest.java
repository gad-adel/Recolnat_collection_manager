package org.recolnat.collection.manager.common.entity.mapper;

import io.recolnat.model.InstitutionRequestDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recolnat.collection.manager.api.domain.enums.PartnerType;
import org.recolnat.collection.manager.common.mapper.InstitutionMapper;
import org.recolnat.collection.manager.repository.entity.InstitutionJPA;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InstitutionMapperTest {

    private static InstitutionMapper institutionMapper;

    @BeforeAll
    public static void init() {
        institutionMapper = Mappers.getMapper(InstitutionMapper.class);
    }

    @Test
    @DisplayName("JUnit test mappe institutionRequestDTO to institution ")
    void givenInstitutionRequestDTO_whenMappe_thenReturnInstitution() {
        // Given - precondition or setup
        var institutionRequestDTOMock = new InstitutionRequestDTO()
                .code("code_institution")
                .name("name_institution");
        // When - action or the behaviour
        var institution = institutionMapper.dtoToInstitution(institutionRequestDTOMock);
        // Then - verify the output
        assertThat(institution.getName()).isEqualTo("name_institution");
    }

    @Test
    @DisplayName("JUnit test mappe institutionJPA to institution ")
    void givenInstitutionJPA_whenMappe_thenReturnInstitution() {
        // Given - precondition or setup
        var institutionMock = InstitutionJPA.builder()
                .id(110)
                .code("LYJB")
                .name("Jardin botanique de Lyon")
                .partnerType(PartnerType.PARTNER).build();
        // When - action or the behaviour
        var institution = institutionMapper.toInstitution(institutionMock);
        // Then - verify the output
        assertThat(institution.getPartnerTypeEn()).isEqualTo("Partner");
        assertThat(institution.getPartnerTypeFr()).isEqualTo("Partenaire");
    }
}
