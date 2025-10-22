package org.recolnat.collection.manager.specimen.api.web;

import io.recolnat.model.OperationTypeDTO;
import io.recolnat.model.SpecimenIntegrationMergeRequestDTO;
import io.recolnat.model.SpecimenIntegrationPageResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceElasticTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("int")
@Sql(scripts = {"classpath:clean_data_all_specimen.sql", "classpath:init_data_all_specimen.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_all_specimen.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SpecimenRetrieveAllResourceITest extends AbstractResourceElasticTest {

    @Value(value = "classPath:specimenMerge.json")
    private Resource specimenMergeData;

    @ParameterizedTest()
    @ValueSource(strings = {"DATA_ENTRY"})//,"ADMIN_COLLECTION", "USER_INFRA"
    @DisplayName("Integration test retrieve all specimen")
    void givenListOfSpecime_whenRetrieveAll_thenReturnAllSpecimen(String role) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.fromValue(role).name())
                .collections(List.of())
                .institution(1).build());
        // When - action or the behaviour
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "3")
                        .param("columnSort", "modifiedAt")
                        .param("typeSort", "desc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getTotalPages()).isEqualTo(2);
        assertThat(collect.getSpecimenListResponse()).hasSize(3);
    }

    @Test
    @DisplayName("Integration test All specimen not in institution")
    void givenListOfSpecimen_whenNotInstitution_thenReturnNoSpecimen() throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(2).build());
        // When - action or the behaviour
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).isEmpty();
    }

    @Test
    @DisplayName("Integration test All specimen in institution and operation")
    void givenListOfSpecimen_whenGiveOperation_thenReturnSpecimenByState() throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());
        // When - action or the behaviour
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "3")
                        .param("state", OperationTypeDTO.DRAFT.name())
                        .param("columnSort", "modifiedAt")
                        .param("typeSort", "asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(2);
    }

    @ParameterizedTest()
    @ValueSource(strings = {"asc", "desc"})
    @DisplayName("Integration test All specimen in institution and operation VALIDATE and by catalogNumber sort ")
    void givenListOfSpecimen_whenGiveOperation_thenReturnSpecimenByStateByCollectionNameSort(String sort) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour  ( default sort created at
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("state", OperationTypeDTO.DRAFT.toString())
                        .param("columnSort", "catalogNumber")
                        .param("typeSort", sort)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.totalPages").value("1"))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(2);
        if ("asc".equals(sort)) {
            assertThat(collect.getSpecimenListResponse().get(0).getCatalogNumber()).isEqualTo("UMC-IP 335");
            assertThat(collect.getSpecimenListResponse().get(1).getCatalogNumber()).isEqualTo("UM-VEY 17");
        } else {
            assertThat(collect.getSpecimenListResponse().get(0).getCatalogNumber()).isEqualTo("UM-VEY 17");
            assertThat(collect.getSpecimenListResponse().get(1).getCatalogNumber()).isEqualTo("UMC-IP 335");
        }

    }

    @Test
    @DisplayName("Integration test update multi specimen at the same time")
    void givenListSpecimen_whenPatchMergeSpec_thenReturnSpecMerged() throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());
        var body = objectMapper.readValue(specimenMergeData.getInputStream(), SpecimenIntegrationMergeRequestDTO.class);
        // When - action or the behaviour
        mvc.perform(MockMvcRequestBuilders
                        .patch("/v1/specimens")
                        .with(csrf())
                        .param("id", "9fdca0c7-2712-46a6-aff5-f88fe6999c1e")
                        .param("id", "359eefe3-901a-4faf-bc3e-6f3fa266a465")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                // Then - verify the output
                .andExpect(status().isOk())
                .andExpect(header().string("specimenId", notNullValue()));
    }

    //

    @ParameterizedTest()
    @ValueSource(strings = {"DATA_ENTRY", "ADMIN_COLLECTION", "ADMIN_INSTITUTION"})
    @DisplayName("Integration test update multi specimen at the same time")
    void givenListSpecimen_whenPatchMergeSpec_thenReturnSpecMergedIsOk(String role) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.fromValue(role).name())
                .collections(List.of())
                .institution(1).build());
        var body = objectMapper.readValue(specimenMergeData.getInputStream(), SpecimenIntegrationMergeRequestDTO.class);
        // When - action or the behaviour
        mvc.perform(MockMvcRequestBuilders
                        .patch("/v1/specimens")
                        .with(csrf())
                        .param("id", "44074484-9867-476a-94c4-abd1c0e63d35")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                // Then - verify the output
                .andExpect(status().isOk())
                .andExpect(header().string("specimenId", notNullValue()));
    }

    @Test
    @DisplayName("Integration test update multi specimen have role admin_instit and spec have state reviewed")
    void givenListSpecimen_whenPatchMergeSpec_thenReturnThrowException() throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());
        var body = objectMapper.readValue(specimenMergeData.getInputStream(), SpecimenIntegrationMergeRequestDTO.class);
        // When - action or the behaviour
        mvc.perform(MockMvcRequestBuilders
                        .patch("/v1/specimens")
                        .with(csrf())
                        .param("id", "e7e68a36-c30b-4891-95a0-bdb55ae1e6ee")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                // Then - verify the output
                .andExpect(status().isForbidden())
                .andExpect(header().string("specimenId", nullValue()));
    }

    @ParameterizedTest()
    @ValueSource(strings = {"DATA_ENTRY", "ADMIN_COLLECTION", "USER_INFRA"})
    @DisplayName("Integration test update multi specimen role not admin_institution spec state valid to throw exception")
    void givenListSpecimen_whenPatchMergeSpecRoleNotAdminInst_thenThrowException(String role) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.fromValue(role).name())
                .institution(1).build());
        var body = objectMapper.readValue(specimenMergeData.getInputStream(), SpecimenIntegrationMergeRequestDTO.class);
        // When - action or the behaviour
        mvc.perform(MockMvcRequestBuilders
                        .patch("/v1/specimens")
                        .with(csrf())
                        .param("id", "9fdca0c7-2712-46a6-aff5-f88fe6999c1e")
                        .param("id", "359eefe3-901a-4faf-bc3e-6f3fa266a465")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                // Then - verify the output
                .andExpect(status().isForbidden())
                .andExpect(header().string("specimenId", nullValue()));
    }

    @ParameterizedTest()
    @ValueSource(strings = {"asc", "desc"})
    @DisplayName("Integration test All specimen in institution by state sort")
    void givenListOfSpecimen_whenGiveOperation_thenReturnSpecimenBySort(String sort) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour  ( default sort created at
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("columnSort", "modifiedAt")
                        .param("typeSort", sort)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.totalPages").value("1"))
                .andExpect(jsonPath("$.numberOfElements").value("6"))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse().stream().filter(o -> "DRAFT".equals(o.getState())).count()).isEqualTo(2L);
        assertThat(collect.getSpecimenListResponse().stream().filter(o -> "REVIEW".equals(o.getState())).count()).isEqualTo(1L);
        assertThat(collect.getSpecimenListResponse().stream().filter(o -> "VALID".equals(o.getState())).count()).isEqualTo(3L);
        assertThat(collect.getSpecimenListResponse()).hasSize(6);
        if ("asc".equals(sort)) {
            assertThat(collect.getSpecimenListResponse().get(0).getId()).isEqualTo("9fdca0c7-2712-46a6-aff5-f88fe6999c1e");
            assertThat(collect.getSpecimenListResponse().get(1).getId()).isEqualTo("44074484-9867-476a-94c4-abd1c0e63d35");
            assertThat(collect.getSpecimenListResponse().get(2).getId()).isEqualTo("0b106e72-daa1-4942-a50b-7bd1ca9446ac");
            assertThat(collect.getSpecimenListResponse().get(3).getId()).isEqualTo("bb4b6db8-4fee-40eb-9a0c-3fb57fdbf940");
            assertThat(collect.getSpecimenListResponse().get(4).getId()).isEqualTo("359eefe3-901a-4faf-bc3e-6f3fa266a465");
            assertThat(collect.getSpecimenListResponse().get(5).getId()).isEqualTo("e7e68a36-c30b-4891-95a0-bdb55ae1e6ee");
        } else {
            assertThat(collect.getSpecimenListResponse().get(0).getId()).isEqualTo("e7e68a36-c30b-4891-95a0-bdb55ae1e6ee");
            assertThat(collect.getSpecimenListResponse().get(1).getId()).isEqualTo("359eefe3-901a-4faf-bc3e-6f3fa266a465");
            assertThat(collect.getSpecimenListResponse().get(2).getId()).isEqualTo("bb4b6db8-4fee-40eb-9a0c-3fb57fdbf940");
            assertThat(collect.getSpecimenListResponse().get(3).getId()).isEqualTo("0b106e72-daa1-4942-a50b-7bd1ca9446ac");
            assertThat(collect.getSpecimenListResponse().get(4).getId()).isEqualTo("9fdca0c7-2712-46a6-aff5-f88fe6999c1e");
            assertThat(collect.getSpecimenListResponse().get(5).getId()).isEqualTo("44074484-9867-476a-94c4-abd1c0e63d35");
        }
    }

    @ParameterizedTest()
    @ValueSource(strings = {"asc", "desc"})
    @DisplayName("Integration test All specimen in institution by scientificName sort")
    void givenListOfSpecimen_whenGiveOperation_thenReturnSpecimenByscientificNameSort(String sort) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour  ( default sort created at
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("columnSort", "name")
                        .param("typeSort", sort)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.totalPages").value("1"))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(6);
        assertThat(collect.getSpecimenListResponse().stream().filter(o -> "DRAFT".equals(o.getState())).count()).isEqualTo(2L);
        assertThat(collect.getSpecimenListResponse().stream().filter(o -> "REVIEW".equals(o.getState())).count()).isEqualTo(1L);
        assertThat(collect.getSpecimenListResponse().stream().filter(o -> "VALID".equals(o.getState())).count()).isEqualTo(3L);
        if ("asc".equals(sort)) {
            assertThat(collect.getSpecimenListResponse().get(0).getScientificName()).isEqualTo("a name");
            assertThat(collect.getSpecimenListResponse().get(1).getScientificName()).isEqualTo("Canis lupus");
            assertThat(collect.getSpecimenListResponse().get(2).getScientificName()).isEqualTo("scientificName");
            assertThat(collect.getSpecimenListResponse().get(3).getScientificName()).isEqualTo("scientificName");
            assertThat(collect.getSpecimenListResponse().get(4).getScientificName()).isEqualTo("scientificName");
            assertThat(collect.getSpecimenListResponse().get(5).getScientificName()).isEqualTo("Scirpus triqueter");
        } else {
            assertThat(collect.getSpecimenListResponse().get(0).getScientificName()).isEqualTo("Scirpus triqueter");
            assertThat(collect.getSpecimenListResponse().get(1).getScientificName()).isEqualTo("scientificName");
            assertThat(collect.getSpecimenListResponse().get(2).getScientificName()).isEqualTo("scientificName");
            assertThat(collect.getSpecimenListResponse().get(3).getScientificName()).isEqualTo("scientificName");
            assertThat(collect.getSpecimenListResponse().get(4).getScientificName()).isEqualTo("Canis lupus");
            assertThat(collect.getSpecimenListResponse().get(5).getScientificName()).isEqualTo("a name");
        }

    }


    @ParameterizedTest()
    @ValueSource(strings = {"asc", "desc"})
    @DisplayName("Integration test All specimen in institution by collectionNameFr sort")
    void givenListOfSpecimen_whenGiveOperation_thenReturnSpecimenBycollectionSort(String sort) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour  ( default sort created at
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("columnSort", "collection")
                        .param("typeSort", sort)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.totalPages").value("1"))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(6);
        assertThat(collect.getSpecimenListResponse().stream().filter(o -> "DRAFT".equals(o.getState())).count()).isEqualTo(2L);
        assertThat(collect.getSpecimenListResponse().stream().filter(o -> "REVIEW".equals(o.getState())).count()).isEqualTo(1L);
        assertThat(collect.getSpecimenListResponse().stream().filter(o -> "VALID".equals(o.getState())).count()).isEqualTo(3L);

        if ("asc".equals(sort)) {
            assertThat(collect.getSpecimenListResponse().get(0).getCollectionName()).isEqualTo("botanique");
            assertThat(collect.getSpecimenListResponse().get(1).getCollectionName()).isEqualTo("botanique");
            assertThat(collect.getSpecimenListResponse().get(2).getCollectionName()).isEqualTo("botanique");
            assertThat(collect.getSpecimenListResponse().get(3).getCollectionName()).isEqualTo("Tunicier");
            assertThat(collect.getSpecimenListResponse().get(4).getCollectionName()).isEqualTo("Tunicier");
            assertThat(collect.getSpecimenListResponse().get(5).getCollectionName()).isEqualTo("Tunicier");
        } else {
            assertThat(collect.getSpecimenListResponse().get(0).getCollectionName()).isEqualTo("Tunicier");
            assertThat(collect.getSpecimenListResponse().get(1).getCollectionName()).isEqualTo("Tunicier");
            assertThat(collect.getSpecimenListResponse().get(2).getCollectionName()).isEqualTo("Tunicier");
            assertThat(collect.getSpecimenListResponse().get(3).getCollectionName()).isEqualTo("botanique");
            assertThat(collect.getSpecimenListResponse().get(4).getCollectionName()).isEqualTo("botanique");
            assertThat(collect.getSpecimenListResponse().get(5).getCollectionName()).isEqualTo("botanique");
        }
    }

    @ParameterizedTest()
    @ValueSource(strings = {"CHE033773", "UM-VEY 17"})
    void givenSearchWord_whenSearchSpecimen_thenReturnAllSpecimen(String searchWord) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("columnSort", "modifiedAt")
                        .param("typeSort", "asc")
                        .param("q", searchWord)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.totalPages").value("1"))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(1);
    }

    @ParameterizedTest()
    @ValueSource(strings = {"Cyperaceae", "Canidae"})
    void givenFamily_whenSearchSpecimen_thenReturnAllSpecimen(String family) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("columnSort", "modifiedAt")
                        .param("typeSort", "asc")
                        .param("family", family)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.totalPages").value("1"))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(1);
    }

    @ParameterizedTest()
    @ValueSource(strings = {"Scirpus", "Canis"})
    void givenGenus_whenSearchSpecimen_thenReturnAllSpecimen(String genus) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("columnSort", "modifiedAt")
                        .param("typeSort", "asc")
                        .param("genus", genus)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.totalPages").value("1"))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(1);
    }

    @ParameterizedTest()
    @ValueSource(strings = {"triqueter", "lupus"})
    void givenSpecificEpithet_whenSearchSpecimen_thenReturnAllSpecimen(String specificEpithet) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("columnSort", "modifiedAt")
                        .param("typeSort", "asc")
                        .param("specific_epithet", specificEpithet)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.totalPages").value("1"))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(1);
    }

    @ParameterizedTest()
    @ValueSource(strings = {"50f4978a-da62-4fde-8f38-5003bd43ff64"})
    void givenInstitutionId_whenSearchSpecimen_thenReturnAllSpecimen(String institutionId) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("columnSort", "modifiedAt")
                        .param("typeSort", "asc")
                        .param("institution_id", institutionId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.numberOfElements").value("6"))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(6);
    }

    @ParameterizedTest()
    @CsvSource({"8342cf1d-f202-4c10-9037-2e2406ce7331,3", "9a342a92-6fe8-48d3-984e-d1731c051666,3"})
    void givenCollectionId_whenSearchSpecimen_thenReturnAllSpecimen(String collectionId, Integer expected) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("columnSort", "modifiedAt")
                        .param("typeSort", "asc")
                        .param("collection_id", collectionId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.numberOfElements").value(expected))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(expected);
    }

    @ParameterizedTest()
    @ValueSource(strings = {"COLL1", "COLL2"})
    void givenCollectionCode_whenSearchSpecimen_thenReturnAllSpecimen(String collectionCode) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("columnSort", "modifiedAt")
                        .param("typeSort", "asc")
                        .param("collection_code", collectionCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.totalPages").value("1"))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(1);
    }

    @ParameterizedTest()
    @ValueSource(strings = {"France", "China"})
    void givenCountry_whenSearchSpecimen_thenReturnAllSpecimen(String country) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("columnSort", "modifiedAt")
                        .param("typeSort", "asc")
                        .param("country", country)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.totalPages").value("1"))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(1);
    }

    @ParameterizedTest()
    @ValueSource(strings = {"Asia", "Europe"})
    void givenContinent_whenSearchSpecimen_thenReturnAllSpecimen(String continent) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("columnSort", "modifiedAt")
                        .param("typeSort", "asc")
                        .param("continent", continent)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.totalPages").value("1"))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(1);
    }

