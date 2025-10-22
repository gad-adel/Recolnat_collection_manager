package org.recolnat.collection.manager.specimen.api.web;

import io.recolnat.model.DatationResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceElasticTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("int")
public class DatationResourceITest extends AbstractResourceElasticTest {

    public static final String EONOTHEM_PARAM = "eonothem";
    public static final String ERATHEME_PARAM = "eratheme";
    public static final String SYSTEM_PARAM = "system";
    public static final String EPOCH_PARAM = "epoch";
    public static final String AGE_PARAM = "age";

    @Value(value = "classpath:datation.json")
    private Resource datationResp;
    @Value(value = "classpath:allParamsdatationResp.json")
    private Resource allParamsdatationResp;


    @Test
    void getDatation_ok() throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());
        var expectVal = objectMapper.readValue(datationResp.getInputStream(), DatationResponseDTO.class);

        mvc.perform(get("/v1/datations")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").value(expectVal))
                .andReturn();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Protéozoïque",
            "Hadéen",
            "Phanérozoïque",
            "Archéen"})
    void getDatation_with_param_eonothem_ok(String eonothem) throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        objectMapper.readValue(datationResp.getInputStream(), DatationResponseDTO.class);

        mvc.perform(get("/v1/datations")
                        .param(EONOTHEM_PARAM, eonothem)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.eonothem").exists())
                .andExpect(jsonPath("$.eonothem", hasItem(eonothem)))
                .andExpect(jsonPath("$.eratheme").exists())
                .andReturn();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Paléozoïque",
            "Paléoprotérozoïque",
            "Paléoarchéen",
            "Mésoarchéen",
            "Néoprotérozoïque",
            "Mésozoïque",
            "Eoarchéen",
            "Cénozoïque",
            "Néoarchéen",
            "Mésoprotérozoïque"})
    void getDatation_with_param_system_ok(String eratheme) throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(get("/v1/datations")
                        .param(ERATHEME_PARAM, eratheme)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.eonothem").exists())
                .andExpect(jsonPath("$.eratheme", hasItem(eratheme)))
                .andExpect(jsonPath("$.eratheme").exists())
                .andReturn();
    }


    @Test
    void getDatation_with_four_params_ok() throws Exception {
        var expectVal = objectMapper.readValue(allParamsdatationResp.getInputStream(), DatationResponseDTO.class);

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(get("/v1/datations")
                        .param(EONOTHEM_PARAM, "Phanérozoïque")
                        .param(SYSTEM_PARAM, "Cambrien")
                        .param(ERATHEME_PARAM, "Paléozoïque")
                        .param(EPOCH_PARAM, "Terreneuvien")
                        .param(AGE_PARAM, "Wuliuen")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").value(expectVal))
                .andReturn();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Néogène",
            "Stathérien",
            "Paléogène",
            "Dévonien",
            "Rhyacien",
            "Sidérien",
            "Permien",
            "Trias",
            "Édiacarien",
            "Carbonifère",
            "Ordovicien",
            "Calymmien",
            "Orosirien",
            "Sténien",
            "Ectasien",
            "Jurassique",
            "Cryogénien",
            "Cambrien",
            "Quaternaire",
            "Tonien",
            "Crétacé",
            "Silurien"})
    void getDatation_with_param_eratheme_ok(String system) throws Exception {
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b2")
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(get("/v1/datations")
                        .param(SYSTEM_PARAM, system)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.eonothem").exists())
                .andExpect(jsonPath("$.system", hasItem(system)))
                .andExpect(jsonPath("$.system", hasSize(1)))
                .andExpect(jsonPath("$.eratheme").exists())
                .andReturn();
    }
}
