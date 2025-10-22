package org.recolnat.collection.manager.specimen.api.web;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import io.recolnat.model.SpecimenIntegrationRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceElasticTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.recolnat.collection.manager.api.domain.enums.RoleEnum.ADMIN_COLLECTION;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("int")
@Sql(scripts = {"classpath:clean_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:init_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class SpecimenUpdateAsReviewedResourceITest extends AbstractResourceElasticTest {

    public static final String SPECIMEN_ID = "70029074-fde4-4f85-b3fe-9e25e7bfd9ea";

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
                InputStream inputm = this.getClass().getResourceAsStream("/mapping.json");
                InputStream inputset = this.getClass().getResourceAsStream("/setting.json");
                CreateIndexRequest request = CreateIndexRequest
                        .of(b -> b.index("rcn_specimen_short").mappings(m -> m.withJson(inputm))
                                .settings(s -> s.withJson(inputset)));
                elasticsearchClient.indices().create(request);
            }
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    @DisplayName("Integration testing when update specimen with statut reviewed")
    void givenSpecimen_whenUpdateSpecimenAsReviewed_thenReturnSpecimenSatutReviewed() throws Exception {
        // Given - precondition or setup
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        // When - action or the behaviour

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .institution(1).role(ADMIN_COLLECTION.name())
                .collections(List.of(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331"), UUID.fromString("9a342a92-6fe8-48d3-984e-d1731c051666")))
                .build());

        mvc.perform(
                        put("/v1/specimens/{specimenId}/review", SPECIMEN_ID)
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
                .andExpect(status().isOk())
                .andExpect(header().string("collectionId", notNullValue()))
                .andExpect(header().string("specimenId", notNullValue()));
    }
}
