package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffScheduleService {

    private final StaffShiftRepository staffShiftRepository;

    /**
     * Chạy mỗi 30 phút để kiểm tra các ca đã quá hạn check-in
     * Nếu quá 2 giờ sau giờ bắt đầu ca mà chưa check-in → đánh dấu ABSENT
     */
    @Scheduled(fixedRate = 5000) // 10p  30 phút = 1800000ms
    @Transactional
    public void markAbsentShifts() {
        log.info("🔄 Scheduled: markAbsentShifts() is running...");
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // Lấy tất cả ca làm hôm nay
        List<StaffShift> todayShifts = staffShiftRepository
                .findByWorkDateBetween(today, today);

        int absentCount = 0;
        for (StaffShift ss : todayShifts) {
            // Nếu ca đang SCHEDULED và chưa check-in
            if (ss.getStatus() == StaffShift.Status.SCHEDULED && ss.getCheckIn() == null) {
                LocalDateTime shiftStart = ss.getWorkDate().atTime(ss.getShift().getStartTime().toLocalTime());
                LocalDateTime deadline = shiftStart.plusMinutes(3); // Quá 2 giờ sau ca bắt đầu

                // Nếu đã quá deadline → đánh dấu ABSENT
                if (now.isAfter(deadline)) {
                    ss.setStatus(StaffShift.Status.ABSENT);
                    ss.setPenaltyPercent(100); // Phạt 100% lương ca này
                    String noteDetail = String.format("ABSENT - Không check-in (Deadline: %s)\n",
                            deadline.toLocalTime());
                    ss.setNote((ss.getNote() != null ? ss.getNote() : "") + noteDetail);
                    staffShiftRepository.save(ss);
                    absentCount++;

                    log.warn("Marked ABSENT for staff {} - shift {} on {}",
                            ss.getStaff().getName(),
                            ss.getShift().getShiftName(),
                            ss.getWorkDate());
                }
            }
        }

        if (absentCount > 0) {
            log.info("Marked {} shifts as ABSENT", absentCount);
        }
    }

    /**
     * Tự động kết thúc ca làm việc nếu nhân viên quên logout
     * Chạy vào 00:30 mỗi ngày
     */
    @Scheduled(cron = "0 30 0 * * *") // 00:30 AM hàng ngày
    @Transactional
    public void autoCompleteShifts() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime now = LocalDateTime.now();

        List<StaffShift> yesterdayShifts = staffShiftRepository
                .findByWorkDateBetween(yesterday, yesterday);

        int autoCompleted = 0;
        for (StaffShift ss : yesterdayShifts) {
            // Nếu đã check-in nhưng chưa check-out
            if (ss.getCheckIn() != null && ss.getCheckOut() == null) {
                // Tự động set check-out = end time của ca
                ss.setCheckOut(ss.getShift().getEndTime());
                ss.setStatus(StaffShift.Status.COMPLETED);
                String noteDetail = "AUTO-COMPLETED - Quên checkout\n";
                ss.setNote((ss.getNote() != null ? ss.getNote() : "") + noteDetail);
                staffShiftRepository.save(ss);
                autoCompleted++;

                log.warn("Auto-completed shift for staff {} - forgot to logout",
                        ss.getStaff().getName());
            }
        }

        if (autoCompleted > 0) {
            log.info("Auto-completed {} shifts", autoCompleted);
        }
    }

}
