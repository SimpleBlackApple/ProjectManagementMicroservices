package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Integer userId;
    private String username;
    private String email;
    private String realName;
    private String profilePhoto;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String status;
}

