package com.uptask.token.service;

import com.uptask.common.constants.AppConstants;
import com.uptask.common.exception.InvalidTokenException;
import com.uptask.common.util.HashUtil;
import com.uptask.common.util.OtpUtil;
import com.uptask.security.jwt.JwtProperties;
import com.uptask.token.entity.RefreshToken;
import com.uptask.token.repository.RefreshTokenRepository;
import com.uptask.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public String createRefreshToken(User user, HttpServletRequest request) {
        String rawToken = OtpUtil.generateSecureToken(AppConstants.REFRESH_TOKEN_BYTE_LENGTH);
        String tokenHash = HashUtil.sha256(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(LocalDateTime.now().plus(jwtProperties.refreshTokenExpiration()));
        refreshToken.setIpAddress(extractIp(request));
        refreshToken.setDeviceInfo(extractUserAgent(request));

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    public RefreshToken validateAndGet(String rawToken) {
        String tokenHash = HashUtil.sha256(rawToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (!token.isValid()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }
        return token;
    }

    public void revoke(String rawToken) {
        String tokenHash = HashUtil.sha256(rawToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(RefreshToken::revoke);
    }

    public void revokeAllForUser(User user) {
        refreshTokenRepository.revokeAllByUser(user);
    }

    private String extractIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return ua != null ? ua.substring(0, Math.min(ua.length(), 255)) : null;
    }
}
