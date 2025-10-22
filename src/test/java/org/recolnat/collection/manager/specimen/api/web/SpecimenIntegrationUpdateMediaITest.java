package org.recolnat.collection.manager.specimen.api.web;

import io.recolnat.model.MediaDTO;
import io.recolnat.model.SpecimenIntegrationRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.collection.api.web.AbstractResourceElasticTest;
import org.recolnat.collection.manager.repository.entity.MediaJPA;
import org.recolnat.collection.manager.repository.jpa.MediaJPARepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.recolnat.collection.manager.api.domain.enums.RoleEnum.ADMIN_INSTITUTION;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Sql(scripts = {"classpath:clean_data_specimen_for_media.sql", "classpath:init_data_specimen_for_media.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"classpath:clean_data_specimen_for_media.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles("int")
@Slf4j
public class SpecimenIntegrationUpdateMediaITest extends AbstractResourceElasticTest {
    public static final String SPECIMEN_ID = "359eefe3-901a-4faf-bc3e-6f3fa266a465";
    public static final String COLLECTION_ID = "8342cf1d-f202-4c10-9037-2e2406ce7331";
    public static final String MEDIA_ID_1 = "91a1bf3f-5cdd-4a9d-860a-c5c1ef22f48b";
    public static final String MEDIA_ID_2_INITIAL_COVER = "b5e0c7e6-b1c7-4d9f-8c7b-0f9e8d7c6b5a";
    public static final String MEDIA_ID_3 = "a4d9b6d5-a0b6-4c8e-7b6a-9e8d7c6b5a4b";
    @Autowired
    protected MediaJPARepository mediaJPARepository;
    @Value(value = "classpath:specimenMediaData.json")
    private Resource specimenData;

    @Test
    @DisplayName("Update Specimen - Standard update from JSON")
    void updateSpecimen_ok() throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);
        List<MediaDTO> mediasToUpdate = body.getMedias();
        UUID media1UUID = UUID.fromString(MEDIA_ID_1);
        UUID media2UUID = UUID.fromString(MEDIA_ID_2_INITIAL_COVER);
        UUID media3UUID = UUID.fromString(MEDIA_ID_3);
        for (MediaDTO media : mediasToUpdate) {
            media.setIsCover(media.getId().equals(media3UUID));
        }
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder()
                .ui("50f4978a-da62-4fde-8f38-5003bd43ff64")
                .institution(1).role(ADMIN_INSTITUTION.name())
                .collections(List.of(UUID.fromString("8342cf1d-f202-4c10-9037-2e2406ce7331"), UUID.fromString("9a342a92-6fe8-48d3-984e-d1731c051666")))
                .build());
        mvc.perform(
                        put("/v1/specimens/{specimenId}", SPECIMEN_ID)
                                .with(opaqueToken()
                                        .attributes(att -> att.put("sub", UID_CONST))
                                        .authorities(new SimpleGrantedAuthority("ADMIN_COLLECTION")))
                                .with(user("test")
                                        .roles("ADMIN_COLLECTION"))
                                .with(csrf())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("collectionId", notNullValue()))
                .andExpect(header().string("specimenId", notNullValue()));
        MediaJPA media1 = mediaJPARepository.findById(media1UUID).orElseThrow();
        MediaJPA media2 = mediaJPARepository.findById(media2UUID).orElseThrow();
        MediaJPA media3 = mediaJPARepository.findById(media3UUID).orElseThrow();
        assertThat(media1.getIsCover()).isFalse();
        assertThat(media2.getIsCover()).isFalse();
        assertThat(media3.getIsCover()).isTrue();
    }

    @Test
    @DisplayName("Update Specimen - When multiple covers sent, should keep only the first one")
    void updateSpecimen_whenMultipleCoversSent_shouldKeepOnlyFirst() throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);
        List<MediaDTO> mediasToUpdate = body.getMedias();
        UUID media1UUID = UUID.fromString(MEDIA_ID_1);
        UUID media2UUID = UUID.fromString(MEDIA_ID_2_INITIAL_COVER);
        UUID media3UUID = UUID.fromString(MEDIA_ID_3);
        for (MediaDTO media : mediasToUpdate) {
            media.setIsCover(media.getId().equals(media1UUID) || media.getId().equals(media3UUID));
        }
        when(authenticationService.findUserAttributes()).thenReturn(UserAttributes.builder().ui(UUID.randomUUID().toString()).role(ADMIN_INSTITUTION.name())
                .institution(1).collections(List.of(UUID.fromString(COLLECTION_ID))).build());
        mvc.perform(put("/v1/specimens/{specimenId}", SPECIMEN_ID)
                        .with(opaqueToken().attributes(att -> att.put("sub", UID_CONST)).authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                        .with(user("test").roles("ADMIN_INSTITUTION"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("specimenId", SPECIMEN_ID));
        MediaJPA media1 = mediaJPARepository.findById(media1UUID).orElseThrow();
        MediaJPA media2 = mediaJPARepository.findById(media2UUID).orElseThrow();
        MediaJPA media3 = mediaJPARepository.findById(media3UUID).orElseThrow();
        assertThat(media1.getIsCover()).isTrue();
        assertThat(media2.getIsCover()).isFalse();
        assertThat(media3.getIsCover()).isFalse();
    }

    @Test
    @DisplayName("Update Specimen - When zero covers sent, should result in zero covers")
    void updateSpecimen_whenZeroCoversSent_shouldResultInZeroCovers() throws Exception {
        var body = objectMapper.readValue(specimenData.getInputStream(), SpecimenIntegrationRequestDTO.class);
        List<MediaDTO> mediasToUpdate = body.getMedias();
        for (MediaDTO media : mediasToUpdate) {
            if (media != null) {
                media.setIsCover(false);
            }
        }
        when(authenticationService.findUserAttributes())
                .thenReturn(UserAttributes
                        .builder()
                        .ui(UUID.randomUUID().toString())
                        .role(ADMIN_INSTITUTION.name()).institution(1).collections(List.of(UUID.fromString(COLLECTION_ID))).build());
        mvc.perform(put("/v1/specimens/{specimenId}", SPECIMEN_ID)
                        .with(opaqueToken().attributes(att -> att.put("sub", UID_CONST)).authorities(new SimpleGrantedAuthority("ADMIN_INSTITUTION")))
                        .with(user("test").roles("ADMIN_INSTITUTION"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("specimenId", SPECIMEN_ID));
        UUID media1UUID = UUID.fromString(MEDIA_ID_1);
        UUID media2UUID = UUID.fromString(MEDIA_ID_2_INITIAL_COVER);
        UUID media3UUID = UUID.fromString(MEDIA_ID_3);
        List<MediaJPA> finalMedias = mediaJPARepository.findAllById(List.of(media1UUID, media2UUID, media3UUID));
        assertThat(finalMedias).allMatch(m -> !Boolean.TRUE.equals(m.getIsCover()));
    }
}
