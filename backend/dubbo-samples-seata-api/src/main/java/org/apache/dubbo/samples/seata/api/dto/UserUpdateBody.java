package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;

@Data
public class UserUpdateBody {
    private String name;
    private String email;
    private String password;
    private String profilePhoto;
}
