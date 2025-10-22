package org.recolnat.collection.manager.specimen.api.web;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.utils.IOUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.SpecimenIndex;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceElasticTest;
import org.recolnat.collection.manager.common.check.service.ControlAttribut;
import org.recolnat.collection.manager.connector.api.domain.MediaDetailsOutput;
import org.recolnat.collection.manager.connector.api.domain.MediathequeOutput;
import org.recolnat.collection.manager.repository.jpa.SpecimenJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("int")
@AutoConfigureMockMvc(addFilters = false)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SpecimenMediaResourceITest extends AbstractResourceElasticTest {

    public static final String COLLECTION_ID = "8342cf1d-f202-4c10-9037-2e2406ce7331";
    public static final String SPECIMEN_ID = "359eefe3-901a-4faf-bc3e-6f3fa266a465";
    public static final String SPECIMEN_DRAFT_ID = "9fdca0c7-2712-46a6-aff5-f88fe6999c1e";
    public static final String SPECIMEN_REVIEW_ID = "f98e0678-8c27-4867-bbfc-12a09dfbce3d";

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected SpecimenJPARepository specimenJPARepository;
    @MockBean
    protected ControlAttribut checkAttribut;
    @Autowired
    @Qualifier("elasticsearchClient")
    ElasticsearchClient elasticsearchClient;
    @Value("${index.specimen}")
    String indexSpecimen;

    @BeforeEach
    public void setUp() {
    }

    @Test
    @Sql(scripts = {"classpath:clean_data_specimen_for_update.sql", "classpath:init_data_specimen_for_media.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void addMedia_should_be_ok() throws Exception {

        String fileName = "fileName";
        File resource = new ClassPathResource("images/papillon.png").getFile();
        MockMultipartFile oneFile;
        try (InputStream input = Files.newInputStream(resource.toPath())) {
            oneFile = new MockMultipartFile(fileName, resource.getName(), "multipart/form-data",
                    IOUtils.toByteArray(input));
        }

        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .collections(List.of(UUID.fromString("82e20227-b0d7-46b4-b44d-2257d86f67b1"))).institution(1).build());

        doNothing().when(checkAttribut).checkUserRightsOnCollection(UUID.fromString(COLLECTION_ID));

        elasticsearchClient.index(i -> i.index(indexSpecimen)
                .id(SPECIMEN_ID)
                .document(SpecimenIndex.builder().id(SPECIMEN_ID).municipality("municipalité").build()));

        MediaDetailsOutput mediaDetail = MediaDetailsOutput.builder()
                .url("https://mediaphoto.mnhn.fr/media/1659622911057fhc2f5XLpGvbRLD8")
                .uid("1648799815425Or21N8YMlkAaHvwZ").mimeType("image/jpeg").build();
        MediathequeOutput media = MediathequeOutput.builder().success(200).media(mediaDetail).build();
        when(mediathequeApiClient.savePicture(oneFile)).thenReturn(new ResponseEntity<>(media, HttpStatus.CREATED));

        var builder = multipart("/v1/specimens/{specimenId}/medias", SPECIMEN_ID);
        builder.with(request -> {
            request.setMethod("PATCH");
            return request;
        });
        mvc.perform(builder.file(oneFile)
                        .with(opaqueToken().attributes(att -> att.put("sub", "712215d4-c795-11ec-9d64-0242ac120002"))
                                .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                        .with(user("test").roles("ADMIN_INSTITUTION")).with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", CoreMatchers.notNullValue()));

    }

    @Test
    @Sql(scripts = {"classpath:init_data_specimenDraft_for_media.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenSpecimenDraftWithMedia_whenSaveMediaDraft_thenReturnSpecimenDraftWithUrlMedia() throws Exception {
        // Given - precondition or setup
        String fileName = "fileName";
        File resource = new ClassPathResource("images/lucane-cerf.jpg").getFile();
        MockMultipartFile oneFile;
        try (InputStream input = Files.newInputStream(resource.toPath())) {
            oneFile = new MockMultipartFile(fileName, resource.getName(), "multipart/form-data",
                    IOUtils.toByteArray(input));
        }
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .collections(List.of(UUID.fromString("82e20227-b0d7-46b4-b44d-2257d86f67b1"))).institution(1).build());
        doNothing().when(checkAttribut).checkUserRightsOnCollection(UUID.fromString(COLLECTION_ID));
        MediaDetailsOutput mediaDetail = MediaDetailsOutput.builder()
                .url("https://mediaphoto.mnhn.fr/media/1659622911057fhc2f5XLpGvbRLD8")
                .uid("1648799815425Or21N8YMlkAaHvwZ").mimeType("image/jpeg").build();
        MediathequeOutput media = MediathequeOutput.builder().success(200).media(mediaDetail).build();
        // When - action or the behaviour
        when(mediathequeApiClient.savePicture(oneFile)).thenReturn(new ResponseEntity<>(media, HttpStatus.CREATED));
        var builder = multipart("/v1/specimens/{specimenId}/medias/draft",
                SPECIMEN_DRAFT_ID);
        builder.with(request -> {
            request.setMethod("PATCH");
            return request;
        });
        // Then - verify the output
        mvc.perform(builder.file(oneFile)
                        .with(opaqueToken().attributes(att -> att.put("sub", "712215d4-c795-11ec-9d64-0242ac120002"))
                                .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                        .with(user("test").roles("ADMIN_INSTITUTION")).with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", CoreMatchers.notNullValue()));
    }

    @Test
    @Sql(scripts = {"classpath:init_data_specimenDraft_for_media.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void givenSpecimenRewiewedWithMedia_whenSaveMediaReviewed_thenReturnSpecimen() throws Exception {
        // Given - precondition or setup
        String fileName = "fileName";
        File resource = new ClassPathResource("images/Insecte_1.jpg").getFile();
        MockMultipartFile oneFile;
        try (InputStream input = Files.newInputStream(resource.toPath())) {
            oneFile = new MockMultipartFile(fileName, resource.getName(), "multipart/form-data",
                    IOUtils.toByteArray(input));
        }
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("82e20227-b0d7-46b4-b44d-2257d86f67b1")
                .collections(List.of(UUID.fromString("82e20227-b0d7-46b4-b44d-2257d86f67b1"))).institution(1).build());
        doNothing().when(checkAttribut).checkUserRightsOnCollection(UUID.fromString(COLLECTION_ID));
        MediaDetailsOutput mediaDetail = MediaDetailsOutput.builder()
                .url("https://mediaphoto.mnhn.fr/media/1660655695742dwrn9mGVIDPbzbEL")
                .uid("453af161-2b70-4477-b69a-f47f00e51ccb").mimeType("image/jpeg").build();
        MediathequeOutput media = MediathequeOutput.builder().success(200).media(mediaDetail).build();
        // When - action or the behaviour
        when(mediathequeApiClient.savePicture(oneFile)).thenReturn(new ResponseEntity<>(media, HttpStatus.CREATED));
        var builder = multipart("/v1/specimens/{specimenId}/medias/review",
                SPECIMEN_REVIEW_ID);
        builder.with(request -> {
            request.setMethod("PATCH");
            return request;
        });
        // Then - verify the output
        mvc.perform(builder.file(oneFile)
                        .with(opaqueToken().attributes(att -> att.put("sub", "712215d4-c795-11ec-9d64-0242ac120002"))
                                .authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                        .with(user("test").roles("ADMIN_COLLECTION")).with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", CoreMatchers.notNullValue()));
    }

}
