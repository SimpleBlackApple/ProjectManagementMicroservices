package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.io.Serializable;

@Data
public class SprintUpdateBody implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
} 