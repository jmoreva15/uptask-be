package com.uptask.project.controller;

import com.uptask.project.dto.CreateProjectDto;
import com.uptask.project.dto.ProjectDto;
import com.uptask.project.dto.UpdateProjectDto;
import com.uptask.project.service.ProjectService;
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
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Proyectos", description = "Gestión de proyectos. Al crear un proyecto el usuario se convierte automáticamente en propietario con rol MANAGER. " +
        "Los datos están completamente aislados — solo se devuelven proyectos propios o en los que el usuario es miembro.")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    @Operation(
        summary = "Listar mis proyectos",
        description = "Devuelve todos los proyectos donde el usuario autenticado es propietario o miembro activo. " +
                      "El filtro se aplica directamente en la base de datos — nunca se devuelven proyectos de otros usuarios."
    )
    @ApiResponse(responseCode = "200", description = "Lista de proyectos devuelta")
    public ResponseEntity<List<ProjectDto>> getMyProjects() {
        return ResponseEntity.ok(projectService.getMyProjects());
    }

    @PostMapping
    @Operation(
        summary = "Crear proyecto",
        description = "Crea un nuevo proyecto. El usuario autenticado se convierte en propietario y se añade automáticamente " +
                      "como miembro con el rol MANAGER, que otorga control total sobre el proyecto. " +
                      "La clave debe ser alfanumérica en mayúsculas (ej. PROJ, UPTASK) y debe ser única en todo el sistema."
    )
    @ApiResponse(responseCode = "201", description = "Proyecto creado correctamente")
    @ApiResponse(responseCode = "409", description = "La clave del proyecto ya está en uso")
    @ApiResponse(responseCode = "422", description = "Error de validación en el cuerpo de la petición")
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody CreateProjectDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(dto));
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("@projectSecurity.isMember(#projectId)")
    @Operation(
        summary = "Obtener proyecto",
        description = "Devuelve el detalle de un proyecto. El usuario debe ser miembro o propietario. " +
                      "Se devuelve 404 para proyectos a los que el usuario no tiene acceso, para evitar filtración de información."
    )
    @ApiResponse(responseCode = "200", description = "Proyecto devuelto")
    @ApiResponse(responseCode = "404", description = "Proyecto no encontrado o el usuario no tiene acceso")
    public ResponseEntity<ProjectDto> getProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProject(projectId));
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'PROJECT_EDIT')")
    @Operation(
        summary = "Actualizar proyecto",
        description = "Actualiza el nombre y la descripción del proyecto. La clave no puede modificarse tras la creación. Requiere permiso PROJECT_EDIT."
    )
    @ApiResponse(responseCode = "200", description = "Proyecto actualizado")
    @ApiResponse(responseCode = "403", description = "Sin permiso PROJECT_EDIT")
    @ApiResponse(responseCode = "422", description = "Error de validación en el cuerpo de la petición")
    public ResponseEntity<ProjectDto> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectDto dto) {
        return ResponseEntity.ok(projectService.updateProject(projectId, dto));
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("@projectSecurity.isOwner(#projectId)")
    @Operation(
        summary = "Eliminar proyecto",
        description = "Elimina permanentemente el proyecto y todos sus datos asociados (roles, miembros, tareas, comentarios, actividad). " +
                      "Solo el propietario del proyecto puede realizar esta acción."
    )
    @ApiResponse(responseCode = "204", description = "Proyecto eliminado")
    @ApiResponse(responseCode = "403", description = "El usuario no es el propietario del proyecto")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }
}
