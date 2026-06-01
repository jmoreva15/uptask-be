package com.uptask.common.exception;

import org.springframework.http.HttpStatus;

public class PasswordMismatchException extends ApplicationException {

    public PasswordMismatchException() {
        super("Passwords do not match", HttpStatus.BAD_REQUEST);
    }
}
