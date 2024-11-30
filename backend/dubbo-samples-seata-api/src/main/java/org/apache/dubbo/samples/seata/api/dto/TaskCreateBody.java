package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskCreateBody {
    private String title;
    private String description;
    private String type;
    private Integer projectId;
    private Integer sprintId;
    private Integer storyPoints;
    private LocalDateTime dueDate;
} 