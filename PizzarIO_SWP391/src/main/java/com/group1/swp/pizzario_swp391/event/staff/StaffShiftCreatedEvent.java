package com.group1.swp.pizzario_swp391.event.staff;

import com.group1.swp.pizzario_swp391.entity.StaffShift;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event được publish khi tạo mới StaffShift
 * Để trigger việc schedule absent check và auto-complete
 */
@Getter
public class StaffShiftCreatedEvent extends ApplicationEvent {

    private final StaffShift staffShift;

    public StaffShiftCreatedEvent(Object source, StaffShift staffShift) {
        super(source);
        this.staffShift = staffShift;
    }
}