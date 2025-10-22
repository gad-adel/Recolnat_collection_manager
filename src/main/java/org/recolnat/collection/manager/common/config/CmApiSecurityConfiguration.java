package org.recolnat.collection.manager.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.api.domain.enums.RoleEnum;
import org.recolnat.collection.manager.common.exception.ErrorDetail;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity()
@EnableMethodSecurity()
@RequiredArgsConstructor
public class CmApiSecurityConfiguration {

    public static final String REALM_ACCESS = "realm_access";
    public static final String ROLES = "roles";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String[] CAN_IMPORT_ROLES = {RoleEnum.ADMIN.name()};
    private static final String API_URL_INSTITUTION = "/v1/institutions";
    private static final String API_URL_INSTITUTIONS = "/v1/institutions/**";
    private static final String API_URL_ARTICLES = "/v1/articles";
    private static final String API_URL_ARTICLE_REFRESH = "/v1/articles/refresh";
    private static final String API_URL_INSTITUTION_REFRESH = "/v1/institutions/refresh";
    private static final String API_URL_ARTICLES_ALL = "/v1/articles/**";
    private static final String API_URL_REFERENTIAL_ALL = "/v1/referential/**";
    private static final String API_URL_DATATION = "/v1/datations";
    private static final String API_URL_DOMAIN = "/v1/domains";
    private static final String API_URL_SPECIMENS = "/v1/specimens";
    private static final String API_URL_SPECIMEN = "/v1/specimens/*";
    private static final String API_URL_SPECIMENS_DRAFT = API_URL_SPECIMEN + "/draft";
    private static final String API_URL_SPECIMENS_POST_DRAFT = API_URL_SPECIMENS + "/draft";
    private static final String API_URL_SPECIMENS_PATCH = API_URL_SPECIMEN + "/medias";
    private static final String API_URL_SPECIMENS_PATCH_DRAFT = API_URL_SPECIMEN + "/medias/draft";
    private static final String API_URL_DUPLICATE_SPECIMENS = API_URL_SPECIMEN + "/duplicate/**";
    private static final String API_URL_SPECIMEN_REFRESH = API_URL_SPECIMENS + "/refresh";
    private static final String API_URL_COLLECTION_ALL = "/v1/collections/**";
    private static final String API_URL_COLLECTION_REFRESH = "/v1/collections/refresh";
    private static final String API_URL_BULK_VALIDATE = "/v1/collections/specimens/bulk-validate";
    private static final String API_URL_PUBLIC = "/v1/public/**";
    private final OAuth2ResourceServerProperties oAuth2ResourceServerProperties;
    private final ObjectMapper objectMapper;

