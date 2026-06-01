package com.uptask.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends ApplicationException {

    public InvalidTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
