package com.uptask.projectrole.controller;

import com.uptask.projectrole.dto.CreateProjectRoleDto;
import com.uptask.projectrole.dto.ProjectRoleDto;
import com.uptask.projectrole.dto.UpdateProjectRoleDto;
import com.uptask.projectrole.service.ProjectRoleService;
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
@RequestMapping("/api/v1/projects/{projectId}/roles")
@RequiredArgsConstructor
@Tag(name = "Roles de proyecto", description = "Gestión de roles por proyecto. Los roles son exclusivos de cada proyecto y no se comparten globalmente. " +
        "Cada rol contiene un conjunto de permisos granulares. El rol MANAGER es un rol de sistema creado automáticamente al crear el proyecto y no puede modificarse ni eliminarse.")
@SecurityRequirement(name = "bearerAuth")
public class ProjectRoleController {

    private final ProjectRoleService roleService;

    @GetMapping
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    @Operation(
        summary = "Listar roles",
        description = "Devuelve todos los roles del proyecto, incluyendo el rol de sistema MANAGER y cualquier rol personalizado. " +
                      "Cada rol incluye su conjunto completo de permisos. Requiere ser miembro del proyecto."
    )
    @ApiResponse(responseCode = "200", description = "Lista de roles devuelta")
    @ApiResponse(responseCode = "403", description = "El usuario no es miembro del proyecto")
    public ResponseEntity<List<ProjectRoleDto>> getRoles(@PathVariable Long projectId) {
        return ResponseEntity.ok(roleService.getRoles(projectId));
    }

    @PostMapping
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'ROLE_CREATE')")
    @Operation(
        summary = "Crear rol",
        description = "Crea un rol personalizado en el proyecto con el conjunto de permisos indicado. " +
                      "Proporciona los permissionIds a asignar — usa GET /roles para ver los IDs de permisos disponibles. " +
                      "El nombre del rol debe ser único dentro del proyecto. Requiere permiso ROLE_CREATE."
    )
    @ApiResponse(responseCode = "201", description = "Rol creado")
    @ApiResponse(responseCode = "400", description = "Uno o más permissionIds no son válidos")
    @ApiResponse(responseCode = "403", description = "Sin permiso ROLE_CREATE")
    @ApiResponse(responseCode = "409", description = "Ya existe un rol con ese nombre en el proyecto")
    @ApiResponse(responseCode = "422", description = "Error de validación en el cuerpo de la petición")
    public ResponseEntity<ProjectRoleDto> createRole(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateProjectRoleDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(projectId, dto));
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'ROLE_EDIT')")
    @Operation(
        summary = "Actualizar rol",
        description = "Actualiza el nombre, descripción y conjunto de permisos de un rol personalizado. " +
                      "Los permissionIds proporcionados reemplazan el conjunto actual completamente — envía todos los permisos deseados, no solo los nuevos. " +
                      "Los roles de sistema (MANAGER) no pueden modificarse. Requiere permiso ROLE_EDIT."
    )
    @ApiResponse(responseCode = "200", description = "Rol actualizado")
    @ApiResponse(responseCode = "400", description = "Uno o más permissionIds no son válidos")
    @ApiResponse(responseCode = "403", description = "Sin permiso ROLE_EDIT o intento de modificar un rol de sistema")
    @ApiResponse(responseCode = "404", description = "Rol no encontrado en este proyecto")
    @ApiResponse(responseCode = "409", description = "Ya existe un rol con ese nombre en el proyecto")
    public ResponseEntity<ProjectRoleDto> updateRole(
            @PathVariable Long projectId,
            @PathVariable Long roleId,
            @Valid @RequestBody UpdateProjectRoleDto dto) {
        return ResponseEntity.ok(roleService.updateRole(projectId, roleId, dto));
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'ROLE_DELETE')")
    @Operation(
        summary = "Eliminar rol",
        description = "Elimina un rol personalizado. Los roles de sistema (MANAGER) no pueden eliminarse. " +
                      "Un rol asignado a uno o más miembros no puede eliminarse — reasigna primero a esos miembros. " +
                      "Requiere permiso ROLE_DELETE."
    )
    @ApiResponse(responseCode = "204", description = "Rol eliminado")
    @ApiResponse(responseCode = "403", description = "Sin permiso ROLE_DELETE o intento de eliminar un rol de sistema")
    @ApiResponse(responseCode = "404", description = "Rol no encontrado en este proyecto")
    @ApiResponse(responseCode = "409", description = "El rol está asignado a uno o más miembros")
    public ResponseEntity<Void> deleteRole(
            @PathVariable Long projectId,
            @PathVariable Long roleId) {
        roleService.deleteRole(projectId, roleId);
        return ResponseEntity.noContent().build();
    }
}
