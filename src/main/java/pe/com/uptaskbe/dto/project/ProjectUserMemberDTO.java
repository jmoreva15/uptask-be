package pe.com.uptaskbe.dto.project;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private String clientName;
    private String managerId;
}
