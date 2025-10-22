package org.recolnat.collection.manager.collection.api.web;


import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
public abstract class AbstractResourceElasticTest extends AbstractResourceDBTest {

    private static final String DOCKER_ELASTIC = "docker.elastic.co/elasticsearch/elasticsearch:7.17.10";
    private static final String XPACK_SECURITY_ENABLED = "xpack.security.enabled";
    private static final String XPACK_SECURITY_TRANSPORT_SSL_ENABLED = "xpack.security.transport.ssl.enabled";
    private static final String XPACK_SECURITY_HTTP_SSL_ENABLED = "xpack.security.http.ssl.enabled";

    public static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(DOCKER_ELASTIC);

    static {
        String regex = ".*(\"message\":\\s?\"started[\\s?|\"].*|] started\n$)";
        try {
            elasticsearchContainer.addEnv("connection-timeout", "100000");
            elasticsearchContainer.addEnv("socket-timeout", "10000");
            elasticsearchContainer.withSharedMemorySize(100_000_000_000L);
            elasticsearchContainer.withReuse(true);
            elasticsearchContainer.withStartupTimeout(Duration.of(50, ChronoUnit.SECONDS));
            elasticsearchContainer.setWaitStrategy(new LogMessageWaitStrategy().withRegEx(regex));
            elasticsearchContainer.addEnv(XPACK_SECURITY_ENABLED, Boolean.FALSE.toString());
            elasticsearchContainer.addEnv(XPACK_SECURITY_TRANSPORT_SSL_ENABLED, Boolean.FALSE.toString());
            elasticsearchContainer.addEnv(XPACK_SECURITY_HTTP_SSL_ENABLED, Boolean.FALSE.toString());
            elasticsearchContainer.start();
            log.info("Elasticsearch Testcontainer is started");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @DynamicPropertySource
    protected static void dynamicProperties(DynamicPropertyRegistry registry) {
        if (elasticsearchContainer.isCreated()) {
            registry.add("elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
        }
    }

}
