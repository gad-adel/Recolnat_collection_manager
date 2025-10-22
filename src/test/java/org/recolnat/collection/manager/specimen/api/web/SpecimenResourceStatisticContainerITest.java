package org.recolnat.collection.manager.specimen.api.web;

import io.recolnat.model.GetPublicInstitutionProgramsRequestDTO;
import io.recolnat.model.StatisticsResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.Institution;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceDBTest;
import org.recolnat.collection.manager.service.InstitutionService;
import org.recolnat.collection.manager.service.SpecimenStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ActiveProfiles("int")
@Sql(scripts = {"classpath:clean_data_all_specimen.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:init_data_all_specimen.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:init_data_institution.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_all_specimen.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class SpecimenResourceStatisticContainerITest extends AbstractResourceDBTest {

    private final CountDownLatch waiter = new CountDownLatch(1);
    @Autowired
    SpecimenStatisticsService specimenStatisticsService;
    @Autowired
    InstitutionService institutionService;
    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Statistic Home Page")
    void givenIndex_whenCountAllDoc_thenReturnValue() throws Exception {
        // Given - precondition or setup

        specimenStatisticsService.clearHomePageStatisticsTTL();
        specimenStatisticsService.getHomePageStatistics();
        // When - action or the behaviour
        waiter.await(5 * 1000, TimeUnit.MILLISECONDS);

        MvcResult andReturn = mvc.perform(get("/v1/public/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andReturn();

        StatisticsResultDTO readValue = objectMapper.readValue(andReturn.getResponse().getContentAsString(), StatisticsResultDTO.class);
        // Then - verify the output
        log.info("Statistic, countSpecimen: {}, countTaxon: {}, countInstitution: {}",
                readValue.getCountSpecimen(), readValue.getCountTaxon(), readValue.getCountInstitution());
        assertThat(readValue.getCountSpecimen()).isGreaterThanOrEqualTo(3);
        assertThat(readValue.getCountTaxon()).isGreaterThanOrEqualTo(2);
        assertThat(readValue.getCountInstitution()).isGreaterThan(2);
    }


    @Test
    @DisplayName("program Home Page")
    void get_institutions_by_codes_list() throws Exception {
        // Given - precondition or setup

        List<Institution> institutions = institutionService.getInstitutionsByCodes(List.of("MNHN", "CJBN"));
        assertThat(institutions.size()).isEqualTo(2);

        GetPublicInstitutionProgramsRequestDTO dtoRequest = new GetPublicInstitutionProgramsRequestDTO();
        dtoRequest.codeinstitutions(List.of("MNHN", "CJBN"));

        mvc.perform(
                        post("/v1/public/programs")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dtoRequest)))
                // Then - verify the output
                .andDo(print())
                .andExpect(status().isOk())
                //pb encoding accent
                // .andExpect(content().json("[{\"id\":1,\"institutionId\":\"50f4978a-da62-4fde-8f38-5003bd43ff64\",\"code\":\"MNHN\",\"name\":\"Muséum National d'Histoire Naturelle\",\"logoUrl\":null},{\"id\":2,\"institutionId\":\"21210632-5d32-42ba-af49-10142142ddf7\",\"code\":\"CJBN\",\"name\":\"Conservatoire et jardins botaniques de Nancy\",\"logoUrl\":null}]"));
                .andExpect(jsonPath("$[0].institutionId").value("50f4978a-da62-4fde-8f38-5003bd43ff64"))
                .andExpect(jsonPath("$[0].code").value("MNHN"))
                .andExpect(jsonPath("$[1].institutionId").value("21210632-5d32-42ba-af49-10142142ddf7"))
                .andExpect(jsonPath("$[1].code").value("CJBN"))
                .andExpect(jsonPath("$[1].name").value("Conservatoire et jardins botaniques de Nancy"));
    }
}
