package org.recolnat.collection.manager.specimen.api.web;

import io.recolnat.model.AdminSpecimenDTO;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceElasticTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("int")
@Sql(scripts = {"classpath:clean_data_specimen_for_update.sql", "classpath:init_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class SpecimenRetrieveResourceITest extends AbstractResourceElasticTest {

    static final UUID SPECIMEN_ID = UUID.fromString("70029074-fde4-4f85-b3fe-9e25e7bfd9ea");
    static final String CATALOG_NUMBER_KO = "12354";
    static final String CATALOG_NUMBER_OK = "12345";
    private static final UUID BAD_SPECIMEN_ID = UUID.fromString("70029074-fde4-4f85-b3fe-9e25e7bfd9eb");
    private static final UUID COLLECTION_ID = UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331");
    @Value(value = "classpath:retrieveSpecimenData.json")
    private Resource expectedResponseJson;

    @Test
    void getSpecimenById_ok() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens/{specimenId}", SPECIMEN_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andReturn();

        var actualResponseDTO = objectMapper.readValue(resp.getResponse().getContentAsString(), AdminSpecimenDTO.class);
        var expResponseDTO = objectMapper.readValue(expectedResponseJson.getInputStream(), AdminSpecimenDTO.class);

        assertThat(actualResponseDTO).isEqualTo(expResponseDTO);
    }

    @Test
    void getSpecimenById_invalid_specId_ko() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens/{specimenId}", BAD_SPECIMEN_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.code", hasToString("ERR_CODE_CM")))
                .andReturn();

    }

    @Test
    void getNotAlreadyExistsCatalogNumber() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens/exists")
                        .param("catalog_number", CATALOG_NUMBER_KO)
                        .param("collection_id", COLLECTION_ID.toString())
                        .param("specimen_id", SPECIMEN_ID.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().string("false"));
    }

    @Test
    void getAlreadyExistsCatalogNumber() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens/exists")
                        .param("catalog_number", CATALOG_NUMBER_OK)
                        .param("collection_id", COLLECTION_ID.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"));
    }

    @Test
    void getAlreadyCatalogNumberAndSpecimenIdIsNull() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens/exists")
                        .param("catalog_number", CATALOG_NUMBER_OK)
                        .param("collection_id", COLLECTION_ID.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().string("true"));

    }

    @Test
    void getIsAlreadyCatalogNumberAndSpecimenIdIsNull() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens/exists")
                        .param("catalog_number", CATALOG_NUMBER_KO)
                        .param("collection_id", COLLECTION_ID.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().string("false"));
    }

    @Test
    void getErrorCaseWithoutCatalogNumber() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(MockMvcRequestBuilders
                        .get("/v1/specimens/exists")
                        .param("collection_id", COLLECTION_ID.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", containsString("Required parameter 'catalog_number' is not present")));
    }
}
