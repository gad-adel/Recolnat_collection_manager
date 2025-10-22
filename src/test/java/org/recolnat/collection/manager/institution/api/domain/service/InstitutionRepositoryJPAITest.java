package org.recolnat.collection.manager.institution.api.domain.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.enums.PartnerType;
import org.recolnat.collection.manager.repository.entity.InstitutionJPA;
import org.recolnat.collection.manager.repository.jpa.InstitutionRepositoryJPA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * test dans Base H2 . profile test
 */
@DataJpaTest
@ActiveProfiles("test")
public class InstitutionRepositoryJPAITest {

    @Autowired
    private InstitutionRepositoryJPA institutionRepositoryJPA;

    @Test
    @DisplayName("Junit test add new Intitution")
    void givenInstitution_whenSave_thenReturnInstitutionJPA() {
        // Given - precondition or setup
        var institution = InstitutionJPA.builder()
                .code("code_1")
                .mandatoryDescription("mandatory description")
                .partnerType(PartnerType.DATA_PROVIDER)
                .name("institution_name").createdAt(LocalDateTime.now())
                .createdBy("ADMIN INST").build();
        // When - action or the behaviour
        var saveInstitution = institutionRepositoryJPA.save(institution);
        // Then - verify the output
        assertThat(saveInstitution.getId()).isNotNull();
    }

    @Test
    @DisplayName("Retrieve all Intitution by pagination")
    @Sql(scripts = "classpath:init_ref_institution.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:clean_institution.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void find_all_by_page() {
        var resp = institutionRepositoryJPA.findAll(PageRequest.of(0, 10));
        assertThat(resp.getTotalPages()).isEqualTo(9);
        assertThat(resp.getTotalElements()).isGreaterThanOrEqualTo(86);
    }

    @Test
    @DisplayName("get institutions by codes")
    @Sql(scripts = "classpath:init_ref_institution.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:clean_institution.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenListInstitution_whenFetchToSync_thenReturnListInstitution() {
        // Given - precondition or setup
        List<InstitutionJPA> findsByCodes = institutionRepositoryJPA.findInstitutionsByCodesIgnoreCase(List.of("MNHN", "BESA"));

        // Then - verify the output
        assertThat(findsByCodes).isNotEmpty();
        String s1 = new String(findsByCodes.get(0).getName().getBytes(), StandardCharsets.UTF_8);
        String s2 = new String(findsByCodes.get(1).getName().getBytes(), StandardCharsets.UTF_8);
        assertEquals(s1, "Muséum National d'Histoire Naturelle");
        assertEquals(s2, "Muséum d'Histoire Naturelle de Besançon");

    }
}
