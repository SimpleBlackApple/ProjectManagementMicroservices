package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCreateBody {
    private Integer userId;
    private String username;
    private String passwordHash;
    private String email;
    private String realName;
    private String profilePhoto;
    private String status;
}

