package org.recolnat.collection.manager.taxon.api.web;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceDBTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("int")
@Sql(scripts = {"classpath:clean_data_all_taxon.sql", "classpath:init_data_all_taxon.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_all_taxon.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TaxonRetrieveResourceITest extends AbstractResourceDBTest {
    @Autowired
    protected MockMvc mvc;

    @Test
    void getFamilyTaxons_whenPrefixIsAna() throws Exception {
        String query = "Ana";
        int size = 3;

        mvc.perform(get("/v1/taxons/families/autocomplete")
                        .param("q", query)
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(hasSize(size)))
                .andExpect(jsonPath("$[0]").value("Anacardiaceae"))
                .andExpect(jsonPath("$[1]").value("Ananaceae"))
                .andExpect(jsonPath("$[2]").value("Anatidae"))
                .andReturn();
    }

    @Test
    void getFamilyTaxons_whenPrefixIsEmpty() throws Exception {
        String query = "";
        int size = 5;

        mvc.perform(get("/v1/taxons/families/autocomplete")
                        .param("q", query)
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty())
                .andReturn();
    }

    @Test
    void getGeneraTaxons_whenPrefixIsAn() throws Exception {
        String query = "An";
        int size = 2;

        mvc.perform(get("/v1/taxons/genera/autocomplete")
                        .param("q", query)
                        .param("family", "")
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(hasSize(size)))
                .andExpect(jsonPath("$[0]").value("Anas"))
                .andExpect(jsonPath("$[1]").value("Annona"))
                .andReturn();
    }

    @Test
    void getGeneraTaxons_whenPrefixIsEmptyAndSize1() throws Exception {
        String query = "";
        int size = 1;

        mvc.perform(get("/v1/taxons/genera/autocomplete")
                        .param("q", query)
                        .param("family", "")
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(hasSize(0)))
                .andReturn();
    }

    @Test
    void getGeneraTaxons_whenSizeIsZero() throws Exception {
        String query = "";
        int size = 0;

        mvc.perform(get("/v1/taxons/genera/autocomplete")
                        .param("q", query)
                        .param("family", "")
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(size)))
                .andExpect(jsonPath("$").isArray())
                .andReturn();
    }

    @Test
    void getGeneraTaxons_whenFamilyIsAnanaceae() throws Exception {
        String query = "Ann";
        int size = 1;

        mvc.perform(get("/v1/taxons/genera/autocomplete")
                        .param("q", query)
                        .param("family", "Ananaceae")
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(hasSize(1)))
                .andExpect(jsonPath("$[0]").value("Annona"))
                .andReturn();
    }

    @Test
    void getSpecificEpithetTaxons_whenPrefixIsLup() throws Exception {
        String query = "Lup";
        int size = 2;
        System.out.println("Debut du test ");
        mvc.perform(get("/v1/taxons/specific-epithets/autocomplete")
                        .param("q", query)
                        .param("genus", "")
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(hasSize(size)))
                .andExpect(jsonPath("$[0]").value("Lupindica"))
                .andExpect(jsonPath("$[1]").value("Lupus"))
                .andReturn();
    }

    @Test
    void getSpecificEpithetTaxons_whenPrefixAndSize1() throws Exception {
        String query = "Cur";
        int size = 1;

        mvc.perform(get("/v1/taxons/specific-epithets/autocomplete")
                        .param("q", query)
                        .param("genus", "")
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(hasSize(size)))
                .andExpect(jsonPath("$[0]").value("Cursifolia"))
                .andReturn();
    }

    @Test
    void getSpecificEpithetTaxons_whenGenusIsAnas() throws Exception {
        String query = "C";
        int size = 1;

        mvc.perform(get("/v1/taxons/specific-epithets/autocomplete")
                        .param("q", query)
                        .param("genus", "Anas")
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(size)))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("Curvirostra"))
                .andReturn();
    }
}
