package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String username;
    private String email;
    private String realName;
    private String profilePhoto;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String status;
}

