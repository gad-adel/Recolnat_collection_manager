package org.recolnat.collection.manager.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MediathequeException extends RuntimeException {
	
	private static final long serialVersionUID = -224202170208628401L;
	private final HttpStatus httpStatus;
	private final String messageMediatheque;
	private final String detailMsg;

	public MediathequeException(final HttpStatus httpStatus, final String message, final String detailMsg) {
		this.httpStatus = httpStatus;
		this.messageMediatheque = message;
		this.detailMsg = detailMsg;
	}

}
