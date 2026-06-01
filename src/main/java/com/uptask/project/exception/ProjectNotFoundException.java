package com.uptask.project.exception;

import com.uptask.common.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class ProjectNotFoundException extends ApplicationException {
    public ProjectNotFoundException(Long id) {
        super("Project not found: " + id, HttpStatus.NOT_FOUND);
    }
}
