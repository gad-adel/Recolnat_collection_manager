package org.recolnat.collection.manager.web;

import io.recolnat.api.RetrieveDatationFieldsApi;
import io.recolnat.model.DatationResponseDTO;
import lombok.RequiredArgsConstructor;

import org.recolnat.collection.manager.common.exception.CollectionManagerBusinessException;
import org.recolnat.collection.manager.common.exception.ErrorCode;
import org.recolnat.collection.manager.common.exception.MediathequeException;
import org.recolnat.collection.manager.service.DatationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
public class DatationResource implements RetrieveDatationFieldsApi{

	private final DatationService datationService;
	
	@Override
	public ResponseEntity<DatationResponseDTO> getDatation(@Valid String eonothem, 
			@Valid String eratheme,
			@Valid String system,
			@Valid String epoch, 
			@Valid String age) {
		try {
			DatationResponseDTO retrieveAllDatation = datationService.retrieveAllDatation(eonothem, eratheme, system, epoch, age);
			return new ResponseEntity<>(retrieveAllDatation, HttpStatus.OK);
		}catch(CollectionManagerBusinessException  | MediathequeException e) {
			throw e;
		}catch( Exception e) {
			throw new CollectionManagerBusinessException(HttpStatus.CONFLICT, ErrorCode.ERROR_APPLICATIVE,e.getMessage());
		}
	}
}
