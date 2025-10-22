package org.recolnat.collection.manager.web;

import io.recolnat.api.RetrieveTaxonsResourceApi;
import lombok.RequiredArgsConstructor;
import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.service.TaxonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
public class TaxonResource implements RetrieveTaxonsResourceApi {
	private final TaxonService taxonService;

	@Override
	public ResponseEntity<List<String>> getTaxonFamilies(String q, Integer size) {
		try {
			var result = taxonService.getFamilyListByPrefix(q, size);
			return new ResponseEntity<>(result, OK);
		} catch (Exception e) {
			throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
		}
	}

	@Override
	public ResponseEntity<List<String>> getTaxonGenera(String q, String family, Integer size) {
		try {
			var result = taxonService.getGenusListByPrefixAndFamily(q, family, size);
			return new ResponseEntity<>(result, OK);
		} catch (Exception e) {
			throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
		}
	}

	@Override
	public ResponseEntity<List<String>> getTaxonSpecificEpithet(String q, String genus, Integer size) {
		try {
			var result = taxonService.getSpecificEpithetByPrefix(q, genus, size);
			return new ResponseEntity<>(result, OK);
		} catch (Exception e) {
			throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE, e.getMessage());
		}
	}
}
