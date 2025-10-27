package com.group1.swp.pizzario_swp391.event.staff;

import com.group1.swp.pizzario_swp391.entity.StaffShift;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event được publish khi update StaffShift
 * Để reschedule absent check và auto-complete nếu cần
 */
@Getter
public class StaffShiftUpdatedEvent extends ApplicationEvent {

    private final StaffShift staffShift;
    private final boolean needsReschedule;

    public StaffShiftUpdatedEvent(Object source, StaffShift staffShift, boolean needsReschedule) {
        super(source);
        this.staffShift = staffShift;
        this.needsReschedule = needsReschedule;
    }
}