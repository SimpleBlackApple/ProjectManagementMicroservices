package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer ownerId;
    private LocalDateTime createdAt;
    private String status; // IN_PROGRESS, DONE
} 