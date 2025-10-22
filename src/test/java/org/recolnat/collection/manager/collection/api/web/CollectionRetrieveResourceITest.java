package org.recolnat.collection.manager.collection.api.web;


import io.recolnat.model.CollectionResponseDTO;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("int")
@Sql(scripts = {"classpath:clean_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:init_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CollectionRetrieveResourceITest extends AbstractResourceDBTest {

    @Test
    void getCollections() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(get("/v1/institutions/{institutionId}/collections?page=0&size=100", UUID.fromString("50f4978a-da62-4fde-8f38-5003bd43ff64"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andReturn();
    }

    @Test
    void getCollections_no_collection_ok() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(get("/v1/institutions/{institutionId}/collections?page=0&size=100", new UUID(0, 1))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.data", hasSize(0)))
                .andReturn();
    }

    @Test
    void getNominativeCollections() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(get("/v1/institutions/{institutionId}/nominative-collections?page=0&size=100", UUID.fromString("50f4978a-da62-4fde-8f38-5003bd43ff64"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andReturn();
    }


    @Test
    void givenAllCollections_whenFetchAll_thenReturnAll() throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());
        // When - action or the behaviour
        var resp = mvc.perform(get("/v1/collections")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andReturn();
        // Then - verify the output
        var actuelResponse = objectMapper.readValue(resp.getResponse().getContentAsString(), CollectionResponseDTO[].class);
        System.out.println(actuelResponse.length);
        assertThat(actuelResponse).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void givenAllCollectionWithDescriptionByInstitutionIdWhenLngIsFr() throws Exception {
        String institutionId = "50f4978a-da62-4fde-8f38-5003bd43ff64";
        String language = "fr";

        mvc.perform(get("/v1/public/institutions/{institutionId}/collection-descriptions", institutionId)
                        .header("lng", language)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("botanique")))
                .andExpect(jsonPath("$[0].description", is("Herbier de la Société des Lettres de l'Aveyron")))
                .andExpect(jsonPath("$[1].name", is("Tunicier")))
                .andExpect(jsonPath("$[1].description", is("Collection de Tuniciers (IT) du Muséum national d'Histoire naturelle (MNHN - Paris)")))
                .andReturn();
    }

    @Test
    void givenAllCollectionWithDescriptionByInstitutionIdWhenLngIsEn() throws Exception {
        String institutionId = "50f4978a-da62-4fde-8f38-5003bd43ff64";
        String language = "en";

        mvc.perform(get("/v1/public/institutions/{institutionId}/collection-descriptions", institutionId)
                        .header("lng", language)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("botanique")))
                .andExpect(jsonPath("$[0].description", is("")))
                .andExpect(jsonPath("$[1].name", is("Tunicates")))
                .andExpect(jsonPath("$[1].description", is("Tunicates collection (IT) of the Muséum national d'Histoire naturelle (MNHN - Paris)")))
                .andReturn();
    }

    @Test
    void getDomainStatisticsByInstitutionId() throws Exception {
        String institutionUuid = "50f4978a-da62-4fde-8f38-5003bd43ff64";

        mvc.perform(get("/v1/public/institutions/{institutionId}/domain-statistics", institutionUuid)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].domainName", is("h")))
                .andExpect(jsonPath("$[0].specimenCount", is(2)))
                .andReturn();
    }

}
