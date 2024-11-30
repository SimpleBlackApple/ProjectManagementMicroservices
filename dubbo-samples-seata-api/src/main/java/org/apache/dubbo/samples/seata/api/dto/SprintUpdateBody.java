package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SprintUpdateBody {
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
} 