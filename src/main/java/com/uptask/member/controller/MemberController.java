package com.uptask.member.controller;

import com.uptask.member.dto.AssignRoleDto;
import com.uptask.member.dto.InviteMemberDto;
import com.uptask.member.dto.MemberDto;
import com.uptask.member.service.InvitationService;
import com.uptask.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
@RequiredArgsConstructor
@Tag(name = "Miembros", description = "Gestión de miembros del proyecto. Los miembros se incorporan mediante invitación por correo. " +
        "Cada miembro tiene asignado exactamente un rol de proyecto que determina sus permisos.")
@SecurityRequirement(name = "bearerAuth")
public class MemberController {

    private final MemberService memberService;
    private final InvitationService invitationService;

    @GetMapping("/members")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'MEMBER_VIEW')")
    @Operation(
        summary = "Listar miembros",
        description = "Devuelve todos los miembros activos del proyecto con su rol asignado. Requiere permiso MEMBER_VIEW."
    )
    @ApiResponse(responseCode = "200", description = "Lista de miembros devuelta")
    @ApiResponse(responseCode = "403", description = "Sin permiso MEMBER_VIEW")
    public ResponseEntity<List<MemberDto>> getMembers(@PathVariable Long projectId) {
        return ResponseEntity.ok(memberService.getMembers(projectId));
    }

    @PostMapping("/invitations")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'MEMBER_INVITE')")
    @Operation(
        summary = "Invitar miembro",
        description = "Envía una invitación por correo con un token seguro válido durante 7 días. " +
                      "El usuario invitado debe registrarse (o iniciar sesión) con ese correo exacto y luego llamar a " +
                      "POST /api/v1/invitations/{token}/accept para unirse al proyecto con el rol especificado. " +
                      "Solo se permite una invitación pendiente por correo por proyecto. Requiere permiso MEMBER_INVITE."
    )
    @ApiResponse(responseCode = "202", description = "Correo de invitación enviado")
    @ApiResponse(responseCode = "403", description = "Sin permiso MEMBER_INVITE")
    @ApiResponse(responseCode = "404", description = "El roleId indicado no pertenece a este proyecto")
    @ApiResponse(responseCode = "409", description = "El usuario ya es miembro o ya existe una invitación pendiente para ese correo")
    @ApiResponse(responseCode = "422", description = "Error de validación en el cuerpo de la petición")
    public ResponseEntity<Void> invite(
            @PathVariable Long projectId,
            @Valid @RequestBody InviteMemberDto dto) {
        invitationService.invite(projectId, dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PatchMapping("/members/{userId}/role")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'MEMBER_ROLE_ASSIGN')")
    @Operation(
        summary = "Cambiar rol de miembro",
        description = "Asigna un rol diferente a un miembro existente. El nuevo rol debe pertenecer a este proyecto. " +
                      "No se puede cambiar el rol del propietario del proyecto. Requiere permiso MEMBER_ROLE_ASSIGN."
    )
    @ApiResponse(responseCode = "200", description = "Rol actualizado")
    @ApiResponse(responseCode = "403", description = "Sin permiso MEMBER_ROLE_ASSIGN o intento de cambiar el rol del propietario")
    @ApiResponse(responseCode = "404", description = "Miembro o rol no encontrado en este proyecto")
    public ResponseEntity<MemberDto> assignRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Valid @RequestBody AssignRoleDto dto) {
        return ResponseEntity.ok(memberService.assignRole(projectId, userId, dto));
    }

    @DeleteMapping("/members/{userId}")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'MEMBER_REMOVE')")
    @Operation(
        summary = "Eliminar miembro",
        description = "Elimina un miembro del proyecto. El propietario no puede ser eliminado. " +
                      "El usuario eliminado pierde acceso inmediato a todos los recursos del proyecto. Requiere permiso MEMBER_REMOVE."
    )
    @ApiResponse(responseCode = "204", description = "Miembro eliminado")
    @ApiResponse(responseCode = "403", description = "Sin permiso MEMBER_REMOVE o intento de eliminar al propietario")
    @ApiResponse(responseCode = "404", description = "El usuario no es miembro de este proyecto")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        memberService.removeMember(projectId, userId);
        return ResponseEntity.noContent().build();
    }
}
