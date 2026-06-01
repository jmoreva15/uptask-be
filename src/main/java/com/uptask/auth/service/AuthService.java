package com.uptask.auth.service;

import com.uptask.auth.dto.*;
import com.uptask.common.exception.*;
import com.uptask.config.SecurityProperties;
import com.uptask.email.service.EmailService;
import com.uptask.otp.entity.OtpType;
import com.uptask.otp.service.OtpService;
import com.uptask.role.entity.RoleName;
import com.uptask.role.service.RoleService;
import com.uptask.security.jwt.JwtService;
import com.uptask.security.principal.UserPrincipal;
import com.uptask.token.service.RefreshTokenService;
import com.uptask.user.entity.User;
import com.uptask.user.entity.UserStatus;
import com.uptask.user.mapper.UserMapper;
import com.uptask.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final OtpService otpService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final SecurityProperties securityProperties;

    public void register(RegisterDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setEmail(dto.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setPhone(dto.phone());
        user.setStatus(UserStatus.PENDING_ACTIVATION);
        user.setRoles(Set.of(roleService.findByName(RoleName.USER)));

        userRepository.save(user);

        String otp = otpService.generateAndSave(user, OtpType.ACCOUNT_ACTIVATION);
        emailService.sendActivationEmail(user.getEmail(), user.getFirstName(), otp);
        log.info("User registered: {}", user.getEmail());
    }

    public void activateAccount(ActivateAccountDto dto) {
        User user = userRepository.findByEmail(dto.email().toLowerCase())
                .orElseThrow(UserNotFoundException::new);

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new ApplicationException("Account is already active", HttpStatus.BAD_REQUEST);
        }

        otpService.validateAndConsume(user, dto.otp(), OtpType.ACCOUNT_ACTIVATION);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        log.info("Account activated: {}", user.getEmail());
    }

    public AuthTokenDto login(LoginDto dto, HttpServletRequest request) {
        User user = userRepository.findByEmail(dto.email().toLowerCase())
                .orElseThrow(() -> new ApplicationException("Invalid credentials", HttpStatus.UNAUTHORIZED));

        if (user.getStatus() == UserStatus.PENDING_ACTIVATION) throw new AccountNotActivatedException();
        if (user.getStatus() == UserStatus.DISABLED)
            throw new ApplicationException("Account is disabled", HttpStatus.FORBIDDEN);
        if (user.isAccountLocked())
            throw new AccountLockedException("Account is temporarily locked. Please try again later.");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email().toLowerCase(), dto.password())
            );
        } catch (BadCredentialsException e) {
            handleFailedLogin(user);
            throw new ApplicationException("Invalid credentials", HttpStatus.UNAUTHORIZED);
        } catch (DisabledException e) {
            throw new AccountNotActivatedException();
        } catch (LockedException e) {
            throw new AccountLockedException("Account is temporarily locked.");
        }

        handleSuccessfulLogin(user);

        String accessToken = jwtService.generateAccessToken(UserPrincipal.of(user));
        String refreshToken = refreshTokenService.createRefreshToken(user, request);
        return AuthTokenDto.of(accessToken, refreshToken, jwtService.getAccessTokenExpirationSeconds(),
                userMapper.toDto(user));
    }

    public AuthTokenDto refresh(RefreshTokenDto dto, HttpServletRequest request) {
        var existingToken = refreshTokenService.validateAndGet(dto.refreshToken());
        User user = existingToken.getUser();
        existingToken.revoke();

        String newAccessToken = jwtService.generateAccessToken(UserPrincipal.of(user));
        String newRefreshToken = refreshTokenService.createRefreshToken(user, request);
        return AuthTokenDto.of(newAccessToken, newRefreshToken, jwtService.getAccessTokenExpirationSeconds(),
                userMapper.toDto(user));
    }

    public void logout(RefreshTokenDto dto) {
        refreshTokenService.revoke(dto.refreshToken());
    }

    public void forgotPassword(ForgotPasswordDto dto) {
        userRepository.findByEmail(dto.email().toLowerCase()).ifPresent(user -> {
            if (user.isActive()) {
                String otp = otpService.generateAndSave(user, OtpType.PASSWORD_RESET);
                emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), otp);
            }
        });
    }

    public String verifyPasswordResetOtp(VerifyOtpDto dto) {
        User user = userRepository.findByEmail(dto.email().toLowerCase())
                .orElseThrow(() -> new InvalidOtpException("Invalid OTP"));
        otpService.validateAndConsume(user, dto.otp(), OtpType.PASSWORD_RESET);
        return jwtService.generatePasswordResetToken(user.getEmail());
    }

    public void resetPassword(ResetPasswordDto dto) {
        if (!dto.newPassword().equals(dto.confirmPassword())) throw new PasswordMismatchException();

        Jwt jwt = jwtService.validatePasswordResetToken(dto.resetToken());
        User user = userRepository.findByEmail(jwt.getSubject())
                .orElseThrow(UserNotFoundException::new);

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(user);
        log.info("Password reset for: {}", user.getEmail());
    }

    public void changePassword(ChangePasswordDto dto, Long userId) {
        if (!dto.newPassword().equals(dto.confirmPassword())) throw new PasswordMismatchException();

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new ApplicationException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
        refreshTokenService.revokeAllForUser(user);
    }

    public void resendActivationOtp(ResendOtpDto dto) {
        User user = userRepository.findByEmail(dto.email().toLowerCase())
                .orElseThrow(UserNotFoundException::new);

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new ApplicationException("Account is already active", HttpStatus.BAD_REQUEST);
        }

        String otp = otpService.generateAndSave(user, OtpType.ACCOUNT_ACTIVATION);
        emailService.sendActivationEmail(user.getEmail(), user.getFirstName(), otp);
    }

    private void handleFailedLogin(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() >= securityProperties.maxFailedLoginAttempts()) {
            user.setStatus(UserStatus.LOCKED);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(securityProperties.accountLockDurationMinutes()));
            log.warn("Account locked: {}", user.getEmail());
        }
        userRepository.save(user);
    }

    private void handleSuccessfulLogin(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        if (user.getStatus() == UserStatus.LOCKED) user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
}
