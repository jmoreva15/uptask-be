package com.uptask.email.dto;

import java.util.Map;

public record EmailDto(
        String to,
        String subject,
        String templateName,
        Map<String, Object> variables
) {}
