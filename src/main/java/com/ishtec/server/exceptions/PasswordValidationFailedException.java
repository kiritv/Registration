package com.ishtec.server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class PasswordValidationFailedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

}
