package com.uptask.common.exception;

import org.springframework.http.HttpStatus;

public class InvalidOtpException extends ApplicationException {

    public InvalidOtpException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
