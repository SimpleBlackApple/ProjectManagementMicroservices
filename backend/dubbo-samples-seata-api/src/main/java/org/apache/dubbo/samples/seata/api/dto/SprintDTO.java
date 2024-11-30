package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SprintDTO {
    private Integer id;
    private Integer projectId;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer totalStoryPoints;
    private Integer completedStoryPoints;
    private String status; // TO_DO, IN_PROGRESS, DONE
} 