package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SprintCreateBody implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
} 