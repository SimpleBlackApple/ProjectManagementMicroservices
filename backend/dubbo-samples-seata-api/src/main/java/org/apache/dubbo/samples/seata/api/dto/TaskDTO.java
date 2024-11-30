package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDTO {
    private Integer id;
    private String title;
    private String description;
    private String type; // TASK, USER_STORY, ISSUE, BUG
    private String status; // TO_DO, IN_PROGRESS, DONE
    private Integer projectId;
    private Integer sprintId;
    private Integer storyPoints;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueDate;
} 