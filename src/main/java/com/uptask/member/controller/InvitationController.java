package com.uptask.member.controller;

import com.uptask.member.service.InvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
@Tag(name = "Invitaciones", description = "Aceptar o rechazar invitaciones a proyectos. El token se recibe por correo tras ser invitado por un Manager. " +
        "El correo del usuario autenticado debe coincidir con el correo al que se envió la invitación.")
@SecurityRequirement(name = "bearerAuth")
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping("/{token}/accept")
    @Operation(
        summary = "Aceptar invitación",
        description = "Acepta una invitación de proyecto usando el token recibido por correo. " +
                      "El usuario autenticado se añade al proyecto con el rol especificado en la invitación. " +
                      "El token es de un solo uso y expira a los 7 días. " +
                      "Devuelve 403 si el correo del usuario autenticado no coincide con el correo de la invitación."
    )
    @ApiResponse(responseCode = "200", description = "Invitación aceptada, usuario añadido al proyecto")
    @ApiResponse(responseCode = "403", description = "El correo del usuario autenticado no coincide con el de la invitación")
    @ApiResponse(responseCode = "404", description = "Token no encontrado")
    @ApiResponse(responseCode = "410", description = "La invitación ha expirado o ya fue aceptada o rechazada")
    public ResponseEntity<Void> accept(@PathVariable String token) {
        invitationService.accept(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{token}/decline")
    @Operation(
        summary = "Rechazar invitación",
        description = "Rechaza una invitación de proyecto. El token queda marcado como rechazado y no puede volver a usarse. " +
                      "Devuelve 403 si el correo del usuario autenticado no coincide con el correo de la invitación."
    )
    @ApiResponse(responseCode = "200", description = "Invitación rechazada")
    @ApiResponse(responseCode = "403", description = "El correo del usuario autenticado no coincide con el de la invitación")
    @ApiResponse(responseCode = "404", description = "Token no encontrado")
    @ApiResponse(responseCode = "410", description = "La invitación ha expirado o ya fue aceptada o rechazada")
    public ResponseEntity<Void> decline(@PathVariable String token) {
        invitationService.decline(token);
        return ResponseEntity.ok().build();
    }
}
