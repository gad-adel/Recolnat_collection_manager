package org.recolnat.collection.manager.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.service.RetrievePlaceholderDatationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RetrievePlaceholderDatationServiceImpl implements RetrievePlaceholderDatationService {

    private final ObjectMapper mapper;

    @Override
    public List<String> getPlaceholderDatation() {
        TypeReference<List<String>> typeReference = new TypeReference<>() {
        };
        InputStream resourceAsStream = TypeReference.class.getResourceAsStream("/json/placeholder-datation.json");
        List<String> placeholderDatation;
        try {
            placeholderDatation = mapper.readValue(resourceAsStream, typeReference);
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value()
                    , HttpStatus.INTERNAL_SERVER_ERROR.name(), "getPlaceholderDatation {} " + e.getMessage());
        }
        return placeholderDatation;
    }

}
