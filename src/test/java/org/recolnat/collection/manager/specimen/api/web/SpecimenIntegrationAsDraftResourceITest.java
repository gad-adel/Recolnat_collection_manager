package org.recolnat.collection.manager.specimen.api.web;

import io.recolnat.model.SpecimenIntegrationRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.recolnat.collection.manager.api.domain.enums.RoleEnum.ADMIN_INSTITUTION;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("int")
@Sql(scripts = {"classpath:clean_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:init_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class SpecimenIntegrationAsDraftResourceITest extends AbstractResourceElasticTest {

    private static final String OTHER_COLLECTION_ID = "e82e315f-c4a0-4a3d-942c-19c26151a1b1";
    @Value(value = "classpath:specimenData.json")
    private Resource specimenData;

    @Test
    void addSpecimenAsDraft_as_resp_inst() throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .institution(1).role(ADMIN_INSTITUTION.name()).build());

        mvc.perform(post("/v1/specimens/draft")
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
                .andExpect(header().string("specimenId", notNullValue()));
    }

    @ParameterizedTest()
    @ValueSource(strings = {"DATA_ENTRY", "ADMIN_COLLECTION"})
    void addSpecimenAsDraft_as_resp_col_ok(String role) throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .institution(1).role(RoleEnum.fromValue(role).name())
                .collections(List.of(UUID.fromString(COLLECTION_ID))).build());

        mvc.perform(post("/v1/specimens/draft")
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", UID_CONST)))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("specimenId", notNullValue()));
    }

    @ParameterizedTest()
    @ValueSource(strings = {"DATA_ENTRY", "ADMIN_COLLECTION"})
    void addSpecimenAsDraft_as_resp_col_ko(String role) throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .institution(1).role(RoleEnum.fromValue(role).name())
                .collections(List.of(UUID.fromString(OTHER_COLLECTION_ID))).build());
        mvc.perform(post("/v1/specimens/draft")
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", UID_CONST)))
                        .with(user("test")
                                .roles("DATA_ENTRY"))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(header().string("specimenId", nullValue()));
    }

    @Test
    void addSpecimenAsDraft_as_resp_inst_of_other_inst_fail() throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .institution(2).role(ADMIN_INSTITUTION.name()).build());

        mvc.perform(post("/v1/specimens/draft")
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", UID_CONST)))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(header().string("specimenId", nullValue()));
    }
}
