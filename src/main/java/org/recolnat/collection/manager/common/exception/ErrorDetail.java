package org.recolnat.collection.manager.common.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Builder
@Data	
public class ErrorDetail {
    private String code;
    private String message;
    private int status;
    private String detail;
    private LocalDateTime timestamp;
    private String developerMessage;
    
}
