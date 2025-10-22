package org.recolnat.collection.manager.common.exception;

import org.springframework.boot.ExitCodeGenerator;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class FailedToStartException extends RuntimeException implements ExitCodeGenerator {

	private static final long serialVersionUID = 3721493826084289962L;
	
	public FailedToStartException(String message) {
		super(message);
		log.error(message);
	}

	@Override
    public int getExitCode() {
        return -2;
    }
}


