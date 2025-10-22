package org.recolnat.collection.manager.specimen.api.web;


import io.recolnat.model.SpecimenIntegrationRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceElasticTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.recolnat.collection.manager.service.impl.SpecimenIntegrationServiceImpl.ERR_CODE_INVALID_REQUEST;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("int")
@Sql(scripts = {"classpath:clean_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:init_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class SpecimenIntegrationResourceITest extends AbstractResourceElasticTest {
    public static final String COLLECTION_ID = "9a342a92-6fe8-48d3-984e-d1731c051666";
    public static final String COLLECTION_ID_2 = "8342cf1d-f202-4c10-9037-2e2406ce7331";
    public static final String INCORECT_COLLECTION_ID = "712215d4-c795-11ec-9d64-0242ac120002";
    public static final String UID_CONST = "712215d4-c795-11ec-9d64-0242ac120003";
    public static final String SPECIMEN_ID = "9c6ab9ea-d049-47b5-972c-18e7831bdd4e";
    @Value(value = "classpath:specimenData.json")
    private Resource specimenData;
    @Value(value = "classpath:invalidSpecimenData.json")
    private Resource invalidSpecimenData;
    @Value(value = "classpath:invalidSpecimenData2.json")
    private Resource invalidSpecimenData2;
    @Value(value = "classpath:specimenBatch.json")
    private Resource specimenBatch;

    @Test
    void addSpecimen() throws Exception {

        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(
                        post("/v1/specimens")
                                .with(opaqueToken()
                                        .attributes(att -> att.put("sub", UID_CONST))
                                        .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                                .with(user("test")
                                        .roles("ADMIN_INSTITUTION"))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))

                .andExpect(status().isCreated())
                .andExpect(header().string("collectionId", notNullValue()))
                .andExpect(header().string("specimenId", notNullValue()));

    }

    @Test
    @DisplayName("given Specimen when Add with Two Current Determination Not Null then Throws Exception")
    void givenSpecimen_whenAddwithTwoCurrentDeterminationNotNull_thenThrowsException() throws Exception {
        // Given - precondition or setup
        var body = objectMapper.readValue(invalidSpecimenData2.getInputStream(), SpecimenIntegrationRequestDTO.class);
        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .collections(List.of(UUID.fromString("82e20227-b0d7-46b4-b44d-2257d86f67b1")))
                .institution(1).build());

        mvc.perform(
                        post("/v1/specimens")
                                .with(opaqueToken()
                                        .attributes(att -> att.put("sub", INCORECT_COLLECTION_ID))
                                        .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                                .with(user("test")
                                        .roles("ADMIN_INSTITUTION"))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))

                // Then - verify the output
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code", hasToString(ERR_CODE_INVALID_REQUEST)))
                .andExpect(header().string("collectionId", nullValue()))
                .andExpect(header().string("specimenId", nullValue()));
    }

    @Test
    void addSpecimen_should_fail_incorect_id() throws Exception {

        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .collections(List.of(UUID.fromString("82e20227-b0d7-46b4-b44d-2257d86f67b1")))
                .institution(1).build());

        mvc.perform(
                        post("/v1/specimens")
                                .with(opaqueToken()
                                        .attributes(att -> att.put("sub", UID_CONST))
                                        .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                                .with(user("test")
                                        .roles("ADMIN_INSTITUTION"))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))

                .andExpect(status().is4xxClientError())
                .andExpect(header().string("collectionId", nullValue()))
                .andExpect(header().string("specimenId", nullValue()));

    }

    @Test
    void addSpecimen_should_fail_for_invalid_input() throws Exception {
        var body = objectMapper.readValue(invalidSpecimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .collections(List.of(UUID.fromString("82e20227-b0d7-46b4-b44d-2257d86f67b1")))
                .institution(1).build());

        mvc.perform(post("/v1/specimens")
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", INCORECT_COLLECTION_ID))
                                .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                        .with(user("test")
                                .roles("ADMIN_INSTITUTION"))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))

                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code", hasToString(ERR_CODE_INVALID_REQUEST)))
                .andExpect(header().string("collectionId", nullValue()))
                .andExpect(header().string("specimenId", nullValue()));

    }

    @ParameterizedTest
    @EnumSource(value = RoleEnum.class, names = {"ADMIN_COLLECTION", "DATA_ENTRY"})
    @DisplayName("Integration test duplicate specimen with status valid and role not admin institution")
    void givenSpecimenStatusValid_whenDuplicateWithRoleAdminCollection_thenThrowsException(RoleEnum role) throws Exception {
        // Given - precondition or setup
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(role.name())
                .collections(List.of(UUID.fromString(COLLECTION_ID), UUID.fromString(COLLECTION_ID_2)))
                .institution(1).build());
        // When - action or the behaviour
        mvc.perform(
                        post("/v1/specimens/{specimenId}/duplicate/{collectionTargetId}?operation=VALIDATE",
                                SPECIMEN_ID, COLLECTION_ID)
                                .with(opaqueToken()
                                        .attributes(att -> att.put("sub", UID_CONST))
                                        .authorities(new SimpleGrantedAuthority("ADMIN_COLLECTION")))
                                .with(user("test")
                                        .roles("ADMIN_COLLECTION"))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                // Then - verify the output
                .andExpect(status().is4xxClientError());    //403 Forbidden
    }

}
