package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.event.staff.StaffShiftUpdatedEvent;
import com.group1.swp.pizzario_swp391.repository.LoginRepository;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final LoginRepository loginRepository;
    private final StaffShiftRepository staffShiftRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Staff findByEmail(String email) {
        return loginRepository.findByEmail(email).orElseThrow();
    }

    @Transactional
    public boolean recordLoginByEmail(String email) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        Staff staff = this.findByEmail(email);

        List<StaffShift> shiftsToday = staffShiftRepository
                .findAllShiftsByStaffIdAndDate(staff.getId(), today);

        if (shiftsToday.isEmpty()) {
            log.warn("No shifts scheduled for staff {} on {}", staff.getName(), today);
            return false;
        }

        // ✅ Case 3: Cho phép login lại nếu bấm nhầm logout sớm (LEFT_EARLY)
// Giống refresh token - chỉ có 5 phút để re-login
        Optional<StaffShift> clickLogoutEarly = shiftsToday.stream()
                .filter(ss -> ss.getStatus() == StaffShift.Status.LEFT_EARLY)
                .filter(ss -> ss.getCheckIn() != null && ss.getCheckOut() != null)
                .filter(ss -> {
                    LocalDateTime logoutTime = ss.getCheckOut();

                    // ✅ CHỈ CHO PHÉP RE-LOGIN TRONG VÒNG 5 PHÚT SAU KHI LOGOUT
                    long minutesSinceLogout = Duration.between(logoutTime, now).toMinutes();

                    if (minutesSinceLogout > 5) {
                        log.warn("Staff {} logout lúc {} - Đã quá 5 phút ({}m), không cho re-login",
                                ss.getStaff().getName(), logoutTime, minutesSinceLogout);
                        return false;
                    }

                    // ✅ Kiểm tra thêm: Vẫn còn trong thời gian ca làm việc
                    LocalDateTime shiftEnd = ss.getWorkDate()
                            .atTime(ss.getShift().getEndTime().toLocalTime());

                    return now.isBefore(shiftEnd.plusMinutes(30)); // Cho thêm 30p buffer sau ca
                })
                .findFirst();

        if (clickLogoutEarly.isPresent()) {
            StaffShift ss = clickLogoutEarly.get();
            LocalDateTime logoutTime = ss.getCheckOut();
            long minutesSinceLogout = Duration.between(logoutTime, now).toMinutes();

            // Reset checkout và status
            LocalDateTime oldCheckOut = ss.getCheckOut();
            ss.setCheckOut(null);
            ss.setStatus(ss.getPenaltyPercent() > 0 ? StaffShift.Status.LATE : StaffShift.Status.PRESENT);

            String noteDetail = String.format("RE-LOGIN (Logout nhầm lúc %s - Re-login sau %dm lúc %s)\n",
                    oldCheckOut.toLocalTime(), minutesSinceLogout, now.toLocalTime());
            ss.setNote((ss.getNote() != null ? ss.getNote() : "") + noteDetail + " | ");

            staffShiftRepository.save(ss);

            // Trigger event để schedule lại auto-complete task
            eventPublisher.publishEvent(new StaffShiftUpdatedEvent(this, ss, true));

            log.info("✅ RE-LOGIN SUCCESS: Staff {} re-login sau {}m (logout lúc {})",
                    staff.getName(), minutesSinceLogout, logoutTime.toLocalTime());

            return true;
        }

        Optional<StaffShift> targetShift = findShiftToCheckIn(shiftsToday, now);

        if (targetShift.isEmpty()) {
            log.warn("No valid shift found for staff {} to check-in at {}",
                    staff.getName(), now);
            return false;
        }

        StaffShift ss = targetShift.get();

        LocalDateTime shiftStart = ss.getWorkDate()
                .atTime(ss.getShift().getStartTime().toLocalTime());

        Long minutesLate = Duration.between(shiftStart, now).toMinutes();

        LocalDateTime actualCheckIn;
        StaffShift.Status status;
        String noteDetail;

        Duration d = Duration.ofMinutes(Math.abs(minutesLate));
        long h = d.toHoursPart();
        int m = d.toMinutesPart();
        String formMinutesLate = h + " giờ " + m + " phút ";

        if (minutesLate <= 0) {
            status = StaffShift.Status.PRESENT;
            actualCheckIn = now;
            noteDetail = String.format("PRESENT - Check-in: %s\n", now.toLocalTime());
        } else if (minutesLate <= 15) {
            status = StaffShift.Status.PRESENT;
            actualCheckIn = shiftStart;
            noteDetail = String.format("PRESENT (Muộn %s phút - Đã tha thứ) - Thực tế: %s - Ghi nhận: %s\n",
                    formMinutesLate, now.toLocalTime(), shiftStart.toLocalTime());
        } else {
            status = StaffShift.Status.LATE;
            actualCheckIn = now;

            int penaltyPercent = 0;
            if (minutesLate <= 30) {
                penaltyPercent = 5; // 5% for 15-30 minutes late
            } else if (minutesLate < 50) {
                penaltyPercent = 10; // 10% for 30-49 minutes late
            } else {
                penaltyPercent = 15; // 15% for 50+ minutes late
            }

            ss.setPenaltyPercent(penaltyPercent);

            noteDetail = String.format("LATE (Muộn %s phút - Phạt %d%%) - Check-in: %s\n",
                    formMinutesLate, penaltyPercent, now.toLocalTime());
        }

        ss.setStatus(status);
        ss.setNote((ss.getNote() != null ? ss.getNote() : "") + noteDetail + " | ");
        ss.setCheckIn(actualCheckIn);
        staffShiftRepository.save(ss);

        // ✅ THÊM: Trigger event để cancel absent check task
        eventPublisher.publishEvent(new StaffShiftUpdatedEvent(this, ss, true));

        log.info("LoginService: Checked in staff {} for shift {} - triggered event",
                staff.getName(), ss.getId());

        return true;
    }

    private Optional<StaffShift> findShiftToCheckIn(List<StaffShift> shifts, LocalDateTime now) {
        return shifts.stream()
                .filter(ss -> ss.getCheckIn() == null && ss.getStatus() != StaffShift.Status.ABSENT)
                .filter(ss -> {

                    LocalDateTime shiftStart = ss.getWorkDate()
                            .atTime(ss.getShift().getStartTime().toLocalTime());
                    LocalDateTime earliestCheckIn = shiftStart.minusHours(1); // 1 hour before
                    LocalDateTime latestCheckIn = shiftStart.plusHours(1); // 1 hour after
                    return now.isAfter(earliestCheckIn) && now.isBefore(latestCheckIn);
                })
                .min(Comparator.comparing(ss -> {
                    LocalDateTime shiftStart = ss.getWorkDate()
                            .atTime(ss.getShift().getStartTime().toLocalTime());
                    return Math.abs(Duration.between(shiftStart, now).toMinutes());
                }));
    }

    @Transactional
    public void recordLogoutByEmail(String email) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Staff staff = this.findByEmail(email);
        Optional<StaffShift> staffShiftOpt = staffShiftRepository.findCurrentShiftByStaffId(staff.getId(), today);

        if (staffShiftOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy ca làm việc hiện tại");
        }

        StaffShift staffShift = staffShiftOpt.get();

        // Check if staff has already checked in
        if (staffShift.getCheckIn() == null) {
            throw new RuntimeException("Nhân viên chưa check-in, không thể check-out");
        }

        // Check if staff has already checked out
        if (staffShift.getCheckOut() != null) {
            throw new RuntimeException("Nhân viên đã check-out rồi");
        }

        if(staffShift.getStatus() == StaffShift.Status.NOT_CHECKOUT){
            throw new RuntimeException("Nhân viên quên check out");
        }

        if (now.isBefore(staffShift.getShift().getEndTime().toLocalTime())) {
            StaffShift.Status shiftType = StaffShift.Status.LEFT_EARLY;
            staffShift.setStatus(shiftType);
            staffShift.setNote(staffShift.getNote() + shiftType + " - " + now + " - ");
        } else {
            StaffShift.Status shiftType = StaffShift.Status.COMPLETED;
            staffShift.setStatus(shiftType);
            staffShift.setNote(staffShift.getNote() + shiftType + " - " + now + " - ");
        }

        staffShift.setCheckOut(LocalDateTime.now());
        staffShiftRepository.save(staffShift);

        // ✅ THÊM: Trigger event để cancel auto-complete task
        eventPublisher.publishEvent(new StaffShiftUpdatedEvent(this, staffShift, true));

        log.info("LoginService: Checked out staff {} for shift {} - triggered event",
                staff.getName(), staffShift.getId());
    }
}
