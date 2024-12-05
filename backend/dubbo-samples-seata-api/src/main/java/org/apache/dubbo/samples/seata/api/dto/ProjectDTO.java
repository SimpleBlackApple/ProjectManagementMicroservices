package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
    private String description;
    private Integer ownerId;
    private LocalDateTime createdAt;
    private String status; // IN_PROGRESS, DONE
    private List<MemberDTO> members;
} 