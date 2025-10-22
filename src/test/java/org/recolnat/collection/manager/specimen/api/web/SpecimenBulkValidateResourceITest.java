package org.recolnat.collection.manager.specimen.api.web;

import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceElasticTest;
import org.recolnat.collection.manager.service.CollectionIdentifier;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("int")
@Sql(scripts = {"classpath:clean_data_specimen_for_update.sql", "classpath:init_data_all_specimen.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class SpecimenBulkValidateResourceITest extends AbstractResourceElasticTest {

    @Test
    void bulkValidate_should_be_ok() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .institution(1).role(RoleEnum.ADMIN_INSTITUTION.name()).build());

        var body = List.of(CollectionIdentifier.builder()
                .collectionId(UUID.fromString("9a342a92-6fe8-48d3-984e-d1731c051666"))
                .specimenId(UUID.fromString("9fdca0c7-2712-46a6-aff5-f88fe6999c1e")).build());
        mvc.perform(
                        patch("/v1/collections/specimens/bulk-validate")
                                .with(opaqueToken()
                                        .attributes(att -> att.put("sub", UID_CONST))
                                        .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                                .with(user("test")
                                        .roles("ADMIN_INSTITUTION"))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andDo((print()))
                .andExpect(status().isOk())
                .andExpect(header().string("CollectionIdentifiers", notNullValue()));
    }

    @Test
    void bulkValidate_should_be_ko_unfound_specimen() throws Exception {
        String unfoundSpecID = "45a560c3-50ae-445d-9bb1-4796d28002f0";

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .institution(1).role(RoleEnum.ADMIN_INSTITUTION.name()).build());

        var body = List.of(CollectionIdentifier.builder()
                .collectionId(UUID.fromString("9a342a92-6fe8-48d3-984e-d1731c051666"))
                .specimenId(UUID.fromString(unfoundSpecID)).build());
        mvc.perform(
                        patch("/v1/collections/specimens/bulk-validate")
                                .with(opaqueToken()
                                        .attributes(att -> att.put("sub", UID_CONST))
                                        .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                                .with(user("test")
                                        .roles("ADMIN_INSTITUTION"))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andDo((print()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void bulkValidate_should_be_ko_specimen_bad_state() throws Exception {

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .institution(1).role(RoleEnum.ADMIN_INSTITUTION.name()).build());

        var body = List.of(CollectionIdentifier.builder()
                        .collectionId(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331"))
                        .specimenId(UUID.fromString("bb4b6db8-4fee-40eb-9a0c-3fb57fdbf940")).build(),
                CollectionIdentifier.builder()
                        .collectionId(UUID.fromString("9a342a92-6fe8-48d3-984e-d1731c051666"))
                        .specimenId(UUID.fromString("9fdca0c7-2712-46a6-aff5-f88fe6999c1e")).build());

        mvc.perform(
                        patch("/v1/collections/specimens/bulk-validate")
                                .with(opaqueToken()
                                        .attributes(att -> att.put("sub", UID_CONST))
                                        .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                                .with(user("test")
                                        .roles("ADMIN_INSTITUTION"))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andDo((print()))
                .andExpect(status().is4xxClientError()).andReturn();
    }
}
