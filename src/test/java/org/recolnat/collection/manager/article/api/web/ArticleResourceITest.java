package org.recolnat.collection.manager.article.api.web;

import io.recolnat.model.ArticleResultPageResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.Article;
import org.recolnat.collection.manager.api.domain.ConnectedUser;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceDBTest;
import org.recolnat.collection.manager.connector.api.domain.MediaDetailsOutput;
import org.recolnat.collection.manager.connector.api.domain.MediathequeOutput;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("int")
@Slf4j
public class ArticleResourceITest extends AbstractResourceDBTest {

    private static final String ART_ID = UUID.fromString("477ee750-5366-4991-8a56-dd3985ae5ad3").toString();

    public static ConnectedUser getConnectedUser() {
        return ConnectedUser.builder()
                .userId(UUID.fromString("a416ce96-bc14-48bc-850e-80aa95ca7221"))
                .userName("admin").build();
    }

    @Test
    void getArticles() throws Exception {
        var resp = mvc.perform(MockMvcRequestBuilders.get("/v1/public/articles")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andReturn();
        var result = objectMapper.readValue(resp.getResponse().getContentAsString(), ArticleResultPageResponseDTO.class);

        assertThat(result.getNumberOfElements()).isPositive();
        assertThat(result.getArticles()).isNotEmpty();
    }

    @Test
    void addArticle() throws Exception {
        MockMultipartFile file
                = new MockMultipartFile(
                "media",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Media work as mock!".getBytes()
        );

        // When - action or the behaviour
        final MediathequeOutput mediaDetailsOutput = MediathequeOutput.builder()
                .media(MediaDetailsOutput.builder()
                        .url("https://mediaphoto.mnhn.fr/media/1671041236335AUUo9kb5wF0rbw9T").build()).build();

        when(mediathequeApiClient.savePicture(file)).thenReturn(new ResponseEntity<>(mediaDetailsOutput, HttpStatusCode.valueOf(HttpStatus.SC_SUCCESS)));

        UUID uidAdmin = UUID.fromString("b9a746ef-3dd6-4954-8e14-a317e380524e");
        when(authenticationService.findUserAttributes()).thenReturn(buildAdminUser(uidAdmin));

        when(authenticationService.getConnected()).thenReturn(getConnectedUser());

        Article.builder().author("author").media(file).build();

        var resp = mvc.perform(MockMvcRequestBuilders.multipart("/v1/articles")
                        .file(file)
                        .param("author", "Samuel")
                        .param("content", "content media html")
                        .param("title", "Plantae article title")
                        .param("creationDate", "2022-12-15")
                        .param("state", "PUBLISHED")
                )
                // Then - verify the output
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, notNullValue()))
                .andReturn();
        log.debug(resp.toString());
    }

    @Test
    void addArticle_ko() throws Exception {
        MockMultipartFile file
                = new MockMultipartFile(
                "media",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Media work as mock!".getBytes()
        );

        // When - action or the behaviour
        final MediathequeOutput mediaDetailsOutput = MediathequeOutput.builder()
                .media(MediaDetailsOutput.builder()
                        .url("https://mediaphoto.mnhn.fr/media/1671041236335AUUo9kb5wF0rbw9T").build()).build();

        when(mediathequeApiClient.savePicture(file)).thenReturn(new ResponseEntity<>(mediaDetailsOutput, HttpStatusCode.valueOf(HttpStatus.SC_SUCCESS)));
        UUID uidAdmin = UUID.fromString("b9a746ef-3dd6-4954-8e14-a317e380524e");
        when(authenticationService.findUserAttributes()).thenReturn(buildAdminUser(uidAdmin));

        when(authenticationService.getConnected()).thenReturn(getConnectedUser());

        mvc.perform(MockMvcRequestBuilders.multipart("/v1/articles")
                        .file(file)
                        .param("content", "content media html")
                        .param("title", "Plantae article title")
                        .param("creationDate", "2022-12-15")
                        .param("state", "PUBLISHED")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateArticle() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "media",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Media work as mock!".getBytes()
        );
        UUID uidAdmin = UUID.fromString("b9a746ef-3dd6-4954-8e14-a317e380524e");
        // When - action or the behaviour
        final MediathequeOutput mediaDetailsOutput = MediathequeOutput.builder()
                .media(MediaDetailsOutput.builder()
                        .url("https://mediaphoto.mnhn.fr/media/1671041236335AUUo9kb5wF0rbw9T").build()).build();

        when(mediathequeApiClient.savePicture(file)).thenReturn(new ResponseEntity<>(mediaDetailsOutput, HttpStatusCode.valueOf(HttpStatus.SC_SUCCESS)));
        when(authenticationService.findUserAttributes()).thenReturn(buildAdminUser(uidAdmin));
        when(authenticationService.getConnected()).thenReturn(getConnectedUser());
        Article.builder().author("author").media(file).build();

        MockMultipartHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.multipart("/v1/articles/" + ART_ID);
        builder.with(request -> {
            request.setMethod(HttpMethod.PUT.name());
            return request;
        });

        mvc.perform(builder
                        .file(file).param("author", "Samuel")
                        .param("content", "content media html")
                        .param("title", "Plantae article title")
                        .param("creationDate", "2022-12-15")
                        .param("state", "PUBLISHED")
                )
                // Then - verify the output
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.LOCATION, notNullValue()))
                .andExpect(header().string("articleId", notNullValue()))
                .andExpect(header().string("articleId", equalTo(ART_ID)))
                .andReturn();
    }

    @Test
    void getArticle_byId_ok() throws Exception {
        UUID uidAdmin = UUID.fromString("b9a746ef-3dd6-4954-8e14-a317e380524e");
        // When - action or the behaviour

        when(authenticationService.findUserAttributes()).thenReturn(buildAdminUser(uidAdmin));

        mvc.perform(MockMvcRequestBuilders.get("/v1/articles/{id}", ART_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$.id", equalTo(ART_ID)))
                .andReturn();
    }

    private UserAttributes buildAdminUser(UUID uidAdmin) {
        return UserAttributes.builder().ui(uidAdmin.toString()).role(RoleEnum.ADMIN.name()).build();
    }

}
