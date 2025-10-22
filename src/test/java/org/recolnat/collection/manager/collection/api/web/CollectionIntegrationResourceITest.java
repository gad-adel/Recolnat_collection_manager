package org.recolnat.collection.manager.collection.api.web;


import io.recolnat.model.CollectionIntegrationRequestDTO;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("int")
@Sql(scripts = {"classpath:clean_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:init_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CollectionIntegrationResourceITest extends AbstractResourceDBTest {

    @Test
    void addCollection_should_be_ok() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .collections(List.of(UUID.fromString("82e20227-b0d7-46b4-b44d-2257d86f67b1")))
                .institution(1).build());

        mvc.perform(post("/v1/collections")
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", "712215d4-c795-11ec-9d64-0242ac120002"))
                                .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                        .with(user("test").roles("ADMIN_INSTITUTION"))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CollectionIntegrationRequestDTO().domain("typeCollection")
                                        .institutionId(UUID.fromString("50f4978a-da62-4fde-8f38-5003bd43ff64"))
                                        .collectionNameFr("collectionNameFr"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("collectionId", notNullValue()));
    }

    @Test
    void addCollection_bad_inst_code_should_fail() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .collections(List.of(UUID.fromString("82e20227-b0d7-46b4-b44d-2257d86f67b1")))
                .institution(1).build());

        mvc.perform(post("/v1/collections")
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", "712215d4-c795-11ec-9d64-0242ac120002"))
                                .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CollectionIntegrationRequestDTO().domain("typeCollection")
                                        .institutionId(UUID.fromString("50f4978a-da62-4fde-8f38-5003bd43ff64"))
                                        .collectionNameEn("collectionNameEn"))))
                .andExpect(status().is4xxClientError())
                .andExpect(header().string("collectionId", nullValue()));

    }

    @Test
    @Sql(scripts = {"classpath:init_collection.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenCollection_whenSameCodeExiste_thenReturnException() throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .collections(List.of(UUID.fromString("82e20227-b0d7-46b4-b44d-2257d86f67b1")))
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        String bodyNewCollection = """
                	{
                "typeCollection": "botanique",
                "collectionNameFr": "CollTest1",
                "collectionNameEn": "CollTest1",
                "descriptionFr": "Description de la collection 1",
                "descriptionEn": "Description de la collection 1",
                "institutionId": 1
                	}""";
        // When - action or the behaviour
        mvc.perform(post("/v1/collections")
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", "712215d4-c795-11ec-9d64-0242ac120002"))
                                .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyNewCollection))
                // Then - verify the output
                .andExpect(status().is4xxClientError())
                .andExpect(header().string("collectionId", nullValue()));
    }

    @Test
    void addCollection2_should_be_ok() throws Exception {
        // Given - precondition or setup
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .collections(List.of(UUID.fromString("82e20227-b0d7-46b4-b44d-2257d86f67b1")))
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        String bodyNewCollection = """
                	{
                "domain": "RESINS",
                "collectionNameFr": "COLLECTION OF AMBER AND OTHER FOSSIL RESINS",
                "collectionNameEn": "COLLECTION OF AMBER AND OTHER FOSSIL RESINS",
                "descriptionFr": "Description Fr",
                "descriptionEn": "Description En",
                "institutionId": "50f4978a-da62-4fde-8f38-5003bd43ff64"
                	}""";
        // When - action or the behaviour

        mvc.perform(post("/v1/collections")
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", "712215d4-c795-11ec-9d64-0242ac120002"))
                                .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                        .with(user("test").roles("ADMIN_INSTITUTION"))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyNewCollection))
                .andExpect(status().isCreated())
                .andExpect(header().string("collectionId", notNullValue()));

    }


    @Test
    void deleteCollection_data_entry_should_throw() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .collections(List.of(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331")))
                .role(RoleEnum.DATA_ENTRY.name())
                .institution(1).build());

        mvc.perform(delete("/v1/collections/{id}", "8342cf1d-f202-4c10-9037-2e2406ce7331")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
                .andReturn();
    }

    @Test
    void deleteCollection_admin_collection_should_throw() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .collections(List.of(UUID.fromString("82e20227-b0d7-46b4-b44d-2257d86f67b1")))
                .role(RoleEnum.ADMIN_COLLECTION.name())
                .institution(1).build());

        mvc.perform(delete("/v1/collections/{id}", "8342cf1d-f202-4c10-9037-2e2406ce7331")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
                .andReturn();
    }

    @Test
    void deleteCollection_admin_institution_on_other_institution_should_throw() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(2).build());

        mvc.perform(delete("/v1/collections/{id}", "9a342a92-6fe8-48d3-984e-d1731c051666")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
                .andReturn();
    }

    @Test
    void deleteCollection_admin_institution_on_same_institution_should_be_ok() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(delete("/v1/collections/{id}", "9a342a92-6fe8-48d3-984e-d1731c051666")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void deleteCollection_admin_should_be_ok() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .role(RoleEnum.ADMIN.name()).build());

        mvc.perform(delete("/v1/collections/{id}", "9a342a92-6fe8-48d3-984e-d1731c051666")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void deleteCollection_admin_on_collection_with_specimen_should_throw() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .role(RoleEnum.ADMIN.name()).build());

        mvc.perform(delete("/v1/collections/{id}", "8342cf1d-f202-4c10-9037-2e2406ce7331")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()))
                .andReturn();
    }
}
