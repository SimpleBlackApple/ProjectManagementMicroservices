package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ProjectCreateBody implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String description;
} 