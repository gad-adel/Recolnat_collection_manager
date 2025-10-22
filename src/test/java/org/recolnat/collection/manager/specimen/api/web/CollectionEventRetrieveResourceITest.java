package org.recolnat.collection.manager.specimen.api.web;

import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceElasticTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("int")
@Sql(scripts = {"classpath:clean_data_specimen_for_autocompletion.sql", "classpath:init_data_specimen_for_autocompletion.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_specimen_for_autocompletion.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CollectionEventRetrieveResourceITest extends AbstractResourceElasticTest {

    @Test
    void getCountries_whithPrefixC() throws Exception {
        String query = "C";
        int size = 3;
        mvc.perform(get("/v1/collection-events/countries/autocomplete")
                        .param("q", query)
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(hasSize(size)))
                .andExpect(jsonPath("$[0]").value("Canada"))
                .andExpect(jsonPath("$[1]").value("China"))
                .andExpect(jsonPath("$[2]").value("Congo"))
                .andReturn();
    }

    @Test
    void getCountries_whithPrefixCan() throws Exception {
        String query = "Can";
        int size = 1;
        mvc.perform(get("/v1/collection-events/countries/autocomplete")
                        .param("q", query)
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(hasSize(size)))
                .andExpect(jsonPath("$[0]").value("Canada"))
                .andReturn();
    }

    @Test
    void getCountries_withPrefixEmpty() throws Exception {
        String query = "";
        int size = 5;

        mvc.perform(get("/v1/collection-events/countries/autocomplete")
                        .param("q", query)
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty())
                .andReturn();
    }

    @Test
    void getContinents_whithPrefixA() throws Exception {
        String query = "A";
        int size = 3;
        mvc.perform(get("/v1/collection-events/continents/autocomplete")
                        .param("q", query)
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(hasSize(size)))
                .andExpect(jsonPath("$[0]").value("Africa"))
                .andExpect(jsonPath("$[1]").value("America"))
                .andExpect(jsonPath("$[2]").value("Asia"))
                .andReturn();
    }

    @Test
    void getContinents_whithSize2() throws Exception {
        String query = "A";
        int size = 2;
        mvc.perform(get("/v1/collection-events/continents/autocomplete")
                        .param("q", query)
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(hasSize(size)))
                .andExpect(jsonPath("$[0]").value("Africa"))
                .andExpect(jsonPath("$[1]").value("America"))
                .andReturn();
    }

    @Test
    void getContinents_withPrefixEmpty() throws Exception {
        String query = "";
        int size = 5;

        mvc.perform(get("/v1/collection-events/continents/autocomplete")
                        .param("q", query)
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty())
                .andReturn();
    }

    @Test
    void getCollectors_whithPrefixPau() throws Exception {
        String query = "Pau";
        int size = 3;
        mvc.perform(get("/v1/collection-events/collectors/autocomplete")
                        .param("q", query)
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(hasSize(size)))
                .andExpect(jsonPath("$[0]").value("Paulette, K."))
                .andExpect(jsonPath("$[1]").value("Pauline, G."))
                .andExpect(jsonPath("$[2]").value("Paul, M."))
                .andReturn();
    }

    @Test
    void getCollectors_withPrefixEmpty() throws Exception {
        String query = "";
        int size = 5;

        mvc.perform(get("/v1/collection-events/collectors/autocomplete")
                        .param("q", query)
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty())
                .andReturn();
    }
}
