package org.recolnat.collection.manager.web;

import io.recolnat.api.RetrievePlaceholderDatationApi;
import lombok.RequiredArgsConstructor;

import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.service.RetrievePlaceholderDatationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RetrievePlaceholderDatationResource implements RetrievePlaceholderDatationApi {

	private final RetrievePlaceholderDatationService retrievePlaceholderDatationService;
	
	@Override
	public ResponseEntity<List<String>> getAllPlaceholderDatation() {
		try {
			List<String> placeholderDatation = retrievePlaceholderDatationService.getPlaceholderDatation();
			return new ResponseEntity<>(placeholderDatation, HttpStatus.OK);
		}catch(CollectionManagerBusinessException  | MediathequeException e) {
			throw e;
		}catch( Exception e) {
			throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE,e.getMessage());
		}
	}

}
