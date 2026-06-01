package com.uptask.security.jwt;

import com.uptask.common.constants.SecurityConstants;
import com.uptask.common.exception.InvalidTokenException;
import com.uptask.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;

    public String generateAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(principal.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.accessTokenExpiration()))
                .claim(SecurityConstants.CLAIM_USER_ID, principal.getId())
                .claim(SecurityConstants.CLAIM_ROLES, roles)
                .claim(SecurityConstants.CLAIM_TOKEN_TYPE, SecurityConstants.TOKEN_TYPE_ACCESS)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generatePasswordResetToken(String email) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(email)
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.passwordResetTokenExpiration()))
                .claim(SecurityConstants.CLAIM_TOKEN_TYPE, SecurityConstants.TOKEN_TYPE_PASSWORD_RESET)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Jwt validateToken(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (JwtException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            throw new InvalidTokenException(msg.toLowerCase().contains("expired") ? "Token has expired" : "Invalid token");
        }
    }

    public Jwt validatePasswordResetToken(String token) {
        Jwt jwt = validateToken(token);
        String tokenType = jwt.getClaimAsString(SecurityConstants.CLAIM_TOKEN_TYPE);
        if (!SecurityConstants.TOKEN_TYPE_PASSWORD_RESET.equals(tokenType)) {
            throw new InvalidTokenException("Invalid token type");
        }
        return jwt;
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.accessTokenExpiration().getSeconds();
    }
}