//    @ParameterizedTest()
//    @CsvSource({"2024-06-31,2", "2025-09-15,0"})
//    void givenStartDate_whenSearchSpecimen_thenReturnAllSpecimen(String startDate, Integer expected) throws Exception {
//        // Given - precondition or setup
//        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
//                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
//                .role(RoleEnum.ADMIN_INSTITUTION.name())
//                .institution(1).build());
//
//        // When - action or the behaviour
//        var resp = mvc.perform(MockMvcRequestBuilders
//                        .get("/v1/specimens")
//                        .param("page", "0")
//                        .param("size", "6")
//                        .param("columnSort", "modifiedAt")
//                        .param("typeSort", "asc")
//                        .param("start_date", startDate)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").exists())
//                .andExpect(jsonPath("$.numberOfElements").value(expected))
//                .andReturn();
//        // Then - verify the output
//        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
//                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
//        assertThat(collect.getSpecimenIntegrationResponse()).hasSize(expected);
//    }
//
//    @ParameterizedTest()
//    @CsvSource({"2024-12-31,1", "2025-12-31,2"})
//    void givenEndDate_whenSearchSpecimen_thenReturnAllSpecimen(String endDate, Integer expected) throws Exception {
//        // Given - precondition or setup
//        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
//                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
//                .role(RoleEnum.ADMIN_INSTITUTION.name())
//                .institution(1).build());
//
//        // When - action or the behaviour
//        var resp = mvc.perform(MockMvcRequestBuilders
//                        .get("/v1/specimens")
//                        .param("page", "0")
//                        .param("size", "6")
//                        .param("columnSort", "modifiedAt")
//                        .param("typeSort", "asc")
//                        .param("end_date", endDate)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").exists())
//                .andExpect(jsonPath("$.numberOfElements").value(expected))
//                .andReturn();
//        // Then - verify the output
//        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
//                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
//        assertThat(collect.getSpecimenIntegrationResponse()).hasSize(expected);
//    }

    @ParameterizedTest()
    @CsvSource({"Jane Doe,1", "John Doe,0"})
    void givenCollector_whenSearchSpecimen_thenReturnAllSpecimen(String collector, Integer expected) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("columnSort", "modifiedAt")
                        .param("typeSort", "asc")
                        .param("collector", collector)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.numberOfElements").value(expected))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(expected);
    }

    @ParameterizedTest()
    @CsvSource({"Collection 1,3", "Collection 2,2"})
    void givenNominativeCollection_whenSearchSpecimen_thenReturnAllSpecimen(String nominativeCollection, Integer expected) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("columnSort", "modifiedAt")
                        .param("typeSort", "asc")
                        .param("nominative_collection", nominativeCollection)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.numberOfElements").value(expected))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(expected);
    }

    @ParameterizedTest()
    @CsvSource({"Sur l'étagère,1", "Dans le batiment B,2"})
    void givenStorageName_whenSearchSpecimen_thenReturnAllSpecimen(String storageName, Integer expected) throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        // When - action or the behaviour
        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens")
                        .param("page", "0")
                        .param("size", "6")
                        .param("columnSort", "modifiedAt")
                        .param("typeSort", "asc")
                        .param("storage_name", storageName)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.numberOfElements").value(expected))
                .andReturn();
        // Then - verify the output
        SpecimenIntegrationPageResponseDTO collect = objectMapper.readValue(resp.getResponse()
                .getContentAsByteArray(), SpecimenIntegrationPageResponseDTO.class);
        assertThat(collect.getSpecimenListResponse()).hasSize(expected);
    }
}
