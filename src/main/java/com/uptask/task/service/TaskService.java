package com.uptask.task.service;

import com.uptask.activity.entity.ActivityType;
import com.uptask.activity.service.ActivityService;
import com.uptask.common.dto.PageDto;
import com.uptask.common.exception.ApplicationException;
import com.uptask.member.repository.ProjectMemberRepository;
import com.uptask.project.entity.Project;
import com.uptask.project.exception.ProjectNotFoundException;
import com.uptask.project.repository.ProjectRepository;
import com.uptask.security.principal.UserPrincipal;
import com.uptask.task.dto.*;
import com.uptask.task.entity.Task;
import com.uptask.task.exception.TaskNotFoundException;
import com.uptask.task.mapper.TaskMapper;
import com.uptask.task.repository.TaskRepository;
import com.uptask.user.entity.User;
import com.uptask.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;
    private final TaskMapper mapper;

    @Transactional(readOnly = true)
    public PageDto<TaskDto> getTasks(Long projectId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var pageResult = taskRepository.findAllByProjectId(projectId, pageable);
        return PageDto.from(pageResult, mapper::toDto);
    }

    @Transactional(readOnly = true)
    public TaskDto getTask(Long projectId, Long taskId) {
        return mapper.toDto(findTask(projectId, taskId));
    }

    @Transactional
    public TaskDto createTask(Long projectId, CreateTaskDto dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        User reporter = currentUser();

        Task task = new Task();
        task.setProject(project);
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setPriority(dto.priority() != null ? dto.priority() : task.getPriority());
        task.setReporter(reporter);
        task.setDueDate(dto.dueDate());

        if (dto.assigneeId() != null) {
            task.setAssignee(resolveProjectMember(projectId, dto.assigneeId()));
        }

        task = taskRepository.save(task);
        activityService.record(task, reporter, ActivityType.TASK_CREATED, null, task.getTitle());

        return mapper.toDto(task);
    }

    @Transactional
    public TaskDto updateTask(Long projectId, Long taskId, UpdateTaskDto dto) {
        Task task = findTask(projectId, taskId);
        User actor = currentUser();

        if (!task.getTitle().equals(dto.title())) {
            activityService.record(task, actor, ActivityType.TITLE_CHANGED, task.getTitle(), dto.title());
            task.setTitle(dto.title());
        }

        if (descriptionChanged(task.getDescription(), dto.description())) {
            activityService.record(task, actor, ActivityType.DESCRIPTION_CHANGED, null, null);
            task.setDescription(dto.description());
        }

        if (dto.priority() != null && task.getPriority() != dto.priority()) {
            activityService.record(task, actor, ActivityType.PRIORITY_CHANGED,
                    task.getPriority().name(), dto.priority().name());
            task.setPriority(dto.priority());
        }

        if (dueDateChanged(task.getDueDate(), dto.dueDate())) {
            activityService.record(task, actor, ActivityType.DUE_DATE_CHANGED,
                    task.getDueDate() != null ? task.getDueDate().toString() : null,
                    dto.dueDate() != null ? dto.dueDate().toString() : null);
            task.setDueDate(dto.dueDate());
        }

        return mapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public TaskDto changeStatus(Long projectId, Long taskId, ChangeStatusDto dto) {
        Task task = findTask(projectId, taskId);
        User actor = currentUser();

        if (task.getStatus() != dto.status()) {
            activityService.record(task, actor, ActivityType.STATUS_CHANGED,
                    task.getStatus().name(), dto.status().name());
            task.setStatus(dto.status());
        }

        return mapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public TaskDto assignTask(Long projectId, Long taskId, AssignTaskDto dto) {
        Task task = findTask(projectId, taskId);
        User actor = currentUser();

        String oldAssignee = task.getAssignee() != null ? task.getAssignee().getEmail() : null;

        if (dto.assigneeId() == null) {
            activityService.record(task, actor, ActivityType.UNASSIGNED, oldAssignee, null);
            task.setAssignee(null);
        } else {
            User newAssignee = resolveProjectMember(projectId, dto.assigneeId());
            activityService.record(task, actor, ActivityType.ASSIGNED, oldAssignee, newAssignee.getEmail());
            task.setAssignee(newAssignee);
        }

        return mapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long projectId, Long taskId) {
        Task task = findTask(projectId, taskId);
        taskRepository.delete(task);
    }

    private Task findTask(Long projectId, Long taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private User resolveProjectMember(Long projectId, Long userId) {
        if (!memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ApplicationException("Assignee is not a member of this project", HttpStatus.BAD_REQUEST);
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException("User not found", HttpStatus.BAD_REQUEST));
    }

    private User currentUser() {
        Long userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException("User not found", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private boolean descriptionChanged(String current, String incoming) {
        if (current == null && incoming == null) return false;
        if (current == null || incoming == null) return true;
        return !current.equals(incoming);
    }

    private boolean dueDateChanged(java.time.LocalDate current, java.time.LocalDate incoming) {
        if (current == null && incoming == null) return false;
        if (current == null || incoming == null) return true;
        return !current.equals(incoming);
    }
}
