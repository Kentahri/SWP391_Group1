package com.group1.swp.pizzario_swp391.dto.reservation;

import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {

    private Long id;
    private Integer tableId;
    private String tableName;
    private String customerName;
    private String customerPhone;
    private int capacityExpected;
    private LocalDateTime startTime;
    private LocalDateTime createdAt;
    private String status;
    private String note;
    private String tableStatus;
    private Integer autoLockMinutes;


    public boolean isReadyToOpen() {
        if (startTime == null || autoLockMinutes == null) {
            return false;
        }
        LocalDateTime validateTime = LocalDateTime.now().plusMinutes(autoLockMinutes);
        return startTime.isBefore(validateTime) || startTime.isEqual(validateTime);
    }
}

