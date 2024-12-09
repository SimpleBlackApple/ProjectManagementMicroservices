package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;

@Data
public class UserLoginResponse {
    private Integer userId;
    private String username;
    private String email;
    private String realName;
    private String status;
} 