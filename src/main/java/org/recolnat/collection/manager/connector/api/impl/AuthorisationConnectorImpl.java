package org.recolnat.collection.manager.connector.api.impl;

import io.netty.channel.ChannelOption;
import io.recolnat.model.UserDashboardPageResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.mapper.UserMapper;
import org.recolnat.collection.manager.connector.api.AuthorisationConnector;
import org.recolnat.collection.manager.connector.api.domain.UserOut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthorisationConnectorImpl implements AuthorisationConnector {

    public static final String AUTHORISATION_EXCEPTION = "TECH_AUTHORISATION_EXCEPTION";
    public static final String USERS = "/v1/users";

    private final UserMapper userMapper;

    @Value("${authorisation.base_url}")
    private String authorisationUrl;

    @Override
    public UserDashboardPageResponseDTO getUsers(UUID institutionId, Integer page, Integer size, String searchTerm) {
        var userefOut = UserOut.builder().users(List.of()).build();

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var credentials = (OAuth2AccessToken) authentication.getCredentials();

        try {
            userefOut = getRequestHeadersUriSpec().uri(uriBuilder -> uriBuilder.path(USERS)
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .queryParam("q", searchTerm)
                            .queryParam("institution_id", institutionId)
                            .build())
                    .header("Authorization", "Bearer " + credentials.getTokenValue())
                    .retrieve()
                    .onStatus(httpStatus -> ((httpStatus.is4xxClientError() && httpStatus != HttpStatus.NOT_FOUND) || httpStatus.is5xxServerError()), response -> {
                        log.error("Error Authorisation API {}", response.statusCode());
                        throw new CollectionManagerBusinessException(response.statusCode()
                                .value(), AUTHORISATION_EXCEPTION, "Technical Exception of the Authorisation API");
                    }).bodyToMono(UserOut.class)
                    .onErrorResume(WebClientResponseException.NotFound.class, notFound -> Mono.empty())
                    .block();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CollectionManagerBusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), AUTHORISATION_EXCEPTION, e.getMessage());
        }
        var dto = new UserDashboardPageResponseDTO();

        if (userefOut == null) {
            dto.setData(Collections.emptyList());
            dto.setTotalPages(0);
            dto.setNumberOfElements(0L);
        } else {
            var users = userefOut.getUsers();
            dto.setData(userMapper.toUsersDashboardDTO(users));
            dto.setTotalPages(userefOut.getTotalPages());
            dto.setNumberOfElements(userefOut.getNumberOfElements());
        }
        return dto;
    }

    private WebClient.RequestHeadersUriSpec<?> getRequestHeadersUriSpec() {
        return WebClient.builder().baseUrl(authorisationUrl)
                .clientConnector(
                        new ReactorClientHttpConnector(HttpClient
                                .create()
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3600)))
                .defaultHeaders(httpHeaders -> httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON)))
                .build().get();
    }


}
