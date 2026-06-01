package com.uptask.common.exception;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends ApplicationException {

    public AccountLockedException(String message) {
        super(message, HttpStatus.LOCKED);
    }
}
