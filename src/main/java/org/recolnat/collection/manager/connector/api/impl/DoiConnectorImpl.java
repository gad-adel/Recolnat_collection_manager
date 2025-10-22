package org.recolnat.collection.manager.connector.api.impl;

import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.connector.api.DoiConnector;
import org.recolnat.collection.manager.connector.api.domain.Doi;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.List;

@Service
@Slf4j
public class DoiConnectorImpl implements DoiConnector {

    @Override
    public Doi getDoi(String id) {
        Doi doi;
        doi = getRequestHeadersUriSpec()
                .uri(uriBuilder -> uriBuilder.path(id).build())
                .retrieve()
                .onStatus(httpStatus -> (
                        (httpStatus.is4xxClientError() && httpStatus != HttpStatus.NOT_FOUND) || httpStatus.is5xxServerError()), response -> {
                    log.error("Error Doi API {}", response.statusCode());
                    throw new CollectionManagerBusinessException(response.statusCode()
                            .value(), "DOI_EXCEPTION", "Erreur lors de la récupération du DOI");
                })
                .bodyToMono(Doi.class)
                .onErrorResume(WebClientResponseException.NotFound.class, notFound -> Mono.empty())
                .block();

        return doi;
    }

    private WebClient.RequestHeadersUriSpec<?> getRequestHeadersUriSpec() {
        return WebClient.builder().baseUrl("https://doi.org")
                .clientConnector(
                        new ReactorClientHttpConnector(HttpClient
                                .create()
                                .followRedirect(true)
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3600)))
                .defaultHeaders(httpHeaders -> httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON)))
                .build().get();
    }
}
