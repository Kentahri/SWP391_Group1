package com.group1.swp.pizzario_swp391.dto.membership;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MembershipDTO {
    private Long id;
    private String phoneNumber;
    private String name;
    private boolean isActive;
    private LocalDateTime joinedAt;
    private Integer points;
}
