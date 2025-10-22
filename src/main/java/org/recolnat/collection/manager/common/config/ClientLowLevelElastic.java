package org.recolnat.collection.manager.common.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Objects;


@Configuration
@Slf4j
public class ClientLowLevelElastic {

    @Value("${elasticsearch.uris}")
    String uriClientElasticSearch;

    @Value("${elasticsearch.username:}")
    String loginClientElasticSearch;

    @Value("${elasticsearch.password:}")
    String passwordClientElasticSearch;

    @Value("${elasticsearch.connection-timeout}")
    Integer connectionTimeouClientElasticSearch;

    @Value("${elasticsearch.socket-timeout}")
    Integer socketTimeoutClientElasticSearch;

    @Value("${elasticsearch.https-ignore_certif}")
    boolean ignoreHttps;

    @Value("${elasticsearch.https-certificate}")
    boolean certificateHttps;

    @Value("${elasticsearch.https-truststore}")
    boolean defaultHttps;

    /**
     * mecaniquement on ne traite , le cadre HTTPS, que lorsqu'un CredentialsProvider est fourni (loggn /pwd)
     *
     * @return
     */
    @Bean
    RestClient getRestClient() {

        RestClientBuilder restClientBuilder;
        if (Objects.nonNull(loginClientElasticSearch) && !loginClientElasticSearch.isBlank()) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(loginClientElasticSearch, passwordClientElasticSearch));

            restClientBuilder = RestClient.builder(HttpHost.create(uriClientElasticSearch));

            try {
                if (ignoreHttps) {
                    final SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (x509Certificates, s) -> true).build();
                    restClientBuilder
                            .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setSSLContext(sslContext)
                                    .setDefaultCredentialsProvider(credentialsProvider)
                                    .setSSLHostnameVerifier((hostname, session) -> true));
                } else if (certificateHttps) {

                    var factory = CertificateFactory.getInstance("X.509");
                    var trustStore = KeyStore.getInstance("pkcs12");
                    try (InputStream is = getClass().getClassLoader().getResourceAsStream("elasticsearch-ca.pem")) {
                        Certificate trustedCa = factory.generateCertificate(is);
                        trustStore.load(null, null);
                        trustStore.setCertificateEntry("ca", trustedCa);
                    }
                    final SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(trustStore, null).build();
                    restClientBuilder
                            .setHttpClientConfigCallback(httpClientBuilder ->
                                    httpClientBuilder
                                            .setDefaultCredentialsProvider(credentialsProvider)
                                            .setSSLContext(sslContext)
                            );
                } else if (defaultHttps) {
                    restClientBuilder
                            .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                                    .setDefaultCredentialsProvider(credentialsProvider)
                                    .setSSLContext(SSLContexts.createDefault()));
                } else {
                    restClientBuilder
                            .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                                    .setDefaultCredentialsProvider(credentialsProvider));
                }
            } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | CertificateException | IOException e) {
                throw new CollectionManagerBusinessException(HttpStatus.BAD_REQUEST, ErrorCode.ERR_CODE_CM, e.getMessage());
            }
        } else {
            restClientBuilder = RestClient.builder(new HttpHost(uriClientElasticSearch));
        }

        restClientBuilder.setFailureListener(new RestClient.FailureListener() {
            @Override
            public void onFailure(Node node) {
                log.error("Error on Elastic Node {}", node.toString());
            }
        });
        restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setSocketTimeout(socketTimeoutClientElasticSearch)
                .setConnectTimeout(connectionTimeouClientElasticSearch));
        return restClientBuilder.build();
    }

    @Bean
    ElasticsearchTransport getElasticsearchTransport() {
        return new RestClientTransport(
                getRestClient(), new JacksonJsonpMapper());
    }


    @Bean("elasticsearchClient")
    ElasticsearchClient getElasticsearchClient() {
        return new ElasticsearchClient(getElasticsearchTransport());
    }

    @PreDestroy
    public void destroy() {
        if (log.isInfoEnabled()) {
            log.info("Callback triggered - @PreDestroy : to close client Elastic .");
        }

        try {
            getRestClient().close();
        } catch (IOException e) {
            log.error("Elastic client improperly closed {}", e.getMessage());
        }
    }
}
