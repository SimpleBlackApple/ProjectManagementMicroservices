package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private String name;
    private String email;
    private String password;
}