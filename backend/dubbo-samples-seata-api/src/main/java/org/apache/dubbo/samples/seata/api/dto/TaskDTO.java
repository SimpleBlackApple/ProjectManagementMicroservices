package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.io.Serializable;

@Data
public class TaskDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer id;
    private String title;
    private String description;
    private String type;
    private String status;
    private Integer projectId;
    private Integer sprintId;
    private Integer managerId;
    private Integer storyPoints;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 