package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberDTO {
    private Integer memberId;
    private String username;
    private String email;
    private String realName;
    private String profilePhoto;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String status;
}
