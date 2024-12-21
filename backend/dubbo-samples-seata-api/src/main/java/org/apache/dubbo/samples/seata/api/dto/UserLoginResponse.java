package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;

@Data
public class UserLoginResponse {
    private String token;
    private long expiresIn;
} 