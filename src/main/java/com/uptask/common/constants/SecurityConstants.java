package com.uptask.common.constants;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TOKEN_TYPE = "Bearer";

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";

    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_PASSWORD_RESET = "password_reset";
}
