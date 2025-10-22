package org.recolnat.collection.manager.connector.api.impl;

import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.connector.api.TaxonRefConnector;
import org.recolnat.collection.manager.connector.api.domain.TaxonRef;
import org.recolnat.collection.manager.connector.api.domain.TaxonRefOut;
import org.recolnat.collection.manager.connector.api.domain.TaxonRefSuggestionOut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
@ConditionalOnProperty(value = "taxonRef.api.mock", havingValue = "false")
public class TaxonRefConnectorImpl implements TaxonRefConnector {

    public static final String TECH_TAX_REF_EXCEPTION = "TECH_TAX_REF_EXCEPTION";
    public static final String API_V_3_REF_TAXONOMIC_MATCH_SCIENTIFICNAME = "/api/v3/ref/taxonomic/match/scientificname";
    public static final String API_V_3_REF_TAXONOMIC_SUGGESTION_SCIENTIFICNAME = "/api/v3/ref/taxonomic/suggestion/scientificname";
    @Value("${taxonRef.api.base_url}")
    private String taxonRefUrl;
    @Value("${taxonRef.api.api_key_name}")
    private String taxonRefKeyName;
    @Value("${taxonRef.api.api_key_value}")
    private String taxonRefKey;


    @Override
    public List<TaxonRef> findByScientifiName(String scientificName) {
        var taxonRefOut = TaxonRefOut.builder().taxonList(List.of()).build();
        try {
            taxonRefOut = getRequestHeadersUriSpec().uri(uriBuilder -> uriBuilder.path(API_V_3_REF_TAXONOMIC_MATCH_SCIENTIFICNAME)
                            .queryParam("scientificName", scientificName).build())
                    .header(taxonRefKeyName, taxonRefKey)
                    .retrieve()
                    .onStatus(httpStatus -> ((httpStatus.is4xxClientError() && httpStatus != HttpStatus.NOT_FOUND) || httpStatus.is5xxServerError()), response -> {
                        log.error("Error TaxonRef API {}", response.statusCode());
                        throw new CollectionManagerBusinessException(response.statusCode()
                                .value(), TECH_TAX_REF_EXCEPTION, "Technical Exception of the TaxonRef API");
                    }).bodyToMono(TaxonRefOut.class)
                    .onErrorResume(WebClientResponseException.NotFound.class, notFound -> Mono.empty())
                    .block();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CollectionManagerBusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), TECH_TAX_REF_EXCEPTION, e.getMessage());
        }
        return taxonRefOut != null ? taxonRefOut.getTaxonList() : List.of();
    }

    @Override
    public List<String> suggestByScientifiName(String scientifiName) {
        var resp = TaxonRefSuggestionOut.builder().suggestions(List.of()).build();
        try {
            resp = getRequestHeadersUriSpec().uri(uriBuilder -> uriBuilder.path(API_V_3_REF_TAXONOMIC_SUGGESTION_SCIENTIFICNAME)
                            .queryParam("scientificName", scientifiName).build())
                    .header(taxonRefKeyName, taxonRefKey)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("Error TaxonRef API {}", response.statusCode());
                        throw new CollectionManagerBusinessException(TECH_TAX_REF_EXCEPTION, "Technical Exception : TaxonRef API call of suggestion");
                    }).bodyToMono(TaxonRefSuggestionOut.class)
                    .block();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CollectionManagerBusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), TECH_TAX_REF_EXCEPTION, e.getMessage());
        }
        return resp != null ? resp.getSuggestions() : List.of();

    }

    private WebClient.RequestHeadersUriSpec<?> getRequestHeadersUriSpec() {
        return WebClient.builder().baseUrl(taxonRefUrl)
                .clientConnector(
                        new ReactorClientHttpConnector(HttpClient
                                .create()
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3600)))
                .defaultHeaders(httpHeaders -> httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON)))
                .build().get();
    }


}
