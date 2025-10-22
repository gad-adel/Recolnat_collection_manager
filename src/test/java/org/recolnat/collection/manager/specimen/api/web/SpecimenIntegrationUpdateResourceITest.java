package org.recolnat.collection.manager.specimen.api.web;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import io.recolnat.model.SpecimenIntegrationRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceElasticTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.recolnat.collection.manager.common.exception.ErrorCode.ERR_CODE_CM;
import static org.recolnat.collection.manager.specimen.api.web.SpecimenRetrieveResourceITest.SPECIMEN_ID;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = {"classpath:clean_data_specimen_for_update.sql", "classpath:init_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles("int")
public class SpecimenIntegrationUpdateResourceITest extends AbstractResourceElasticTest {
    public static final String SPEC_ID = "70029074-fde4-4f85-b3fe-9e25e7bfd9ea";
    public static final String COL_ID = "8342cf1d-f202-4c10-9037-2e2406ce7331";
    public static final String NO_LINK_COLLECTION_ID = "9a342a92-6fe8-48d3-984e-d1731c051666";
    public static final String NO_EXIST_COLLECTION_ID = "9a342a92-6fe8-48d3-984e-d1731c051667";
    @Autowired()
    @Qualifier("elasticsearchClient")
    ElasticsearchClient elasticsearchClient;
    @Value(value = "classpath:updateSpecimenData.json")
    private Resource specimenData;

    /**
     * on initialize l index car l'update implique la suppression eventuel du specimen
     * sur elastic (ne fonctionne que si elastic possede l index). le mapping et le setting sont optionnels
     */
    @BeforeEach
    public void initIndex() {
        try {
            BooleanResponse resp = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(b -> b.index("rcn_specimen_short")));

            if (!resp.value()) {
                CreateIndexRequest request;
                try (InputStream inputm = this.getClass().getResourceAsStream("/mapping.json")) {
                    try (InputStream inputset = this.getClass().getResourceAsStream("/setting.json")) {
                        request = CreateIndexRequest
                                .of(b -> b.index("rcn_specimen_short").mappings(m -> m.withJson(inputm))
                                        .settings(s -> s.withJson(inputset)));
                    }
                }
                elasticsearchClient.indices().create(request);
            }
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Transactional
    @Test
    void updateSpecimen_ok() throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(put("/v1/specimens/{specimenId}", SPECIMEN_ID)
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", UID_CONST))
                                .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                        .with(user("test")
                                .roles("ADMIN_INSTITUTION"))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(header().string("collectionId", notNullValue()))
                .andExpect(header().string("specimenId", notNullValue()));
    }

    @Transactional
    @Test
    void updateSpecimen_admin_institution_can_change_collection() throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        body.setCollectionId(UUID.fromString(NO_LINK_COLLECTION_ID));

        mvc.perform(put("/v1/specimens/{specimenId}", SPECIMEN_ID)
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", UID_CONST))
                                .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                        .with(user("test")
                                .roles("ADMIN_INSTITUTION"))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is2xxSuccessful());
    }

    @Transactional
    @Test
    void updateSpecimen_admin_can_change_collection() throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN.name())
                .institution(1).build());

        body.setCollectionId(UUID.fromString(NO_LINK_COLLECTION_ID));

        mvc.perform(put("/v1/specimens/{specimenId}", SPECIMEN_ID)
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", UID_CONST))
                                .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                        .with(user("test")
                                .roles("ADMIN_INSTITUTION"))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is2xxSuccessful());
    }

    @Transactional
    @Test
    void updateSpecimen_admin_collection_can_update_as_published() throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_COLLECTION.name())
                .collections(List.of(UUID.fromString(NO_LINK_COLLECTION_ID)))
                .institution(1).build());

        body.setCollectionId(UUID.fromString(NO_LINK_COLLECTION_ID));

        mvc.perform(put("/v1/specimens/{specimenId}", SPECIMEN_ID)
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", UID_CONST))
                                .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                        .with(user("test")
                                .roles("ADMIN_INSTITUTION"))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is2xxSuccessful());
    }

    @Transactional
    @Test
    void updateSpecimen_invalid_collectionId_ko() throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        body.setCollectionId(UUID.fromString(NO_EXIST_COLLECTION_ID));

        mvc.perform(put("/v1/specimens/{specimenId}", SPECIMEN_ID)
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
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code", hasToString(ERR_CODE_CM)))
                .andExpect(header().string("collectionId", nullValue()))
                .andExpect(header().string("specimenId", nullValue()));
    }

    @Transactional
    @Test
    void updateSpecimen_as_resp_collection_ok() throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui(UUID.randomUUID().toString())
                .role(RoleEnum.fromValue("ADMIN_COLLECTION").name())
                .collections(List.of(UUID.fromString(COL_ID)))
                .institution(1).build());

        mvc.perform(put("/v1/specimens/{specimenId}", SPEC_ID)
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", UID_CONST)))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

    }

    @Transactional
    @Test
    void updateSpecimen_as_data_entry_ko() throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui(UUID.randomUUID().toString())
                .role(RoleEnum.fromValue("DATA_ENTRY").name())
                .collections(List.of(UUID.fromString(COL_ID)))
                .institution(1).build());

        mvc.perform(put("/v1/specimens/{specimenId}", SPEC_ID)
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", UID_CONST)))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(header().string("collectionId", nullValue()))
                .andExpect(header().string("specimenId", nullValue()));

    }

    @Transactional
    @ParameterizedTest()
    @ValueSource(strings = {"DATA_ENTRY", "ADMIN_COLLECTION"})
    void updateSpecimen_as_draft_ko(String role) throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui(UUID.randomUUID().toString())
                .role(RoleEnum.fromValue(role).name())
                .collections(List.of(UUID.fromString(INCORECT_COLLECTION_ID)))
                .institution(1).build());

        mvc.perform(put("/v1/specimens/{specimenId}/draft", SPECIMEN_ID)
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", UID_CONST)))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(header().string("collectionId", nullValue()))
                .andExpect(header().string("specimenId", nullValue()));

    }

    @Transactional
    @ParameterizedTest()
    @ValueSource(strings = {"DATA_ENTRY", "ADMIN_COLLECTION"})
    void updateSpecimen_as_draft_ok(String role) throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui(UUID.randomUUID().toString())
                .role(RoleEnum.fromValue(role).name())
                .collections(List.of(UUID.fromString(COL_ID)))
                .institution(1).build());

        mvc.perform(put("/v1/specimens/{specimenId}/draft", SPEC_ID)
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", UID_CONST)))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(header().string("collectionId", notNullValue()))
                .andExpect(header().string("specimenId", notNullValue()));

    }

    @Transactional
    @ParameterizedTest()
    @ValueSource(strings = {"DATA_ENTRY", "ADMIN_COLLECTION", "ADMIN_INSTITUTION"})
    void givenSpecimen_when_callDeleteSpecimen_then_removeSpecimen(String role) throws Exception {
        // Given - precondition or setup  
        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui(UUID.randomUUID().toString())
                .role(RoleEnum.fromValue(role).name())
                .collections(List.of(UUID.fromString(COL_ID)))
                .institution(1).build());

        mvc.perform(delete("/v1/specimens/{specimenId}", SPEC_ID)
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", UID_CONST)))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                // Then - verify the output
                .andExpect(status().isOk());
    }

    @Transactional
    @Test
    void givenDataWithoutSpecimen_when_callDeleteSpecimen_then_throwsException() throws Exception {
        // Given - precondition or setup 
        String specimenNo = "1ecc4e9a-1230-4931-9918-c586ded155b6";
        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui(UUID.randomUUID().toString())
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .collections(List.of(UUID.fromString(COL_ID)))
                .institution(1).build());

        mvc.perform(delete("/v1/specimens/{specimenId}", specimenNo)
                        .with(opaqueToken()
                                .attributes(att -> att.put("sub", UID_CONST)))
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                // Then - verify the output
                .andExpect(status().is4xxClientError());
    }

}
