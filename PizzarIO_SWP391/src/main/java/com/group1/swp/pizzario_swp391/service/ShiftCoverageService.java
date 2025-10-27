package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.event.staff.StaffShiftCoverageEvent;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service chuyên xử lý coverage của ca làm việc
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftCoverageService {

    private final StaffShiftRepository staffShiftRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * BƯỚC 1: Lấy tất cả staff của một ca
     */
    public List<StaffShift> getAllStaffForShift(Integer shiftId, LocalDate workDate) {
        log.debug("Getting all staff for shift {} on {}", shiftId, workDate);
        return staffShiftRepository.findAllStaffForShift(shiftId, workDate);
    }

    /**
     * BƯỚC 2: Tính toán coverage metrics
     */
    public CoverageMetrics calculateCoverageMetrics(List<StaffShift> allStaffShifts) {
        if (allStaffShifts.isEmpty()) {
            return new CoverageMetrics(0, 0, 0, 0, "Unknown");
        }

        long totalStaff = allStaffShifts.size();
        long presentStaff = allStaffShifts.stream()
                .filter(ss -> ss.getStatus() == StaffShift.Status.PRESENT ||
                        ss.getStatus() == StaffShift.Status.LATE)
                .count();
        long absentStaff = allStaffShifts.stream()
                .filter(ss -> ss.getStatus() == StaffShift.Status.ABSENT)
                .count();
        long scheduledStaff = allStaffShifts.stream()
                .filter(ss -> ss.getStatus() == StaffShift.Status.SCHEDULED)
                .count();

        String shiftName = allStaffShifts.get(0).getShift().getShiftName().name();

        return new CoverageMetrics(totalStaff, presentStaff, absentStaff, scheduledStaff, shiftName);
    }

    /**
     * BƯỚC 3: Đánh giá coverage và log kết quả
     */
    public void evaluateCoverage(CoverageMetrics metrics, LocalDate workDate) {
        log.info("📊 Coverage Report - {} on {}", metrics.shiftName, workDate);
        log.info("📊 Total: {}, Present: {}, Absent: {}, Scheduled: {}",
                metrics.totalStaff, metrics.presentStaff, metrics.absentStaff, metrics.scheduledStaff);

        if (metrics.presentStaff == 0) {
            log.error("🚨 CRITICAL: No staff present for shift {} on {}!", metrics.shiftName, workDate);
        } else if (metrics.presentStaff < (metrics.totalStaff * 0.5)) {
            log.warn("⚠️ WARNING: Low coverage for shift {} on {} - Only {}/{} staff present",
                    metrics.shiftName, workDate, metrics.presentStaff, metrics.totalStaff);
        } else {
            log.info("✅ Good coverage for shift {} on {} - {}/{} staff present",
                    metrics.shiftName, workDate, metrics.presentStaff, metrics.totalStaff);
        }
    }

    /**
     * BƯỚC 4: Publish coverage event
     */
    public void publishCoverageEvent(Integer shiftId, LocalDate workDate, List<StaffShift> allStaffShifts) {
        log.debug("Publishing coverage event for shift {} on {}", shiftId, workDate);
        eventPublisher.publishEvent(new StaffShiftCoverageEvent(this, shiftId, workDate, allStaffShifts));
    }

    /**
     * BƯỚC 5: Xử lý coverage event (có thể gửi alert, email, etc.)
     */
    @EventListener
    public void onShiftCoverageEvent(StaffShiftCoverageEvent event) {
        log.info("📧 Processing coverage event for shift {} on {}",
                event.getShiftName(), event.getWorkDate());

        // TODO: Implement alert logic
        if (event.getPresentStaff() == 0) {
            log.error("🚨 CRITICAL ALERT: No staff present for shift {} on {}!",
                    event.getShiftName(), event.getWorkDate());
        } else if (event.getPresentStaff() < (event.getTotalStaff() * 0.5)) {
            log.warn("⚠️ ALERT: Low coverage for shift {} on {} - Only {}/{} staff present",
                    event.getShiftName(), event.getWorkDate(),
                    event.getPresentStaff(), event.getTotalStaff());
        }
    }

    /**
     * Data class để chứa coverage metrics
     */
    public static class CoverageMetrics {
        public final long totalStaff;
        public final long presentStaff;
        public final long absentStaff;
        public final long scheduledStaff;
        public final String shiftName;

        public CoverageMetrics(long totalStaff, long presentStaff, long absentStaff,
                               long scheduledStaff, String shiftName) {
            this.totalStaff = totalStaff;
            this.presentStaff = presentStaff;
            this.absentStaff = absentStaff;
            this.scheduledStaff = scheduledStaff;
            this.shiftName = shiftName;
        }
    }
}