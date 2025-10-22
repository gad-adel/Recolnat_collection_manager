package org.recolnat.collection.manager.web;

import io.recolnat.model.ArticleResponseDTO;
import io.recolnat.model.CollectionDetailPublicDTO;
import io.recolnat.model.CollectionPublicDTO;
import io.recolnat.model.InstitutionDTO;
import io.recolnat.model.InstitutionDetailPublicResponseDTO;
import io.recolnat.model.InstitutionResponseDTO;
import io.recolnat.model.PublicSpecimenDTO;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceDBTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("int")
public class PublicResourceITest extends AbstractResourceDBTest {

    private static final String SPECIMEN_ID = "70029074-fde4-4f85-b3fe-9e25e7bfd9ef";
    private static final String INST_CJBN_ID = "21210632-5d32-42ba-af49-10142142ddf7";

    @Value(value = "classpath:publicSpecimen.json")
    private Resource expectedResponseJson;

    @Test
    @Sql(scripts = {"classpath:clean_data_collection.sql", "classpath:init_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenCollectionId_whenRetrievePublicCollectionById_thenReturnCollection() throws Exception {
        // Given - precondition or setup
        // When - action or the behaviour
        var resp = mvc.perform(get("/v1/public/collections/{id}", COLLECTION_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists()).andReturn();
        // Then - verify the output
        var actuelResponse = objectMapper.readValue(resp.getResponse().getContentAsString(), CollectionPublicDTO.class);
        assertThat(actuelResponse).isNotNull();
        assertThat(actuelResponse.getCollectionNameFr()).isEqualTo("Tunicier");
    }

    @Test
    @Sql(scripts = {"classpath:init_data_public_specimen.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_data_all_specimen.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenSpecimen_whenGetPublicSpecimen_thenReturnDetail() throws Exception {
        // Given - precondition or setup
        // When - action or the behaviour
        var resp = mvc.perform(get("/v1/public/specimens/{specimenId}", SPECIMEN_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists()).andReturn();
        // Then - verify the output
        var actualResponseDTO = objectMapper.readValue(resp.getResponse().getContentAsString(StandardCharsets.UTF_8), PublicSpecimenDTO.class);
        var expResponseDTO = objectMapper.readValue(new InputStreamReader(expectedResponseJson.getInputStream(), StandardCharsets.UTF_8), PublicSpecimenDTO.class);

        AssertionsForClassTypes.assertThat(actualResponseDTO).isEqualTo(expResponseDTO);
    }

    @Test
    @Sql(scripts = {"classpath:init_data_institution.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_institution.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenInstitution_whenGetPublicInstitutions_thenReturnInstitution() throws Exception {
        // Given - precondition or setup
        // When - action or the behaviour
        var resp = mvc.perform(get("/v1/public/institutions")
                        .param("page", "0")
                        .param("size", "3")
                        .param("partnerType", "DATA_PROVIDER")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists()).andReturn();
        // Then - verify the output
        var actuelResponse = objectMapper.readValue(resp.getResponse().getContentAsString(), InstitutionResponseDTO.class);
        assertThat(actuelResponse).isNotNull();
        assertThat(actuelResponse.getInstitutions()).hasSize(2);
    }

    @Test
    @Sql(scripts = {"classpath:init_data_institution.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_institution.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenInstitution_whenGetPublicInstitutionDetails_thenReturnDetails() throws Exception {
        // Given - precondition or setup
        // When - action or the behaviour
        var resp = mvc.perform(get("/v1/public/institutions/{institutionId}", INST_CJBN_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists()).andReturn();
        // Then - verify the output
        var actuelResponse = objectMapper.readValue(resp.getResponse().getContentAsString(), InstitutionDetailPublicResponseDTO.class);
        assertThat(actuelResponse).isNotNull();
        assertThat(actuelResponse.getCode()).isEqualTo("CJBN");
    }

    @Test
    @Sql(scripts = {"classpath:init_articles.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_articles.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenInstitution_whengetPublicArticleById_thenReturnArticle() throws Exception {
        // Given - precondition or setup
        // When - action or the behaviour
        var resp = mvc.perform(get("/v1/public/articles/{id}", ARTICLE_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists()).andReturn();
        // Then - verify the output
        var actuelResponse = objectMapper.readValue(resp.getResponse().getContentAsString(), ArticleResponseDTO.class);
        assertThat(actuelResponse).isNotNull();
        assertThat(actuelResponse.getAuthor()).isEqualTo("Arthur Martin");
    }


    @Test
    @Sql(scripts = {"classpath:init_data_institution.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_institution.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenInstitutions_whenGetPublicListInstitutions() throws Exception {
        // Given - precondition or setup
        // When - action or the behaviour
        var resp = mvc.perform(post("/v1/public/listInstitutions")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of("50f4978a-da62-4fde-8f38-5003bd43ff64", "21210632-5d32-42ba-af49-10142142ddf7")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists()).andReturn();
        // Then - verify the output
        var actuelResponse = objectMapper.readValue(resp.getResponse()
                .getContentAsString(), new com.fasterxml.jackson.core.type.TypeReference<List<InstitutionDTO>>() {
        });
        assertThat(actuelResponse).isNotNull();
        if (actuelResponse.get(1).getInstitutionId().equals(UUID.fromString("50f4978a-da62-4fde-8f38-5003bd43ff64"))) {
            assertThat(actuelResponse.get(1).getCode()).isEqualTo("MNHN");
        } else if (actuelResponse.get(1).getInstitutionId().equals(UUID.fromString("21210632-5d32-42ba-af49-10142142ddf7"))) {
            assertThat(actuelResponse.get(1).getCode()).isEqualTo("CJBN");
        }
    }

    @Test
    @Sql(scripts = {"classpath:init_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenCollections_whenGetPublicListCollections() throws Exception {
        // Given - precondition or setup
        // When - action or the behaviour
        var resp = mvc.perform(post("/v1/public/listCollections")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of("9a342a92-6fe8-48d3-984e-d1731c051666", "8342cf1d-f202-4c10-9037-2e2406ce7331")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists()).andReturn();
        // Then - verify the output
        var actuelResponse = objectMapper.readValue(resp.getResponse()
                .getContentAsString(), new com.fasterxml.jackson.core.type.TypeReference<List<CollectionDetailPublicDTO>>() {
        });
        assertThat(actuelResponse).isNotNull();
        if (actuelResponse.get(1).getId().equals(UUID.fromString("9a342a92-6fe8-48d3-984e-d1731c051666"))) {
            assertThat(actuelResponse.get(1).getCollectionNameFr()).isEqualTo("Tunicier");
        } else if (actuelResponse.get(1).getId().equals(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331"))) {
            assertThat(actuelResponse.get(1).getCollectionNameFr()).isEqualTo("botanique");
        }
    }

}
