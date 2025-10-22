package org.recolnat.collection.manager.common.exception;


import io.recolnat.model.ErrorDTO;
import io.recolnat.model.ErrorDTO.ErrorLevelEnum;
import io.recolnat.model.ErrorDTO.ErrorTypeEnum;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Locale;

import static java.util.Objects.nonNull;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    public RestExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(CollectionManagerBusinessException.class)
    public ResponseEntity<ErrorDetail> handleException(CollectionManagerBusinessException fle, HttpServletRequest request) {

        final var errorDetail = ErrorDetail.builder()
                .timestamp(LocalDateTime.now())
                .status(nonNull(fle.getStatus()) ? fle.getStatus() : BAD_REQUEST.value())
                .code(fle.getCode())
                .message(fle.getMessage())
                .detail(fle.getMessage())
                .developerMessage(fle.getClass().getName()).build();

        //mapper ErrorDTO
        return new ResponseEntity<>(errorDetail, null, HttpStatus.valueOf(errorDetail.getStatus()));
    }


    @ExceptionHandler(MediathequeException.class)
    public ResponseEntity<ErrorDetail> handlerMediathequeException(MediathequeException mediaException) {
        final var errorMediaDetail = ErrorDetail.builder()
                .timestamp(LocalDateTime.now())
                .status(mediaException.getHttpStatus().value())
                .code(mediaException.getHttpStatus().name())
                .developerMessage(mediaException.getDetailMsg())
                .detail(messageSource.getMessage(mediaException.getMessageMediatheque(), null, Locale.ENGLISH)).build();
        return new ResponseEntity<>(errorMediaDetail, mediaException.getHttpStatus());
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        final var errorDetail = ErrorDetail.builder()
                .timestamp(LocalDateTime.now())
                .code(((HttpStatus) status).name())
                .status(status.value())
                .detail(ex.getLocalizedMessage())
                .developerMessage(messageSource.getMessage("error.type.mismatch", null, Locale.ENGLISH)).build();
        return new ResponseEntity<>(errorDetail, status);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        final var errorDetail = ErrorDetail.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .code(((HttpStatus) status).name())
                .detail(ex.getLocalizedMessage())
                .developerMessage(messageSource.getMessage("error.type.notfound", null, Locale.ENGLISH)).build();
        return new ResponseEntity<>(errorDetail, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(BAD_REQUEST)
    ResponseEntity<ErrorDetail> onIllegalArgumentException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        final var errorDetail = ErrorDetail.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .code(status.name())
                .detail(ex.getLocalizedMessage()).build();
        return new ResponseEntity<>(errorDetail, status);
    }


    public ResponseEntity<ErrorDTO> handleError(HttpServletRequest request) {
        var httpStatus = getHttpStatus(request);
        String message = getErrorMessage(request, httpStatus);
        var errorDTO = new ErrorDTO()
                .errorCode(httpStatus.value())
                .errorMessage(message)
                .errorLevel(ErrorLevelEnum.ERROR)
                .errorType(ErrorTypeEnum.TECHNICAL);
        return new ResponseEntity<>(errorDTO, httpStatus);
    }

    private HttpStatus getHttpStatus(HttpServletRequest request) {
        Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            return HttpStatus.valueOf(status);
        }
        String code = request.getParameter("code");
        if (code != null && !code.isBlank()) {
            return HttpStatus.valueOf(code);
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String getErrorMessage(HttpServletRequest request, HttpStatus httpStatus) {
        String message = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (message != null && !message.isEmpty()) {
            return message;
        }
        message = switch (httpStatus) {
            case NOT_FOUND -> "The resource does not exist";
            case INTERNAL_SERVER_ERROR -> "Something went wrong internally";
            case FORBIDDEN -> "Permission denied";
            case TOO_MANY_REQUESTS -> "Too many requests";
            default -> httpStatus.getReasonPhrase();
        };
        return message;
    }
}
