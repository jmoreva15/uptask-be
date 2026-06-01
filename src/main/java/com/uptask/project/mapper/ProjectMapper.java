package com.uptask.project.mapper;

import com.uptask.project.dto.ProjectDto;
import com.uptask.project.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectDto toDto(Project project) {
        return new ProjectDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getKey(),
                new ProjectDto.ProjectOwnerDto(
                        project.getOwner().getId(),
                        project.getOwner().getFirstName(),
                        project.getOwner().getLastName(),
                        project.getOwner().getEmail()
                ),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
