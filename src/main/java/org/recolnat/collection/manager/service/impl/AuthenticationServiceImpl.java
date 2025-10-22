package org.recolnat.collection.manager.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.api.domain.ConnectedUser;
import org.recolnat.collection.manager.api.domain.UserAttributes;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {


    @Override
    public UserAttributes findUserAttributes() {//@AuthenticationPrincipal OAuth2AccessToken principals
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        final var principal = authentication.getPrincipal();
        final var credentials = (OAuth2AccessToken) authentication.getCredentials();

        var auth = ((OAuth2IntrospectionAuthenticatedPrincipal) principal);
        List<UUID> uuidList;
        List<String> collectionIdList = auth.getClaimAsStringList(COLLECTIONS_KEY);
        try {
            uuidList = getCollectionCode(collectionIdList);
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.name(),
                    e.getMessage());
        }
        String role = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .filter(roleUser -> roleUser.startsWith(ROLE_PREFIX))
                .map(val -> StringUtils.removeStart(val, ROLE_PREFIX))
                .findFirst().orElse(null);
        return UserAttributes.builder()
                .ui(auth.getClaimAsString(USER_KEY))
                .institution(nonNull(auth.getClaimAsString(INSTITUTION_KEY)) ?
                        Integer.parseInt(auth.getClaimAsString(INSTITUTION_KEY)) : null)
                .role(role)
                .jwtUser(credentials.getTokenValue())
                .collections(uuidList).build();
    }

    private List<UUID> getCollectionCode(List<String> collectionIdList) {
        return Optional.ofNullable(collectionIdList).stream().flatMap(Collection::stream)
                .map(UUID::fromString)
                .toList();
    }

    @Override
    public ConnectedUser getConnected() {//voir @AuthenticationPrincipal OAuth2AccessToken principals
        final var auth = (OAuth2IntrospectionAuthenticatedPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final var userName = (String) auth.getAttribute("username");
        final var email = (String) auth.getAttribute("mail");
        return ConnectedUser.builder()
                .userId(UUID.fromString(auth.getClaimAsString(SUB)))
                .userName(userName)
                .email(email)
                .build();
    }
}
