package com.uptask.member.exception;

import com.uptask.common.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends ApplicationException {
    public MemberNotFoundException() {
        super("Member not found in this project", HttpStatus.NOT_FOUND);
    }
}
