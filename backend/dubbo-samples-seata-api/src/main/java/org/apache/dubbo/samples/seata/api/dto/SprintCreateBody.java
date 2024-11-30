package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SprintCreateBody {
    private Integer projectId;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
} 