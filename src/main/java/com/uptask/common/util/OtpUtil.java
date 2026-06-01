package com.uptask.common.util;

import com.uptask.common.constants.AppConstants;

import java.security.SecureRandom;
import java.util.Base64;

public final class OtpUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private OtpUtil() {}

    public static String generateNumericOtp() {
        int bound = (int) Math.pow(10, AppConstants.OTP_LENGTH);
        int otp = RANDOM.nextInt(bound);
        return String.format("%0" + AppConstants.OTP_LENGTH + "d", otp);
    }

    public static String generateSecureToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
