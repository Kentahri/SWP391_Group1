package com.group1.swp.pizzario_swp391.event.staff;

import com.group1.swp.pizzario_swp391.entity.StaffShift;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;
import java.util.List;

/**
 * Event được publish khi check coverage của một ca
 */
@Getter
public class StaffShiftCoverageEvent extends ApplicationEvent {

    private final Integer shiftId;
    private final LocalDate workDate;
    private final String shiftName;
    private final long totalStaff;
    private final long presentStaff;
    private final long absentStaff;

    public StaffShiftCoverageEvent(Object source, Integer shiftId, LocalDate workDate,
                                   List<StaffShift> allStaffShifts) {
        super(source);
        this.shiftId = shiftId;
        this.workDate = workDate;

        // Tính toán metrics - SỬA LỖI: getShiftName() trả về ShiftType, cần convert sang String
        this.shiftName = allStaffShifts.isEmpty() ? "Unknown" :
                allStaffShifts.get(0).getShift().getShiftName().name();
        this.totalStaff = allStaffShifts.size();
        this.presentStaff = allStaffShifts.stream()
                .filter(ss -> ss.getStatus() == StaffShift.Status.PRESENT ||
                        ss.getStatus() == StaffShift.Status.LATE)
                .count();
        this.absentStaff = allStaffShifts.stream()
                .filter(ss -> ss.getStatus() == StaffShift.Status.ABSENT)
                .count();
    }
}