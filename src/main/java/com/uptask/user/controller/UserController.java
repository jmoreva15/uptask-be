package com.uptask.user.controller;

import com.uptask.security.principal.UserPrincipal;
import com.uptask.user.dto.UserDto;
import com.uptask.user.mapper.UserMapper;
import com.uptask.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
@Tag(name = "Mi cuenta", description = "Endpoints del usuario autenticado. La identidad se resuelve desde el Bearer token, no se requiere userId en la petición.")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/profile")
    @Operation(
        summary = "Obtener mi perfil",
        description = "Devuelve el perfil completo del usuario autenticado incluyendo nombre, correo y estado de la cuenta."
    )
    @ApiResponse(responseCode = "200", description = "Perfil devuelto correctamente")
    @ApiResponse(responseCode = "401", description = "Bearer token ausente o inválido")
    public ResponseEntity<UserDto> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userMapper.toDto(userService.findById(principal.getId())));
    }
}
