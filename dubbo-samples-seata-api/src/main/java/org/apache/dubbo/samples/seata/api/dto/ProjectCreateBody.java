package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;

@Data
public class ProjectCreateBody {
    private String name;
    private String description;
    private Integer ownerId;
} 