package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SprintDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private Integer projectId;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer totalStoryPoints;
    private Integer completedStoryPoints;
    private String status;
} 