    /**
     * all calls HttpMethod.GET go through authenticated() method
     * if you don't specify the exact path, the RequestMatcherDelegatingAuthorizationManager class will handle calls to the /** expression
     *
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(management -> management
                        .sessionCreationPolicy(STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(HttpMethod.POST, "v1/mids/**")
                        .hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name(),
                                RoleEnum.DATA_ENTRY.name())
                        .requestMatchers(HttpMethod.GET, "v1/doi")
                        .hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name(),
                                RoleEnum.DATA_ENTRY.name())
                        .requestMatchers(HttpMethod.POST, "v1/import/specimen/check")
                        .hasAnyRole(CAN_IMPORT_ROLES)
                        .requestMatchers(HttpMethod.POST, "v1/import/identification/check")
                        .hasAnyRole(CAN_IMPORT_ROLES)
                        .requestMatchers(HttpMethod.POST, "v1/import/publication/check")
                        .hasAnyRole(CAN_IMPORT_ROLES)
                        .requestMatchers(HttpMethod.POST, "v1/import/check")
                        .hasAnyRole(CAN_IMPORT_ROLES)
                        .requestMatchers(HttpMethod.POST, "v1/import/validate")
                        .hasAnyRole(CAN_IMPORT_ROLES)
                        .requestMatchers(HttpMethod.GET, "v1/import")
                        .hasAnyRole(CAN_IMPORT_ROLES)
                        .requestMatchers(HttpMethod.GET, "v1/import/file/*/download")
                        .hasAnyRole(CAN_IMPORT_ROLES)
                        .requestMatchers(HttpMethod.GET, API_URL_INSTITUTION)
                        .hasRole(RoleEnum.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, API_URL_INSTITUTION + "/options")
                        .hasRole(RoleEnum.ADMIN.name())
                        .requestMatchers(HttpMethod.POST, API_URL_INSTITUTION)
                        .hasRole(RoleEnum.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, API_URL_INSTITUTIONS)
                        .hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.ADMIN_INSTITUTION.name(), RoleEnum.ADMIN_COLLECTION.name())
                        .requestMatchers(HttpMethod.PATCH, API_URL_INSTITUTIONS)
                        .hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.ADMIN_INSTITUTION.name())
                        .requestMatchers(POST, API_URL_ARTICLES)
                        .hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.USER_INFRA.name())
                        .requestMatchers(HttpMethod.POST, API_URL_DUPLICATE_SPECIMENS)
                        .hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name(),
                                RoleEnum.DATA_ENTRY.name())
                        .requestMatchers(HttpMethod.POST, API_URL_SPECIMENS_POST_DRAFT)
                        .hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name(),
                                RoleEnum.DATA_ENTRY.name())
                        .requestMatchers(HttpMethod.GET, API_URL_SPECIMENS)
                        .hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name(),
                                RoleEnum.DATA_ENTRY.name())
                        .requestMatchers(HttpMethod.PUT, API_URL_SPECIMENS_DRAFT)
                        .hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name(),
                                RoleEnum.DATA_ENTRY.name())
                        .requestMatchers(HttpMethod.POST, "/v1/specimens/review")
                        .hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name(),
                                RoleEnum.DATA_ENTRY.name()
                        )
                        .requestMatchers(HttpMethod.PUT, "/v1/specimens/*/review")
                        .hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name(),
                                RoleEnum.DATA_ENTRY.name()
                        )
                        .requestMatchers(HttpMethod.POST, API_URL_SPECIMENS)
                        .hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name())
                        // Validation en masse de spécimen
                        .requestMatchers(HttpMethod.PATCH, API_URL_BULK_VALIDATE)
                        .hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.ADMIN_INSTITUTION.name())
                        .requestMatchers(HttpMethod.PUT, API_URL_SPECIMEN)
                        .hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.ADMIN_INSTITUTION.name(), RoleEnum.ADMIN_COLLECTION.name())
                        .requestMatchers(HttpMethod.PATCH, "/v1/specimens/*/medias/review")
                        .hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name(),
                                RoleEnum.DATA_ENTRY.name()
                        )
                        .requestMatchers(HttpMethod.PATCH, API_URL_SPECIMENS_PATCH_DRAFT)
                        .hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name(),
                                RoleEnum.DATA_ENTRY.name())
                        .requestMatchers(HttpMethod.PATCH, API_URL_SPECIMENS_PATCH)
                        .hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name())
                        // TODO à quoi sert ce endpoint (modification de masse des spécimens)
                        .requestMatchers(HttpMethod.PATCH, API_URL_SPECIMENS)
                        .hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.ADMIN_INSTITUTION.name())
                        // Suppression d'un spécimen
                        .requestMatchers(HttpMethod.DELETE, API_URL_SPECIMEN)
                        .hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.ADMIN_INSTITUTION.name(), RoleEnum.ADMIN_COLLECTION.name())
                        .requestMatchers(HttpMethod.POST, "/v1/collections")
                        .hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.ADMIN_INSTITUTION.name())
                        .requestMatchers(HttpMethod.DELETE, "/v1/collections/*")
                        .hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.ADMIN_INSTITUTION.name())
                        .requestMatchers(HttpMethod.GET, API_URL_SPECIMEN).authenticated()
                        .requestMatchers(HttpMethod.GET, API_URL_SPECIMEN + "/has-to-publish").authenticated()
                        // TODO à modifier ?
                        .requestMatchers(HttpMethod.GET, API_URL_COLLECTION_ALL).authenticated()
                        .requestMatchers(HttpMethod.PUT, API_URL_COLLECTION_ALL).hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name())
                        .requestMatchers(HttpMethod.PUT, API_URL_PUBLIC).permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui**", "/v3/api-docs/**", "/v3/api-docs**", "/actuator/**", API_URL_PUBLIC).permitAll()
                        .requestMatchers(API_URL_INSTITUTIONS, API_URL_ARTICLES_ALL, API_URL_REFERENTIAL_ALL, API_URL_DATATION, API_URL_DOMAIN).authenticated()
                        .requestMatchers(API_URL_SPECIMEN_REFRESH, API_URL_COLLECTION_REFRESH, API_URL_ARTICLE_REFRESH, API_URL_INSTITUTION_REFRESH)
                        .authenticated().requestMatchers("/v1/taxons/families/autocomplete").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/specimens/nominative-collection/autocomplete").hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name(),
                                RoleEnum.DATA_ENTRY.name())
                        .requestMatchers(HttpMethod.GET, "/v1/specimens/storage-name/autocomplete").hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name(),
                                RoleEnum.DATA_ENTRY.name())
                        .requestMatchers(HttpMethod.GET, "/v1/specimens/exists").hasAnyRole(
                                RoleEnum.ADMIN.name(),
                                RoleEnum.ADMIN_INSTITUTION.name(),
                                RoleEnum.ADMIN_COLLECTION.name(),
                                RoleEnum.DATA_ENTRY.name())
                )
                .oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(opaqueToken -> opaqueToken
                        .introspector(introspector())
                ))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }


    @Bean
    AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            final var errorDetail = ErrorDetail.builder()
                    .message("Access Denied : you don't have a role for the action")
                    .status(HttpStatus.FORBIDDEN.value())
                    .code("ACCESS_DENIED_CODE")
                    .developerMessage(AccessDeniedException.class.getCanonicalName()).build();

            OutputStream out = response.getOutputStream();
            objectMapper.writeValue(out, errorDetail);
            out.flush();
        };
    }

    /**
     * doc:<br>
     * setAllowedHeaders:<br>
     * -> you have to specify which parameters are allowed to be sent to the backend
     * services through the front-end app, for example, if you are using Bearer/Basic Token Authorization methods,
     * you need to pass your JWT-Token through the "Authorization" header.
     * So you need to make sure that backed would accept this data accordingly and for this purpose,
     * you must put "Authorization" in the list of Allowed-Headers.<br><br>
     * -> vous devez spécifier quels paramètres sont autorisés à être envoyés aux services backend
     * via l'application front-end, Par exemple, si vous utilisez les méthodes d'autorisation type Bearer ou Basic,
     * exemple dans le cas Bearere, vous devez transmettre votre jeton JWT via l'en-tête "Authorization".
     * Vous devez donc vous assurer que le header acceptera ces données en conséquence et à cette fin, vous devez mettre « Autorisation » dans la liste des en-têtes autorisés.<br><br>
     * <br>
     * setAllowedMethods:<br>
     * ->(https://developer.mozilla.org/en-US/docs/Glossary/Preflight_request)
     * Do not forget to put "OPTIONS" method in the list for Pre-flight process for Cors<br><br>
     * ->(https://developer.mozilla.org/en-US/docs/Glossary/Preflight_request)
     * N'oubliez pas de mettre la méthode "OPTIONS" dans la liste pour le processus http, pour les Cors.<br><br>
     * <p>
     * setAllowCredentials-> <br>If you are using Authorization header, set it True.( not necessary).
     * When allowCredentials is true, allowedOrigins cannot contain the special value \"*\"
     * <br><br>
     * setExposedHeaders-> <br>If you are returning data through Response Headers, you need to specify them here. for example, some APIs are designed to return Authorization token after success /authentication through Response Headers. Thus, the related header needs to be exposed accordingly.
     * <br><br>
     * setAllowedOrigins<br>
     * -> You must specify the domains that are eligible to send requests to your backend µ
     * applications. for example, if your application is hosted on https://penguin.com and your APIs are
     * on https://api.penguin.com, you need to allow "https://penguing.com" to send requests to your backend.
     * Also, you are able to pass wildcard (*) to allow any domains to send requests to your backend.
     * But it's recommended to not use "any" unless you are providing public APIs or you are deploying
     * in the non-production environments.
     * <br><br>
     * -> Vous devez spécifier les domaines éligibles pour envoyer des requêtes à vos
     * applications backend. par exemple, si votre application est hébergée sur https://penguin.com et que
     * vos API sont sur https://api.penguin.com, vous devez autoriser « https://penguing.com » à envoyer
     * des requêtes à votre backend . En outre, vous pouvez transmettre un caractère générique (*)
     * pour permettre à n'importe quel domaine d'envoyer des requêtes à votre backend.
     * Mais il est recommandé de ne pas utiliser « any », sauf si vous fournissez des API publiques
     * ou si vous déployez dans des environnements hors production.
     * <br><br>
     * <p>
     * <p>
     * //old version
     * var configuration = new CorsConfiguration();
     * configuration.setAllowedOrigins(Arrays.asList("*"));
     * configuration.setAllowedMethods(
     * Arrays.asList(
     * HttpMethod.GET.name(),
     * HttpMethod.POST.name(),
     * HttpMethod.PUT.name(),
     * HttpMethod.PATCH.name()));
     * configuration.setAllowCredentials(false);
     * configuration.setExposedHeaders(Arrays.asList("*"));
     * //the below three lines will add the relevant CORS response headers
     * configuration.addAllowedOrigin("*");
     * configuration.addAllowedHeader("*");
     * configuration.addAllowedMethod("*");
     * <p>
     * var source = new UrlBasedCorsConfigurationSource();
     * source.registerCorsConfiguration("/**", configuration);
     * return source;
     *
     * @return
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.setAllowCredentials(false);
        configuration.setExposedHeaders(Collections.singletonList(CorsConfiguration.ALL));
        configuration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.PATCH.name()));
        configuration.applyPermitDefaultValues();

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @SuppressWarnings("unchecked")
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        var authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS);
            if (Objects.nonNull(realmAccess)) {
                return ((List<String>) (realmAccess.get(ROLES))).
                        stream().map(role -> ROLE_PREFIX + role)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        });
        return authenticationConverter;
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            final var errorDetail = ErrorDetail.builder()
                    .message("Access Denied : you don't have a role for the action")
                    .status(HttpStatus.FORBIDDEN.value())
                    .code("ACCESS_DENIED_CODE")
                    .timestamp(LocalDateTime.now())
                    .detail(accessDeniedException.getMessage())
                    .developerMessage(AccessDeniedException.class.getCanonicalName()).build();

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            OutputStream out = response.getOutputStream();

            objectMapper.writeValue(out, errorDetail);

            out.flush();
        };
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            final var errorDetail = ErrorDetail.builder()
                    .message("you are not authenticated")
                    .detail(authException.getMessage())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .code("UNAUTHORIZED_ACCESS_CODE")
                    .developerMessage(AccessDeniedException.class.getCanonicalName())
                    .timestamp(LocalDateTime.now()).build();

            OutputStream out = response.getOutputStream();
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(out, errorDetail);
            out.flush();
        };

    }


    @Bean
    OpaqueTokenIntrospector introspector() {
        return new CustomAuthoritiesOpaqueTokenIntrospector(oAuth2ResourceServerProperties);
    }


}



