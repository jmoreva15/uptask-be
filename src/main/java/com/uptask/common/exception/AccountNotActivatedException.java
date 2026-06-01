package com.uptask.common.exception;

import org.springframework.http.HttpStatus;

public class AccountNotActivatedException extends ApplicationException {

    public AccountNotActivatedException() {
        super("Account is not activated. Please check your email for the activation code.", HttpStatus.FORBIDDEN);
    }
}
