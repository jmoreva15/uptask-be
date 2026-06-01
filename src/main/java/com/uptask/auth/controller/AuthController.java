package com.uptask.auth.controller;

import com.uptask.auth.dto.*;
import com.uptask.auth.service.AuthService;
import com.uptask.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro, inicio de sesión, rotación de tokens, restablecimiento de contraseña y activación de cuenta. " +
        "Todos los endpoints son públicos excepto cambiar-contraseña, que requiere Bearer token.")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
        summary = "Registrarse",
        description = "Crea una nueva cuenta de usuario en estado PENDING_ACTIVATION. " +
                      "Se envía un OTP de activación al correo indicado, que debe enviarse a POST /activate antes de poder usar la cuenta."
    )
    @ApiResponse(responseCode = "201", description = "Cuenta creada, OTP de activación enviado por correo")
    @ApiResponse(responseCode = "409", description = "El correo ya está registrado")
    @ApiResponse(responseCode = "422", description = "Error de validación en el cuerpo de la petición")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterDto dto) {
        authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registration successful. Please check your email for the activation code."));
    }

    @PostMapping("/activate")
    @Operation(
        summary = "Activar cuenta",
        description = "Valida el OTP enviado durante el registro y establece el estado de la cuenta como ACTIVE. " +
                      "El OTP expira tras el período configurado (10 minutos por defecto) y tiene un límite de 3 intentos."
    )
    @ApiResponse(responseCode = "200", description = "Cuenta activada correctamente")
    @ApiResponse(responseCode = "400", description = "OTP inválido, expirado o máximo de intentos superado")
    public ResponseEntity<Map<String, String>> activate(@Valid @RequestBody ActivateAccountDto dto) {
        authService.activateAccount(dto);
        return ResponseEntity.ok(Map.of("message", "Account activated successfully. You can now log in."));
    }

    @PostMapping("/resend-activation")
    @Operation(
        summary = "Reenviar OTP de activación",
        description = "Invalida cualquier OTP de activación existente y envía uno nuevo al correo registrado. " +
                      "Solo funciona para cuentas en estado PENDING_ACTIVATION."
    )
    @ApiResponse(responseCode = "200", description = "Nuevo OTP enviado")
    @ApiResponse(responseCode = "400", description = "La cuenta ya está activa o no existe")
    public ResponseEntity<Map<String, String>> resendActivation(@Valid @RequestBody ResendOtpDto dto) {
        authService.resendActivationOtp(dto);
        return ResponseEntity.ok(Map.of("message", "Activation code resent. Please check your email."));
    }

    @PostMapping("/login")
    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica al usuario y devuelve un access token de corta duración (JWT RS256, 15 min) y un refresh token de larga duración (7 días). " +
                      "Tras el número configurado de intentos fallidos la cuenta se bloquea temporalmente."
    )
    @ApiResponse(responseCode = "200", description = "Sesión iniciada correctamente, tokens devueltos")
    @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    @ApiResponse(responseCode = "403", description = "Cuenta no activada o bloqueada")
    public ResponseEntity<AuthTokenDto> login(@Valid @RequestBody LoginDto dto, HttpServletRequest request) {
        return ResponseEntity.ok(authService.login(dto, request));
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Rotar refresh token",
        description = "Intercambia un refresh token válido por un nuevo access token y un nuevo refresh token. " +
                      "El refresh token antiguo se revoca inmediatamente (rotación). Reutilizar un token revocado devuelve 401."
    )
    @ApiResponse(responseCode = "200", description = "Nuevo par de tokens devuelto")
    @ApiResponse(responseCode = "401", description = "Refresh token inválido, expirado o ya revocado")
    public ResponseEntity<AuthTokenDto> refresh(@Valid @RequestBody RefreshTokenDto dto, HttpServletRequest request) {
        return ResponseEntity.ok(authService.refresh(dto, request));
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Cerrar sesión",
        description = "Revoca el refresh token proporcionado. El access token sigue siendo válido hasta que expire naturalmente (15 min). " +
                      "Para revocación inmediata en todos los dispositivos, revoca todas las sesiones desde los endpoints de gestión de tokens."
    )
    @ApiResponse(responseCode = "200", description = "Refresh token revocado")
    @ApiResponse(responseCode = "401", description = "Refresh token no encontrado o ya revocado")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshTokenDto dto) {
        authService.logout(dto);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    @PostMapping("/forgot-password")
    @Operation(
        summary = "Solicitar restablecimiento de contraseña",
        description = "Envía un OTP de restablecimiento al correo dado si pertenece a una cuenta activa. " +
                      "Siempre devuelve 200 para evitar revelar si un correo está registrado."
    )
    @ApiResponse(responseCode = "200", description = "Respuesta enviada independientemente de si el correo existe")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordDto dto) {
        authService.forgotPassword(dto);
        return ResponseEntity.ok(Map.of("message", "If that email is registered, you will receive a reset code shortly."));
    }

    @PostMapping("/verify-password-reset-otp")
    @Operation(
        summary = "Verificar OTP de restablecimiento",
        description = "Valida el OTP de restablecimiento de contraseña y devuelve un token de uso único. " +
                      "Envía este token a POST /reset-password dentro del período de validez."
    )
    @ApiResponse(responseCode = "200", description = "OTP válido, reset token devuelto en el cuerpo como { resetToken: \"...\" }")
    @ApiResponse(responseCode = "400", description = "OTP inválido o expirado")
    public ResponseEntity<Map<String, String>> verifyPasswordResetOtp(@Valid @RequestBody VerifyOtpDto dto) {
        String resetToken = authService.verifyPasswordResetOtp(dto);
        return ResponseEntity.ok(Map.of("resetToken", resetToken));
    }

    @PostMapping("/reset-password")
    @Operation(
        summary = "Restablecer contraseña",
        description = "Establece una nueva contraseña usando el reset token obtenido de POST /verify-password-reset-otp. " +
                      "El token está firmado como JWT con tipo PASSWORD_RESET y debe enviarse en la cabecera Authorization como Bearer."
    )
    @ApiResponse(responseCode = "200", description = "Contraseña actualizada")
    @ApiResponse(responseCode = "401", description = "Reset token ausente, inválido o expirado")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordDto dto) {
        authService.resetPassword(dto);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully. Please log in with your new password."));
    }

    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Cambiar contraseña",
        description = "Cambia la contraseña del usuario autenticado actualmente. " +
                      "Es necesario proporcionar la contraseña actual como confirmación."
    )
    @ApiResponse(responseCode = "200", description = "Contraseña cambiada")
    @ApiResponse(responseCode = "400", description = "La contraseña actual es incorrecta")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        authService.changePassword(dto, principal.getId());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
    }
}
