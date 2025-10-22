package org.recolnat.collection.manager.article.api.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.recolnat.model.InstitutionDashboardResponseDTO;
import io.recolnat.model.InstitutionDetailResponseDTO;
import io.recolnat.model.InstitutionRequestDTO;
import io.recolnat.model.PartnerResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.recolnat.collection.manager.api.domain.ConnectedUser;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceDBTest;
import org.recolnat.collection.manager.connector.api.domain.MediaDetailsOutput;
import org.recolnat.collection.manager.connector.api.domain.MediathequeOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.recolnat.collection.manager.api.domain.enums.RoleEnum.ADMIN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/***
 * test dans base postgresprofile= int (integration)
 */
@ActiveProfiles("int")
@Slf4j
@Sql(scripts = {"classpath:clean_institution.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:init_ref_institution.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_institution.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class InstitutionResourceITest extends AbstractResourceDBTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Value(value = "classpath:images/mnhnLogo.jpg")
    private Resource logoImg;

    public static ConnectedUser getConnectedUser() {
        return ConnectedUser.builder()
                .userId(UUID.fromString("a416ce96-bc14-48bc-850e-80aa95ca7221"))
                .userName("admin").build();
    }

    @Test
    void getInstitutions_as_admin() throws Exception {

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder().ui("b9a746ef-3dd6-4954-8e14-a317e380524e")
                .role(ADMIN.name())
                .build());

        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/institutions?page=0&size=100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.data", hasSize(87))).andReturn();

        var allInst = getInstitutionResponseDTO(resp);
        assertThat(allInst.getData()).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN_INSTITUTION", "ADMIN_COLLECTION", "DATA_ENTRY", "USER_INFRA"})
    void getInstitutions_as_other_roles(String role) throws Exception {

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder().ui("b9a746ef-3dd6-4954-8e14-a317e380524e")
                .role(RoleEnum.fromValue(role).name())
                .build());

        var resp = mvc.perform(MockMvcRequestBuilders
                        .get("/v1/institutions?page=0&size=100")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.data", hasSize(0))).andReturn();

        InstitutionDashboardResponseDTO allInst = getInstitutionResponseDTO(resp);

        assertThat(allInst.getData()).isEmpty();

    }

    private InstitutionDashboardResponseDTO getInstitutionResponseDTO(MvcResult resp) throws JsonProcessingException, UnsupportedEncodingException {
        return objectMapper.readValue(resp.getResponse().getContentAsString(), InstitutionDashboardResponseDTO.class);
    }

    @Transactional
    @Test
    @DisplayName("Integration test add new Institution")
    void given_whenAddInstitutions_thenReturnnewInstitutionId() throws Exception {
        // Given - precondition or setup
        ConnectedUser connectedUser = getConnectedUser();

        var jsonInstitution = """ 
                {
                	"code": "code_1",
                	"name": "institution_name",
                	"mandatoryDescription": "Mandatory Description",
                	"optionalDescription": "optional Description",
                	"partnerType": "DATA_PROVIDER",
                	"institutionId": "a1273029-1fba-41ec-884a-e06c1b5524a2"
                }""";
        InstitutionRequestDTO body = objectMapper.readValue(jsonInstitution, InstitutionRequestDTO.class);
        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder().ui(connectedUser.getUserId().toString())
                .role(RoleEnum.ADMIN.name())
                .build());

        when(authenticationService.getConnected()).thenReturn(connectedUser);
        mvc.perform(MockMvcRequestBuilders
                        .post("/v1/institutions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .accept(MediaType.APPLICATION_JSON))
                // Then - verify the output
                .andExpect(status().isCreated())
                .andExpect(header().string("institutionId", notNullValue()))
                .andReturn();
    }

    @Transactional
    @Test
    @DisplayName("Update Institution")
    void givenInstitutionId_whenUpdateInstitution_thenReturnInstituionUpdate() throws Exception {
        // Given - precondition or setup
        var jsonInstitution = """ 
                {
                	"code": "CJBN",
                	"name": "Conservatoire et jardins botaniques de Nancy-update",
                	"mandatoryDescription": "Mandatory Description",
                              "optionalDescription": "optional Description",
                              "partnerType": "MEMBER"
                }""";
        InstitutionRequestDTO body = objectMapper.readValue(jsonInstitution, InstitutionRequestDTO.class);
        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder().ui("b9a746ef-3dd6-4954-8e14-a317e380524e")
                .role(RoleEnum.ADMIN.name())
                .build());

        when(authenticationService.getConnected()).thenReturn(getConnectedUser());

        mvc.perform(MockMvcRequestBuilders
                        .put("/v1/institutions/{institutionId}", "21210632-5d32-42ba-af49-10142142ddf7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .accept(MediaType.APPLICATION_JSON))
                // Then - verify the output
                .andExpect(status().isOk())
                .andExpect(header().string("institutionId", notNullValue()))
                .andReturn();
    }

    @Transactional
    @Test
    @DisplayName("Add Institution")
    void givenInstitutionId_whenAddInstitution() throws Exception {
        // Given - precondition or setup

        var jsonInstitution = """ 
                {
                	"code": "CJBNX",
                	"name": "Conservatoire et jardins botaniques de Nancy-Add",
                	"mandatoryDescription": "Mandatory Description ADD",
                              "optionalDescription": "optional Description ADD",
                              "partnerType": "MEMBER"
                }""";
        InstitutionRequestDTO body = objectMapper.readValue(jsonInstitution, InstitutionRequestDTO.class);
        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder().ui("b9a746ef-3dd6-4954-8e14-a317e380524e")
                .role(RoleEnum.ADMIN.name())
                .build());

        when(authenticationService.getConnected()).thenReturn(getConnectedUser());

        mvc.perform(MockMvcRequestBuilders
                        .post("/v1/institutions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .accept(MediaType.APPLICATION_JSON))
                // Then - verify the output
                .andExpect(status().is(201))
                .andExpect(header().string("institutionId", notNullValue()))
                .andReturn();
    }

    @Test
    void given_whenRetrieveInstitutionPartner_thenReturnAllPartner() throws Exception {
        // Given - precondition or setup

        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder().ui("b9a746ef-3dd6-4954-8e14-a317e380524e")
                .role(RoleEnum.ADMIN.name())
                .build());

        MvcResult resp = mvc.perform(get("/v1/institutions/partners")
                        .accept(MediaType.APPLICATION_JSON))
                // Then - verify the output
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andReturn();
        var actuelResponse = objectMapper.readValue(resp.getResponse().getContentAsString(), PartnerResponseDTO.class);
        assertThat(actuelResponse.getPartner()).hasSize(3);
    }

    @Test
    void givenInstitution_whenAddLogo_thenReturnInstitWithLogo() throws Exception {
        // Given - precondition or setup
        var mediaResponse = new ResponseEntity<>(MediathequeOutput.builder().media(MediaDetailsOutput.builder().url("https://mediatheque.mnhn.fr/uuid").build())
                .build(), OK);

        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder().ui("b9a746ef-3dd6-4954-8e14-a317e380524e")
                .role(RoleEnum.ADMIN.name())
                .build());

        when(authenticationService.getConnected()).thenReturn(getConnectedUser());
        when(mediathequeApiClient.savePicture(any(MultipartFile.class))).thenReturn(mediaResponse);
        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("logo", logoImg.getFilename(), "multipart/form-data",
                        IOUtils.toByteArray(logoImg.getInputStream()));
        var builder = multipart("/v1/institutions/{institutionId}/logo", UUID.fromString("50f4978a-da62-4fde-8f38-5003bd43ff64"));
        builder.with(request -> {
            request.setMethod("PATCH");
            return request;
        });
        mvc.perform(builder.file(mockMultipartFile)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                // Then - verify the output
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void givenInstitution_whenGetInstitutionDetails_thenReturnInstitution() throws Exception {
        // Given - precondition or setup
        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder().ui("b9a746ef-3dd6-4954-8e14-a317e380524e")
                .role(RoleEnum.ADMIN.name())
                .build());

        MvcResult resp = mvc.perform(get("/v1/institutions/{institutionId}", UUID.fromString("50f4978a-da62-4fde-8f38-5003bd43ff64"))
                        .accept(MediaType.APPLICATION_JSON))
                // Then - verify the output
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andReturn();

        var actuelResponse = objectMapper.readValue(resp.getResponse().getContentAsString(), InstitutionDetailResponseDTO.class);
        assertThat(actuelResponse.getCode()).isEqualTo("MNHN");

    }
}
