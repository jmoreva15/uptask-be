package com.uptask.task.controller;

import com.uptask.activity.dto.ActivityDto;
import com.uptask.activity.service.ActivityService;
import com.uptask.comment.dto.CommentDto;
import com.uptask.comment.dto.CreateCommentDto;
import com.uptask.comment.service.CommentService;
import com.uptask.common.dto.PageDto;
import com.uptask.projectrole.entity.ProjectPermissionName;
import com.uptask.security.project.ProjectSecurityService;
import com.uptask.task.dto.*;
import com.uptask.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/projects/{projectId}/tasks")
@RequiredArgsConstructor
@Tag(name = "Tareas", description = "Gestión de tareas dentro de un proyecto. Todos los endpoints requieren que el usuario sea miembro del proyecto con el permiso correspondiente.")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;
    private final CommentService commentService;
    private final ActivityService activityService;
    private final ProjectSecurityService projectSecurityService;

    @GetMapping
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'TASK_VIEW')")
    @Operation(
        summary = "Listar tareas",
        description = "Devuelve una lista paginada de tareas del proyecto, ordenadas por fecha de creación descendente. Requiere permiso TASK_VIEW."
    )
    @ApiResponse(responseCode = "200", description = "Lista de tareas devuelta")
    @ApiResponse(responseCode = "403", description = "Sin permiso TASK_VIEW")
    public ResponseEntity<PageDto<TaskDto>> getTasks(
            @PathVariable Long projectId,
            @Parameter(description = "Índice de página (base cero)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (máximo recomendado: 50)") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(taskService.getTasks(projectId, page, size));
    }

    @PostMapping
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'TASK_CREATE')")
    @Operation(
        summary = "Crear tarea",
        description = "Crea una nueva tarea. El usuario autenticado se convierte en el reportero. " +
                      "La prioridad es MEDIUM por defecto si no se indica. El assigneeId debe ser un miembro activo del proyecto. Requiere permiso TASK_CREATE."
    )
    @ApiResponse(responseCode = "201", description = "Tarea creada")
    @ApiResponse(responseCode = "400", description = "El assigneeId no es miembro de este proyecto")
    @ApiResponse(responseCode = "403", description = "Sin permiso TASK_CREATE")
    @ApiResponse(responseCode = "422", description = "Error de validación en el cuerpo de la petición")
    public ResponseEntity<TaskDto> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(projectId, dto));
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'TASK_VIEW')")
    @Operation(
        summary = "Obtener tarea",
        description = "Devuelve el detalle completo de una tarea incluyendo responsable, reportero, estado y prioridad. Requiere permiso TASK_VIEW."
    )
    @ApiResponse(responseCode = "200", description = "Tarea devuelta")
    @ApiResponse(responseCode = "403", description = "Sin permiso TASK_VIEW")
    @ApiResponse(responseCode = "404", description = "Tarea no encontrada en este proyecto")
    public ResponseEntity<TaskDto> getTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTask(projectId, taskId));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'TASK_EDIT')")
    @Operation(
        summary = "Actualizar tarea",
        description = "Actualiza título, descripción, prioridad y fecha límite. Cada campo modificado queda registrado automáticamente en el historial de actividad. Requiere permiso TASK_EDIT."
    )
    @ApiResponse(responseCode = "200", description = "Tarea actualizada")
    @ApiResponse(responseCode = "403", description = "Sin permiso TASK_EDIT")
    @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    @ApiResponse(responseCode = "422", description = "Error de validación en el cuerpo de la petición")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskDto dto) {
        return ResponseEntity.ok(taskService.updateTask(projectId, taskId, dto));
    }

    @PatchMapping("/{taskId}/status")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'TASK_STATUS_CHANGE')")
    @Operation(
        summary = "Cambiar estado de tarea",
        description = "Cambia el estado de la tarea. Valores válidos: TODO, IN_PROGRESS, REVIEW, DONE. " +
                      "El cambio queda registrado en el historial de actividad. Requiere permiso TASK_STATUS_CHANGE."
    )
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "403", description = "Sin permiso TASK_STATUS_CHANGE")
    @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    public ResponseEntity<TaskDto> changeStatus(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody ChangeStatusDto dto) {
        return ResponseEntity.ok(taskService.changeStatus(projectId, taskId, dto));
    }

    @PatchMapping("/{taskId}/assignee")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'TASK_ASSIGN')")
    @Operation(
        summary = "Asignar o desasignar tarea",
        description = "Asigna la tarea al miembro indicado. Envía assigneeId=null para desasignar. " +
                      "El responsable debe ser miembro activo del proyecto. El cambio queda registrado en el historial. Requiere permiso TASK_ASSIGN."
    )
    @ApiResponse(responseCode = "200", description = "Responsable actualizado")
    @ApiResponse(responseCode = "400", description = "El userId indicado no es miembro del proyecto")
    @ApiResponse(responseCode = "403", description = "Sin permiso TASK_ASSIGN")
    @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    public ResponseEntity<TaskDto> assignTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody AssignTaskDto dto) {
        return ResponseEntity.ok(taskService.assignTask(projectId, taskId, dto));
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'TASK_DELETE')")
    @Operation(
        summary = "Eliminar tarea",
        description = "Elimina permanentemente la tarea junto con todos sus comentarios e historial de actividad. Requiere permiso TASK_DELETE."
    )
    @ApiResponse(responseCode = "204", description = "Tarea eliminada")
    @ApiResponse(responseCode = "403", description = "Sin permiso TASK_DELETE")
    @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        taskService.deleteTask(projectId, taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{taskId}/comments")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'TASK_VIEW')")
    @Operation(
        summary = "Listar comentarios",
        description = "Devuelve todos los comentarios de la tarea ordenados por fecha de creación ascendente. Requiere permiso TASK_VIEW."
    )
    @ApiResponse(responseCode = "200", description = "Lista de comentarios devuelta")
    @ApiResponse(responseCode = "403", description = "Sin permiso TASK_VIEW")
    @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    public ResponseEntity<List<CommentDto>> getComments(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.getComments(projectId, taskId));
    }

    @PostMapping("/{taskId}/comments")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'COMMENT_CREATE')")
    @Operation(
        summary = "Añadir comentario",
        description = "Añade un comentario a la tarea. El usuario autenticado se convierte en el autor. Requiere permiso COMMENT_CREATE."
    )
    @ApiResponse(responseCode = "201", description = "Comentario creado")
    @ApiResponse(responseCode = "403", description = "Sin permiso COMMENT_CREATE")
    @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    @ApiResponse(responseCode = "422", description = "Error de validación en el cuerpo de la petición")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody CreateCommentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addComment(projectId, taskId, dto));
    }

    @DeleteMapping("/{taskId}/comments/{commentId}")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'COMMENT_DELETE_OWN') " +
                  "or @projectSecurity.hasPermission(#projectId, 'COMMENT_DELETE_ANY')")
    @Operation(
        summary = "Eliminar comentario",
        description = "Elimina un comentario. Un usuario con COMMENT_DELETE_OWN solo puede eliminar sus propios comentarios. " +
                      "Un usuario con COMMENT_DELETE_ANY puede eliminar cualquier comentario. Se requiere al menos uno de los dos permisos."
    )
    @ApiResponse(responseCode = "204", description = "Comentario eliminado")
    @ApiResponse(responseCode = "403", description = "Intento de eliminar el comentario de otro usuario sin tener COMMENT_DELETE_ANY")
    @ApiResponse(responseCode = "404", description = "Comentario no encontrado")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @PathVariable Long commentId) {
        boolean canDeleteAny = projectSecurityService.hasPermission(projectId, ProjectPermissionName.COMMENT_DELETE_ANY);
        commentService.deleteComment(projectId, taskId, commentId, canDeleteAny);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{taskId}/activity")
    @PreAuthorize("@projectSecurity.hasPermission(#projectId, 'TASK_VIEW')")
    @Operation(
        summary = "Historial de actividad",
        description = "Devuelve el registro de auditoría completo e inmutable de la tarea, ordenado por más reciente primero. " +
                      "Registra: creación, cambios de título/descripción/prioridad/fecha límite, transiciones de estado, asignaciones y eventos de comentarios. Requiere permiso TASK_VIEW."
    )
    @ApiResponse(responseCode = "200", description = "Historial devuelto")
    @ApiResponse(responseCode = "403", description = "Sin permiso TASK_VIEW")
    @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    public ResponseEntity<List<ActivityDto>> getActivity(
            @PathVariable Long projectId,
            @PathVariable Long taskId) {
        taskService.getTask(projectId, taskId);
        return ResponseEntity.ok(activityService.getTaskActivity(taskId));
    }
}
