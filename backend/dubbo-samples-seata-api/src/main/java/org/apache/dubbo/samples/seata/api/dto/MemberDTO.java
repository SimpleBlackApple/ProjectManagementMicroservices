package org.apache.dubbo.samples.seata.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MemberDTO {
    private Integer userId;
    private LocalDateTime joinedAt;
}
