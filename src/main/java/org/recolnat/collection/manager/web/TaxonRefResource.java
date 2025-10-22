package org.recolnat.collection.manager.web;

import io.recolnat.api.RetrieveTaxonsRefsApi;
import io.recolnat.model.TaxonDTO;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.common.mapper.TaxonMapper;
import org.recolnat.collection.manager.connector.api.TaxonRefConnector;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
public class TaxonRefResource implements RetrieveTaxonsRefsApi {
    private final TaxonRefConnector taxonRefConnector;
    private final TaxonMapper taxonMapper;

    @Override
    public ResponseEntity<List<String>> getSuggestions(String scientificName) {
        try {
            var result = taxonRefConnector.suggestByScientifiName(scientificName);
            return new ResponseEntity<>(result, OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<TaxonDTO>> findTaxons(String scientificName) {
        try {
            var result = taxonRefConnector.findByScientifiName(scientificName).stream().map(taxonMapper::toDto).toList();
            return new ResponseEntity<>(result, OK);
        } catch (CollectionManagerBusinessException | MediathequeException e) {
            throw e;
        } catch (Exception e) {
            throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
        }
    }
}
