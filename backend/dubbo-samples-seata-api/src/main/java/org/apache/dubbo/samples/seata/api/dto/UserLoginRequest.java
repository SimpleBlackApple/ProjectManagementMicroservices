package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String username;
    private String password;
} 