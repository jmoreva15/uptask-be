package com.uptask.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public static ErrorDto of(int status, String error, String message, String path) {
        return new ErrorDto(Instant.now(), status, error, message, path, null);
    }

    public static ErrorDto withFieldErrors(int status, String error, String message, String path,
                                           Map<String, String> fieldErrors) {
        return new ErrorDto(Instant.now(), status, error, message, path, fieldErrors);
    }
}
