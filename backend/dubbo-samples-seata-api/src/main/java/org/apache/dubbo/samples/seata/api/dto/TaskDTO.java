package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Integer id;
    private String title;
    private String description;
    private String type;
    private String status;
    private Integer projectId;
    private Integer sprintId;
    private Integer memberId;
    private Integer storyPoints;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 