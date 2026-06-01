package com.uptask.auth.dto;

import com.uptask.common.constants.SecurityConstants;
import com.uptask.user.dto.UserDto;

public record AuthTokenDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserDto user
) {
    public static AuthTokenDto of(String accessToken, String refreshToken, long expiresIn, UserDto user) {
        return new AuthTokenDto(accessToken, refreshToken, SecurityConstants.TOKEN_TYPE, expiresIn, user);
    }
}
