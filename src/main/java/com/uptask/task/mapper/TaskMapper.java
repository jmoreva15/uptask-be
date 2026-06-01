package com.uptask.task.mapper;

import com.uptask.task.dto.TaskDto;
import com.uptask.task.entity.Task;
import com.uptask.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskDto toDto(Task task) {
        return new TaskDto(
                task.getId(),
                task.getProject().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                toUserRef(task.getAssignee()),
                toUserRef(task.getReporter()),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private TaskDto.UserRef toUserRef(User user) {
        if (user == null) return null;
        return new TaskDto.UserRef(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
    }
}
