package com.group1.swp.pizzario_swp391.event.staff;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;

@Getter
public class StaffAbsentEvent extends ApplicationEvent {

    private final Integer staffShiftId;
    private final LocalDateTime triggeredAt;

    public StaffAbsentEvent(Object source, Integer staffShiftId) {
        super(source);
        this.staffShiftId = staffShiftId;
        this.triggeredAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("StaffAbsentEvent{staffShiftId=%d, triggeredAt=%s}",
                staffShiftId, triggeredAt);
    }
}