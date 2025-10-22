package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.Staff;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.repository.LoginRepository;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public Optional<Staff> authenticate(String email, String pas) {

        return loginRepository.findByEmail(email)
                .filter(db -> java.util.Objects.equals(db.getPassword(), pas));
    }

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

        Optional<StaffShift> targetShift = findShiftToCheckIn(shiftsToday, now);

        if (targetShift.isEmpty()) {
            log.warn("No valid shift found for staff {} to check-in at {}",
                    staff.getName(), now);
            return false;
        }

        StaffShift ss = targetShift.get();
        LocalDateTime shiftStart = ss.getShift().getStartTime();
        
        Long minutesLate = Duration.between(shiftStart, now).toMinutes();

        LocalDateTime actualCheckIn;
        StaffShift.Status status;
        String noteDetail;

        Duration d = Duration.ofMinutes(Math.abs(minutesLate));
        long h = d.toHoursPart();      // phần giờ
        int m = d.toMinutesPart();     // phần phút còn lại
        String formMinutesLate = h + " giờ " + m + " phút ";

        if(minutesLate <= 0){
            status = StaffShift.Status.PRESENT;
            actualCheckIn = now;
            noteDetail = String.format("PRESENT - Check-in: %s\n", now.toLocalTime());
        }else if (minutesLate <= 15) {

            status = StaffShift.Status.PRESENT;
            actualCheckIn = shiftStart;
            noteDetail = String.format("PRESENT (Muộn %s phút - Đã tha thứ) - Thực tế: %s - Ghi nhận: %s\n",
                    formMinutesLate, now.toLocalTime(), shiftStart.toLocalTime());

        } else {

            status = StaffShift.Status.LATE;
            actualCheckIn = now;

            int penaltyPercent;
            if (minutesLate <= 30) {
                penaltyPercent = 5;
            } else if (minutesLate <= 60) {
                penaltyPercent = 10;
            } else {
                penaltyPercent = 15;
            }

            noteDetail = String.format("LATE (Muộn %s phút - Phạt %d%%) - Check-in: %s\n",
                    formMinutesLate, penaltyPercent, now.toLocalTime());
        }

        ss.setStatus(status);
        ss.setNote((ss.getNote() != null ? ss.getNote() : "") + noteDetail + " | ");
        ss.setCheckIn(actualCheckIn);
        staffShiftRepository.save(ss);

        return true;
    }

    private Optional<StaffShift> findShiftToCheckIn(List<StaffShift> shifts, LocalDateTime now) {
        return shifts.stream()
                .filter(ss -> ss.getCheckIn() == null) // Chưa check-in
                .min(Comparator .comparing(ss -> {
                    // Chọn ca có thời gian bắt đầu gần nhất
                    return Math.abs(Duration.between(ss.getShift().getStartTime(), now).toMinutes());
                }));
    }

    public void recordLogoutByEmail(String email) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Staff staff = this.findByEmail(email);
        StaffShift staffShift = staffShiftRepository.findCurrentShiftByStaffId(staff.getId(), today).get();

        if (now.isBefore(staffShift.getShift().getEndTime().toLocalTime())){
            StaffShift.Status shiftType = StaffShift.Status.LEFT_EARLY;
            staffShift.setStatus(shiftType);
            staffShift.setNote(staffShift.getNote() + shiftType + " - " + now + " - ");
        }
        else{
            StaffShift.Status shiftType = StaffShift.Status.COMPLETED;
            staffShift.setStatus(shiftType);
            staffShift.setNote(staffShift.getNote() + shiftType + " - " + now + " - ");
        }

        staffShift.setCheckOut(LocalDateTime.now());
        staffShiftRepository.save(staffShift);
    }

}
