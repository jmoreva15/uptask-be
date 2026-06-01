package com.uptask.task.exception;

import com.uptask.common.exception.ApplicationException;
import org.springframework.http.HttpStatus;

public class TaskNotFoundException extends ApplicationException {
    public TaskNotFoundException(Long id) {
        super("Task not found: " + id, HttpStatus.NOT_FOUND);
    }
}
