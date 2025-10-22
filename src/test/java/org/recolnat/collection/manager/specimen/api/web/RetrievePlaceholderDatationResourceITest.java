package org.recolnat.collection.manager.specimen.api.web;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceElasticTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("int")
public class RetrievePlaceholderDatationResourceITest extends AbstractResourceElasticTest {

    @Value(value = "classpath:json/placeholder-datation.json")
    private Resource jsonData;

    @Test
    void getAllPlaceholderDatation() throws Exception {
        var expectVal = objectMapper.readValue(jsonData.getInputStream(), new TypeReference<List<String>>() {
        });

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui(UID_CONST)
                .role(RoleEnum.ADMIN_INSTITUTION.name())
                .institution(1).build());

        mvc.perform(get("/v1/placeholderDatations")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").value(expectVal))
                .andReturn();
    }
}
