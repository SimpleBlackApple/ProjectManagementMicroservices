package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private String email;
    private String profilePhoto;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    private String newToken;
    private Long expiresIn;
}

