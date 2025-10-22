package org.recolnat.collection.manager.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CollectionManagerBusinessException extends RuntimeException {

    private static final long serialVersionUID = 7117159728483867507L;
    private final String code;
    private final Integer status;
    private final HttpStatus httpStatus;

    public CollectionManagerBusinessException(final String code, final String message) {
        super(message);
        this.code = code;
        this.status = null;
        httpStatus = null;
    }

    public CollectionManagerBusinessException(final int status, final String code, final String message) {
        super(message);
        this.code = code;
        this.status = status;
        httpStatus = null;
    }

    public CollectionManagerBusinessException(final HttpStatus httpStatus, final String code, final String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.status = null;
    }

    public CollectionManagerBusinessException(final HttpStatus httpStatus, final String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = null;
        this.status = null;
    }
}
