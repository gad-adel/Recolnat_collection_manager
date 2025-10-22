package org.recolnat.collection.manager.collection.api.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.connector.api.MediathequeService;
import org.recolnat.collection.manager.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
@ActiveProfiles("int")
@AutoConfigureMockMvc(addFilters = false)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractResourceDBTest {

    public static final String COLLECTION_ID = "9a342a92-6fe8-48d3-984e-d1731c051666";
    public static final String COLLECTION_ID_KO = "e82e315f-c4a0-4a3d-942c-19c26151a1d2";
    public static final String INCORECT_COLLECTION_ID = "712215d4-c795-11ec-9d64-0242ac120002";
    public static final String UID_CONST = "712215d4-c795-11ec-9d64-0242ac120003";
    public static final String ARTICLE_ID = "477ee750-5366-4991-8a56-dd3985ae5a4f";
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16.0-bullseye")
            .withDatabaseName("test").withReuse(false)
            .withPassword("test")
            .withUsername("test");

    static {
        postgreSQLContainer.start();
    }

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockBean
    protected AuthenticationService authenticationService;
    @MockBean
    protected MediathequeService mediathequeApiClient;

}
