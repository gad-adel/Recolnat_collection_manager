package org.recolnat.collection.manager.specimen.api.domain.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.SpecimenIndex;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceElasticTest;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.mapper.SpecimenMapper;
import org.recolnat.collection.manager.connector.api.domain.MediaDetailsOutput;
import org.recolnat.collection.manager.connector.api.domain.MediathequeOutput;
import org.recolnat.collection.manager.service.MediaIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("int")
public class MediaIntegrationServiceImplITest extends AbstractResourceElasticTest {

    static UUID specimenId = UUID.fromString("359eefe3-901a-4faf-bc3e-6f3fa266a465");
    static UUID specimenIdWithoutMedia = UUID.fromString("bb4b6db8-4fee-40eb-9a0c-3fb57fdbf940");
    @Autowired
    @Qualifier("elasticsearchClient")
    ElasticsearchClient elasticsearchClient;
    @Value("${index.specimen}")
    String indexSpecimen;
    @Autowired
    private MediaIntegrationService mediaIntegrationService;
    @Autowired
    private SpecimenMapper specimenMapper;

    /**
     * on initialize l index car l'update implique la suppression eventuel du specimen
     * sur elastic (ne fonctionne que si elastic possede l index). le mapping et le setting sont optionnels
     */
    @BeforeEach
    void initIndex() {
        try {
            BooleanResponse resp = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(b -> b.index("rcn_specimen_short")));

            if (!resp.value()) {
                InputStream inputm = this.getClass().getResourceAsStream("/mapping.json");
                InputStream inputset = this.getClass().getResourceAsStream("/setting.json");
                CreateIndexRequest request = CreateIndexRequest
                        .of(b -> b.index("rcn_specimen_short").mappings(m -> m.withJson(inputm))
                                .settings(s -> s.withJson(inputset)));
                elasticsearchClient.indices().create(request);
            }
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    @Sql(scripts = {"classpath:clean_data_specimen_for_update.sql", "classpath:init_data_specimen_for_media.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("JUnit test for add media to specimen")
    void add_media_to_specimen_ok() throws IOException {
        // Given
        String fileName = "papillon.png";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", fileName,
                "image/jpeg", "test data".getBytes());

        elasticsearchClient.index(i -> i.index(indexSpecimen)
                .id(specimenId.toString())
                .document(SpecimenIndex.builder().id(specimenId.toString()).municipality("municipalité").build()));

        var mediaDetail = MediaDetailsOutput.builder()
                .url("https://mediaphoto.mnhn.fr/media/1648799815425Or21N8YMlkAaHvwZ")
                .uid("1648799815425Or21N8YMlkAaHvwZ")
                .mimeType("image/jpeg").build();
        var specimenMediaResponse = MediathequeOutput.builder()
                .success(1)
                .media(mediaDetail).build();

        // When
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("user_1").institution(1).role("ADMIN_INSTITUTION")
                .collections(List.of(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331"))).build());

        when(mediathequeApiClient.savePicture(mockMultipartFile))
                .thenReturn(new ResponseEntity<>(specimenMediaResponse, HttpStatus.CREATED));

        List<String> response = mediaIntegrationService.add(specimenId, List.of(mockMultipartFile));
        // Then
        assertThat(response).isNotEmpty();
    }

    @Test
    @Sql(scripts = {"classpath:clean_data_specimen_for_update.sql", "classpath:init_data_specimen_for_media.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("JUnit test for add media to specimen with media sent not the same name")
    void givenSpecimenWithMedia_whenAdd_thenThrowsException() throws IOException {
        // Given - precondition or setup
        String fileName = "papillon_.png";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", fileName,
                "image/jpeg", "test data".getBytes());

        var mediaDetail = MediaDetailsOutput.builder()
                .url("https://mediaphoto.mnhn.fr/media/1648799815425Or21N8YMlkAaHvwZ")
                .uid("1648799815425Or21N8YMlkAaHvwZ")
                .mimeType("image/jpeg").build();
        var specimenMediaResponse = MediathequeOutput.builder()
                .success(1)
                .media(mediaDetail).build();
        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("user_1").institution(1).role("ADMIN_INSTITUTION")
                .collections(List.of(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331"))).build());

        when(mediathequeApiClient.savePicture(mockMultipartFile))
                .thenReturn(new ResponseEntity<>(specimenMediaResponse, HttpStatus.CREATED));
        List<MultipartFile> listeOf = List.of(mockMultipartFile);
        CollectionManagerBusinessException exception = assertThrows(CollectionManagerBusinessException.class,
                () -> mediaIntegrationService.add(specimenId, listeOf));
        // Then - verify the output
        assertThat(exception.getCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    @Sql(scripts = {"classpath:clean_data_specimen_for_update.sql", "classpath:init_data_specimen_for_media.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("JUnit test for add media to specimen when throw Exception")
    void givenSpecimenWithoutMedia_whenAdd_thenThrowsCollectionManagerBusinessException() {
        // Given - precondition or setup
        String fileName = "papillon.png";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", fileName,
                "image/jpeg", "test data".getBytes());
        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("user_1").institution(1).role("ADMIN_INSTITUTION")
                .collections(List.of(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331"))).build());

        List<MultipartFile> listeOf = List.of(mockMultipartFile);
        CollectionManagerBusinessException exception = assertThrows(CollectionManagerBusinessException.class,
                () -> mediaIntegrationService.add(specimenIdWithoutMedia, listeOf));
        // Then - verify the output
        assertThat(exception.getCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    @Sql(scripts = {"classpath:clean_data_specimen_for_update.sql", "classpath:init_data_specimen_for_media.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("JUnit test for add media to specimen when mediatheque API throw Exception")
    void givenSpecimenWithMedia_whenAdd_thenThrowsIOException() throws IOException {
        // Given - precondition or setup
        String fileName = "papillon.png";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", fileName,
                "image/jpeg", "test data".getBytes());
        // When - action or the behaviour
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("user_1").institution(1).role("ADMIN_INSTITUTION")
                .collections(List.of(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331"))).build());

        when(mediathequeApiClient.savePicture(mockMultipartFile))
                .thenThrow(new IOException());
        List<MultipartFile> listeOf = List.of(mockMultipartFile);
        CollectionManagerBusinessException exception = assertThrows(CollectionManagerBusinessException.class,
                () -> mediaIntegrationService.add(specimenId, listeOf));
        // Then - verify the output
        assertThat(exception.getCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }


    @Test
    @Sql(scripts = {"classpath:clean_data_specimen_for_update.sql", "classpath:init_data_specimen_for_media.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:clean_data_specimen_for_update.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("JUnit test for add media to specimen")
    void add_media_to_specimen_throw() throws IOException {
        // Given
        String fileName = "papillon.png";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", fileName,
                "image/jpeg", "test data".getBytes());

        var mediaDetail = MediaDetailsOutput.builder()
                .url("https://mediaphoto.mnhn.fr/media/1648799815425Or21N8YMlkAaHvwZ")
                .uid("1648799815425Or21N8YMlkAaHvwZ")
                .mimeType("image/jpeg").build();
        var specimenMediaResponse = MediathequeOutput.builder()
                .success(1)
                .media(mediaDetail).build();

        // When
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("user_1").institution(1).role("ADMIN_INSTITUTION")
                .collections(List.of(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331"))).build());

        when(mediathequeApiClient.savePicture(mockMultipartFile))
                .thenReturn(new ResponseEntity<>(specimenMediaResponse, HttpStatus.CREATED));

        List<String> list = assertDoesNotThrow(() ->
                mediaIntegrationService.add(specimenId, List.of(mockMultipartFile)));

        assertThat(list).hasSize(3);
        assertThat(list.stream().filter(Objects::nonNull).findFirst().orElse("")).isEqualTo("https://mediaphoto.mnhn.fr/media/1648799815425Or21N8YMlkAaHvwZ");
    }

}
