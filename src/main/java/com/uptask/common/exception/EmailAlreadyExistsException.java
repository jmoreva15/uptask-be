package com.uptask.common.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends ApplicationException {

    public EmailAlreadyExistsException() {
        super("Email already registered", HttpStatus.CONFLICT);
    }
}
