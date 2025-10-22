package org.recolnat.collection.manager.connector.api.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.recolnat.collection.manager.connector.api.TaxonRefConnector;
import org.recolnat.collection.manager.connector.api.domain.TaxonRef;
import org.recolnat.collection.manager.connector.api.domain.TaxonRefOut;
import org.recolnat.collection.manager.connector.api.domain.TaxonRefSuggestionOut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "taxonRef.api.mock", havingValue = "true")
public class TaxonRefConnectorMockImpl implements TaxonRefConnector {

    @Value(value = "classpath:json/taxref.json")
    private Resource taxref;

    @Value(value = "classpath:json/sn.json")
    private Resource sn;

    private final ObjectMapper objectMapper;

    @Override
    public List<TaxonRef> findByScientifiName(String scientifiName) {
         TaxonRefOut taxonRef= TaxonRefOut.builder().taxonList(List.of()).build();
        if (StringUtils.length(scientifiName)>=3){
        try {
            taxonRef=  objectMapper.readValue(taxref.getInputStream(), TaxonRefOut.class);
        } catch (IOException e) {
             log.error( e.getMessage(),e);
        }
        }
        return taxonRef.getTaxonList().stream()
                .map(ref-> {
             ref.setScientificName(scientifiName);
             return ref;
                }).toList();
    }

    @Override
    public List<String> suggestByScientifiName(String scientifiName) {
        var suggestionOut = TaxonRefSuggestionOut.builder().suggestions(List.of()).build();
        if (StringUtils.length(scientifiName)>=3){
        try {
            suggestionOut =  objectMapper.readValue(sn.getInputStream(), TaxonRefSuggestionOut.class);
        } catch (IOException e) {
            log.error( e.getMessage(),e);
        }
        }
        return suggestionOut.getSuggestions().stream().map(s -> s.replace("Canis", scientifiName)).toList();

    }
}
