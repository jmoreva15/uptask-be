package com.uptask.otp.service;

import com.uptask.common.exception.InvalidOtpException;
import com.uptask.common.util.HashUtil;
import com.uptask.common.util.OtpUtil;
import com.uptask.config.SecurityProperties;
import com.uptask.otp.entity.OtpCode;
import com.uptask.otp.entity.OtpType;
import com.uptask.otp.repository.OtpCodeRepository;
import com.uptask.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final SecurityProperties securityProperties;

    public String generateAndSave(User user, OtpType type) {
        otpCodeRepository.invalidateAllByUserAndType(user, type);

        String rawOtp = OtpUtil.generateNumericOtp();
        String codeHash = HashUtil.sha256(rawOtp);

        OtpCode otp = new OtpCode();
        otp.setUser(user);
        otp.setCodeHash(codeHash);
        otp.setType(type);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(securityProperties.otpExpirationMinutes()));

        otpCodeRepository.save(otp);
        return rawOtp;
    }

    public OtpCode validateAndConsume(User user, String rawOtp, OtpType type) {
        OtpCode otpCode = findActiveOtp(user, type);
        verifyOtp(rawOtp, otpCode);
        otpCode.markAsUsed();
        return otpCodeRepository.save(otpCode);
    }

    private OtpCode findActiveOtp(User user, OtpType type) {
        OtpCode otpCode = otpCodeRepository
                .findTopByUserAndTypeAndUsedFalseOrderByCreatedAtDesc(user, type)
                .orElseThrow(() -> new InvalidOtpException("No active OTP found"));

        if (otpCode.isExpired()) {
            throw new InvalidOtpException("OTP has expired");
        }
        if (otpCode.getAttempts() >= securityProperties.otpMaxAttempts()) {
            throw new InvalidOtpException("Too many failed attempts. Please request a new OTP");
        }
        return otpCode;
    }

    private void verifyOtp(String rawOtp, OtpCode otpCode) {
        if (!HashUtil.matches(rawOtp, otpCode.getCodeHash())) {
            otpCode.incrementAttempts();
            otpCodeRepository.save(otpCode);
            int remaining = securityProperties.otpMaxAttempts() - otpCode.getAttempts();
            throw new InvalidOtpException("Invalid OTP. " + remaining + " attempt(s) remaining");
        }
    }
}
