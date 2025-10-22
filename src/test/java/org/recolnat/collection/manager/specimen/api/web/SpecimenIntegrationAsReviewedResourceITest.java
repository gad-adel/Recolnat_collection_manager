package org.recolnat.collection.manager.specimen.api.web;

import io.recolnat.model.SpecimenIntegrationRequestDTO;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.UserAttributes;
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
import static org.mockito.Mockito.when;
import static org.recolnat.collection.manager.api.domain.enums.RoleEnum.ADMIN_COLLECTION;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("int")
@Sql(scripts = {"classpath:clean_data_collection.sql", "classpath:init_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_collection.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class SpecimenIntegrationAsReviewedResourceITest extends AbstractResourceElasticTest {

    @Value(value = "classpath:specimenData.json")
    private Resource specimenData;

    @Test
    void givenSpecimen_whenAddSpecimenAsReviewed_thenReturnSpecimenSatutReviewed() throws Exception {
        // Given - precondition or setup
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);

        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .institution(1).role(ADMIN_COLLECTION.name())
                .collections(List.of(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331"), UUID.fromString("9a342a92-6fe8-48d3-984e-d1731c051666")))
                .build());

        mvc.perform(post("/v1/specimens/review")
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
                .andExpect(status().isCreated())
                .andExpect(header().string("specimenId", notNullValue()));
    }
}
