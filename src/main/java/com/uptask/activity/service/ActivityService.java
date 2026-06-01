package com.uptask.activity.service;

import com.uptask.activity.dto.ActivityDto;
import com.uptask.activity.entity.ActivityType;
import com.uptask.activity.entity.TaskActivity;
import com.uptask.activity.repository.TaskActivityRepository;
import com.uptask.task.entity.Task;
import com.uptask.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final TaskActivityRepository activityRepository;

    @Transactional
    public void record(Task task, User actor, ActivityType type, String oldValue, String newValue) {
        TaskActivity activity = new TaskActivity();
        activity.setTask(task);
        activity.setActor(actor);
        activity.setActionType(type);
        activity.setOldValue(oldValue);
        activity.setNewValue(newValue);
        activityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityDto> getTaskActivity(Long taskId) {
        return activityRepository.findAllByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::toDto)
                .toList();
    }

    private ActivityDto toDto(TaskActivity a) {
        return new ActivityDto(
                a.getId(),
                a.getActionType(),
                a.getOldValue(),
                a.getNewValue(),
                new ActivityDto.ActorDto(a.getActor().getId(), a.getActor().getFirstName(), a.getActor().getLastName()),
                a.getCreatedAt()
        );
    }
}
