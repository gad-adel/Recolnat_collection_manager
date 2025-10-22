package org.recolnat.collection.manager.common.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebClientConfiguration {

	@Value("${mediatheque.stream.base_url}")
	private String baseUrl;

	@Value("${mediatheque.header.key}")
	private String keyClient;

	@Value("${mediatheque.header.value}")
    private String keyValue;

	@Value("${mediatheque.connect.timeout:1000}")
	private Integer timeOut;

	@Bean
	@Primary
	 WebClient mediathequeWebClient() {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(keyClient, keyValue);
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		return WebClient.builder().baseUrl(baseUrl)
				.clientConnector(
						new ReactorClientHttpConnector(HttpClient
						.create()
						.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeOut)))
				.defaultHeaders(head -> head.addAll(headers))
				.build();
	}

	
}
