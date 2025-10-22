package org.recolnat.collection.manager.connector.api.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.api.domain.enums.ParamBodyMediathequeEnum;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.connector.api.MediathequeService;
import org.recolnat.collection.manager.connector.api.domain.MediathequeOutput;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediathequeServiceImpl implements MediathequeService {

    private final WebClient mediathequeWebClient;


    @Override
    public ResponseEntity<MediathequeOutput> savePicture(MultipartFile file) {
        var block = mediathequeWebClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/stream").build())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(buildParamBody(file.getResource()))).retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    log.error("Error Mediatheque Client {}", response.statusCode());
                    throw new MediathequeException(HttpStatus.INTERNAL_SERVER_ERROR, "error.api.media", "We can't post resources on mediatheque API");
                }).toEntity(MediathequeOutput.class).block();
        if (block != null) {
            toHttp(Objects.requireNonNull(block.getBody()));
        }
        return block;

    }

    @Override
    public ResponseEntity<Void> deletePicture(String uid) {
        return mediathequeWebClient
                .delete()
                .uri(uid)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    log.error("Error Mediatheque Client status code: {}", response.statusCode());
                    log.error("Error Mediatheque UID : {}", uid);
                    throw new MediathequeException(HttpStatus.INTERNAL_SERVER_ERROR, "error.api.media", "We can't delete resources from mediatheque API");
                })
                .toBodilessEntity().block();
    }

    private MultiValueMap<String, HttpEntity<?>> buildParamBody(Resource file) {
        var paramBody = new MultipartBodyBuilder();
        paramBody.part(ParamBodyMediathequeEnum.STREAMFILE.getParamBody(), file);
        return paramBody.build();
    }

    private void toHttp(MediathequeOutput mediathequeOutput) {
        var sbImgUrl = new StringBuilder(mediathequeOutput.getMedia().getUrl());
        CharSequence subSequence = sbImgUrl.subSequence(0, 5);
        if ("https".contentEquals(subSequence)) {
            mediathequeOutput.getMedia().setUrl(sbImgUrl.deleteCharAt(4).toString());
        }
    }

}
