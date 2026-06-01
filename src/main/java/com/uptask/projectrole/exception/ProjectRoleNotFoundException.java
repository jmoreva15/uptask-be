package com.uptask.projectrole.exception;

import com.uptask.common.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class ProjectRoleNotFoundException extends ApplicationException {
    public ProjectRoleNotFoundException(Long id) {
        super("Project role not found: " + id, HttpStatus.NOT_FOUND);
    }
}
