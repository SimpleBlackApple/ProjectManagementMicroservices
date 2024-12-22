package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ProjectUpdateBody implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String name;
    private String description;
    private String status;
}