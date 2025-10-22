package org.recolnat.collection.manager.common.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
@AllArgsConstructor
@Getter
public class KeycloakProperties {

    private final String authServerUrlOpenApi;
}
