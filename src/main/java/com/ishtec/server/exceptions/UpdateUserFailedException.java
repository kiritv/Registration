package com.ishtec.server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class UpdateUserFailedException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5L;

	public UpdateUserFailedException(String message) {
        super(message);
    }
}
