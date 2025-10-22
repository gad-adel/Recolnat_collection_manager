package org.recolnat.collection.manager.connector.taxonref;

//@Slf4j
//@Disabled("en attente d implementation de spring-contract test")
//class TaxonRefApiTest {
//    @Test
//    void findByScientifiName() throws IOException {
//        String sn ="Canis Linnaeus, 1758";
//        var result =  WebClient.builder().baseUrl("http://dock-pas1.arzt.mnhn.fr:8098")
//                .clientConnector(
//                        new ReactorClientHttpConnector(HttpClient
//                                .create()
//                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3600)))
//                .defaultHeaders(httpHeaders -> httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON)))
//                .build().get() .uri(uriBuilder -> uriBuilder.path("/api/v3/ref/taxonomic/match/scientificname")
//                        .queryParam("scientificName", sn).build())
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, response -> {
//                    log.error("Error TaxonRef API {}",  response.statusCode());
//                    throw new CollectionManagerBusinessException("TAX_REF_EXCEPTION", "We can't get resources on TaxonRef API");
//                }).bodyToFlux(TaxonRef.class)
//                .collectList().block();
//        assertThat(result).hasSize(1);
//
//    }
//
//    @Test
//    void findByScientifiName_suggestion() throws IOException {
//        String sn ="Canis";
//        var result =  WebClient.builder().baseUrl("http://dock-pas1.arzt.mnhn.fr:8098")
//                .clientConnector(
//                        new ReactorClientHttpConnector(HttpClient
//                                .create()
//                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3600)))
//                .defaultHeaders(httpHeaders -> httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON)))
//                .build().get() .uri(uriBuilder -> uriBuilder.path("/api/v3/ref/taxonomic/suggestion/scientificname")
//                        .queryParam("scientificName", sn).build())
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, response -> {
//                    log.error("Error TaxonRef API {}",  response.statusCode());
//                    throw new CollectionManagerBusinessException("TAX_REF_EXCEPTION", "We can't get resources on TaxonRef API");
//                }).bodyToMono(TaxonRefSuggestionOut.class)
//                .block();
//
//        log.info("Result : {}", result);
//        assertThat(result.getSuggestions()).isNotEmpty();
//        assertThat(result.getSuggestions()).hasSizeGreaterThanOrEqualTo(20);
//    }
//
//
//}
