package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskUpdateBody {
    private String title;
    private String description;
    private String type;
    private String status;
    private Integer sprintId;
    private Integer storyPoints;
    private LocalDateTime dueDate;
} 