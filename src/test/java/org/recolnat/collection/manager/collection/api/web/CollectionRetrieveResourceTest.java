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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("int")
@Sql(scripts = {"classpath:clean_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:init_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CollectionRetrieveResourceTest extends AbstractResourceDBTest {

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

}
