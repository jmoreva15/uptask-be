package com.uptask.member.exception;

import com.uptask.common.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class InvitationNotFoundException extends ApplicationException {
    public InvitationNotFoundException() {
        super("Invitation not found or has expired", HttpStatus.NOT_FOUND);
    }
}
