package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.StaffShift;
import com.group1.swp.pizzario_swp391.event.staff.StaffAbsentEvent;
import com.group1.swp.pizzario_swp391.event.staff.StaffShiftCreatedEvent;
import com.group1.swp.pizzario_swp391.event.staff.StaffShiftUpdatedEvent;
import com.group1.swp.pizzario_swp391.repository.StaffShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffScheduleService {

    private final StaffShiftRepository staffShiftRepository;

    // ✅ FIX: Inject TaskScheduler riêng với @Qualifier
    @Qualifier("staffTaskScheduler")
    private final TaskScheduler taskScheduler;

    private final ApplicationEventPublisher eventPublisher;
    private final ShiftCoverageService shiftCoverageService;

    private final Map<Integer, ScheduledFuture<?>> absentCheckTasks = new ConcurrentHashMap<>();
    private final Map<Integer, ScheduledFuture<?>> autoCompleteTasks = new ConcurrentHashMap<>();

    @EventListener
    public void onStaffShiftCreated(StaffShiftCreatedEvent event) {
        StaffShift staffShift = event.getStaffShift();
        log.info("🎯 EVENT LISTENER: Received StaffShiftCreatedEvent for shift ID: {}", staffShift.getId());
        log.info("🎯 EVENT LISTENER: Staff: {}, Date: {}, Status: {}",
                staffShift.getStaff().getName(),
                staffShift.getWorkDate(),
                staffShift.getStatus());

        log.info("🎯 EVENT LISTENER: Scheduling absent check for shift ID: {}", staffShift.getId());
        scheduleAbsentCheck(staffShift);

        log.info("🎯 EVENT LISTENER: Scheduling auto-complete check for shift ID: {}", staffShift.getId());
        scheduleAutoCompleteCheck(staffShift);

        log.info("🎯 EVENT LISTENER: Scheduling coverage check for shift ID: {}", staffShift.getId());
        scheduleShiftCoverageCheck(staffShift);

        log.info("🎯 EVENT LISTENER: Completed processing StaffShiftCreatedEvent for shift ID: {}", staffShift.getId());
    }

    @EventListener
    public void onStaffShiftUpdated(StaffShiftUpdatedEvent event) {
        StaffShift staffShift = event.getStaffShift();
        log.info("🎯 EVENT LISTENER: Received StaffShiftUpdatedEvent for shift ID: {} - status: {}",
                staffShift.getId(), staffShift.getStatus());
        log.info("🎯 EVENT LISTENER: Needs reschedule: {}", event.isNeedsReschedule());

        if (event.isNeedsReschedule()) {
            cancelAbsentCheck(staffShift.getId());
            cancelAutoCompleteCheck(staffShift.getId());

            if (staffShift.getStatus() == StaffShift.Status.SCHEDULED) {
                scheduleAbsentCheck(staffShift);
                scheduleAutoCompleteCheck(staffShift);
                scheduleShiftCoverageCheck(staffShift);
            } else if (staffShift.getStatus() == StaffShift.Status.PRESENT ||
                    staffShift.getStatus() == StaffShift.Status.LATE) {
                scheduleAutoCompleteCheck(staffShift);
            }
        }
    }

    /**
     * ✅ FIX: Schedule absent check với capture ID sớm
     */
    public void scheduleAbsentCheck(StaffShift staffShift) {
        if (staffShift.getStatus() != StaffShift.Status.SCHEDULED || staffShift.getCheckIn() != null) {
            log.debug("Skip scheduling absent check for shift {} - status: {}, checkIn: {}",
                    staffShift.getId(), staffShift.getStatus(), staffShift.getCheckIn());
            return;
        }

        LocalDateTime shiftStart = staffShift.getWorkDate()
                .atTime(staffShift.getShift().getStartTime().toLocalTime());
        LocalDateTime checkTime = shiftStart.plusMinutes(2);

        if (checkTime.isBefore(LocalDateTime.now())) {
            log.warn("Skip scheduling absent check for shift {} - deadline already passed: {}",
                    staffShift.getId(), checkTime);
            return;
        }

        // ✅ FIX: Capture values EARLY to avoid race condition
        Integer staffShiftId = staffShift.getId();
        String staffName = staffShift.getStaff().getName();
        String shiftName = staffShift.getShift().getShiftName().name();

        Instant scheduledInstant = checkTime.atZone(ZoneId.systemDefault()).toInstant();

        ScheduledFuture<?> task = taskScheduler.schedule(
                () -> handleAbsentCheck(staffShiftId), // Use captured ID
                scheduledInstant);

        absentCheckTasks.put(staffShiftId, task);
        log.info("✅ Scheduled absent check for staff shift {} at {} (staff: {}, shift: {})",
                staffShiftId, checkTime, staffName, shiftName);
    }

    public void scheduleAutoCompleteCheck(StaffShift staffShift) {
        if (staffShift.getStatus() != StaffShift.Status.SCHEDULED &&
                staffShift.getStatus() != StaffShift.Status.PRESENT) {
            return;
        }

        LocalDate nextDay = staffShift.getWorkDate().plusDays(1);
        LocalDateTime autoCompleteTime = nextDay.atTime(0, 30);
        Instant scheduledInstant = autoCompleteTime.atZone(ZoneId.systemDefault()).toInstant();

        ScheduledFuture<?> task = taskScheduler.schedule(
                () -> handleAutoComplete(staffShift.getId()),
                scheduledInstant);

        autoCompleteTasks.put(staffShift.getId(), task);
        log.info("Scheduled auto-complete check for staff shift {} at {}",
                staffShift.getId(), autoCompleteTime);
    }

    public void scheduleShiftCoverageCheck(StaffShift staffShift) {
        LocalDateTime shiftStart = staffShift.getWorkDate()
                .atTime(staffShift.getShift().getStartTime().toLocalTime());
        LocalDateTime coverageCheckTime = shiftStart.plusMinutes(10);

        if (coverageCheckTime.isBefore(LocalDateTime.now())) {
            log.debug("Skip scheduling coverage check for shift {} - time already passed: {}",
                    staffShift.getId(), coverageCheckTime);
            return;
        }

        Instant scheduledInstant = coverageCheckTime.atZone(ZoneId.systemDefault()).toInstant();

        taskScheduler.schedule(
                () -> handleShiftCoverageCheck(staffShift.getShift().getId(), staffShift.getWorkDate()),
                scheduledInstant);

        log.info("Scheduled coverage check for shift {} on {} at {}",
                staffShift.getShift().getShiftName().name(), staffShift.getWorkDate(), coverageCheckTime);
    }

    /**
     * ✅ FIX: Better error handling
     */
    private void handleAbsentCheck(Integer staffShiftId) {
        try {
            log.info("🎯 Executing absent check for staff shift {}", staffShiftId);
            eventPublisher.publishEvent(new StaffAbsentEvent(this, staffShiftId));
            absentCheckTasks.remove(staffShiftId);
            log.info("✅ Absent check event published for shift {}", staffShiftId);
        } catch (Exception e) {
            log.error("❌ Error executing absent check for shift {}: {}", staffShiftId, e.getMessage(), e);
            absentCheckTasks.remove(staffShiftId);
        }
    }

    private void handleAutoComplete(Integer staffShiftId) {
        try {
            log.info("Executing auto-complete check for staff shift {}", staffShiftId);

            Optional<StaffShift> shiftOpt = staffShiftRepository.findById(staffShiftId);
            if (shiftOpt.isPresent()) {
                StaffShift ss = shiftOpt.get();

                if (ss.getCheckIn() != null && ss.getCheckOut() == null) {
                    ss.setCheckOut(ss.getShift().getEndTime());
                    ss.setStatus(StaffShift.Status.COMPLETED);
                    String noteDetail = "AUTO-COMPLETED - Quên checkout\n";
                    ss.setNote((ss.getNote() != null ? ss.getNote() : "") + noteDetail);
                    staffShiftRepository.save(ss);

                    log.warn("Auto-completed shift for staff {} - forgot to logout",
                            ss.getStaff().getName());
                }
            }

            autoCompleteTasks.remove(staffShiftId);
        } catch (Exception e) {
            log.error("Error executing auto-complete for shift {}: {}", staffShiftId, e.getMessage(), e);
        }
    }

    private void handleShiftCoverageCheck(Integer shiftId, LocalDate workDate) {
        try {
            log.info("🔍 Checking coverage for shift ID {} on {}", shiftId, workDate);

            List<StaffShift> allStaffShifts = shiftCoverageService.getAllStaffForShift(shiftId, workDate);

            if (allStaffShifts.isEmpty()) {
                log.warn("No staff found for shift {} on {}", shiftId, workDate);
                return;
            }

            ShiftCoverageService.CoverageMetrics metrics = shiftCoverageService
                    .calculateCoverageMetrics(allStaffShifts);
            shiftCoverageService.evaluateCoverage(metrics, workDate);
            shiftCoverageService.publishCoverageEvent(shiftId, workDate, allStaffShifts);

        } catch (Exception e) {
            log.error("Error checking shift coverage for shift {} on {}: {}",
                    shiftId, workDate, e.getMessage(), e);
        }
    }

    /**
     * ✅ FIX: Sử dụng pessimistic lock để tránh race condition
     */
    @EventListener
    @Transactional
    public void onStaffAbsentEvent(StaffAbsentEvent event) {
        Integer staffShiftId = event.getStaffShiftId();
        log.info("🎯 Processing StaffAbsentEvent for shift ID: {}", staffShiftId);

        try {
            // ✅ FIX: Sử dụng pessimistic lock
            Optional<StaffShift> shiftOpt = staffShiftRepository.findByIdWithLock(staffShiftId);

            if (shiftOpt.isEmpty()) {
                log.warn("⚠️ StaffShift not found for ID: {}", staffShiftId);
                return;
            }

            StaffShift ss = shiftOpt.get();

            if (ss.getStatus() == StaffShift.Status.SCHEDULED && ss.getCheckIn() == null) {
                ss.setStatus(StaffShift.Status.ABSENT);
                ss.setPenaltyPercent(100);
                String noteDetail = String.format("ABSENT - Không check-in (Deadline: %s)\n",
                        event.getTriggeredAt().toLocalTime());
                ss.setNote((ss.getNote() != null ? ss.getNote() : "") + noteDetail);

                staffShiftRepository.save(ss);
                staffShiftRepository.flush();

                log.warn("✅ ABSENT marked for staff {} - shift {} on {}",
                        ss.getStaff().getName(),
                        ss.getShift().getShiftName().name(),
                        ss.getWorkDate());
            } else {
                log.info("⏭️ Skip marking ABSENT for shift {} - status: {}, checkIn: {}",
                        staffShiftId, ss.getStatus(), ss.getCheckIn());
            }

        } catch (Exception e) {
            log.error("❌ Error processing StaffAbsentEvent for shift {}: {}",
                    staffShiftId, e.getMessage(), e);
            log.error("Stack trace:", e);
        }
    }

    public void cancelAbsentCheck(Integer staffShiftId) {
        ScheduledFuture<?> task = absentCheckTasks.remove(staffShiftId);
        if (task != null && !task.isDone()) {
            task.cancel(false);
            log.info("Cancelled absent check for staff shift {}", staffShiftId);
        }
    }

    public void cancelAutoCompleteCheck(Integer staffShiftId) {
        ScheduledFuture<?> task = autoCompleteTasks.remove(staffShiftId);
        if (task != null && !task.isDone()) {
            task.cancel(false);
            log.info("Cancelled auto-complete check for staff shift {}", staffShiftId);
        }
    }

    public void cancelAllTasks(Integer staffShiftId) {
        cancelAbsentCheck(staffShiftId);
        cancelAutoCompleteCheck(staffShiftId);
    }

    @Scheduled(cron = "0 20 8 * * *", zone = "Asia/Bangkok")
    @Transactional
    public void dailyCleanup() {
        log.info("🔄 Daily cleanup: Checking for missed shifts and coverage...");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime now = LocalDateTime.now();

        var yesterdayShifts = staffShiftRepository.findByWorkDateBetween(yesterday, yesterday);

        Map<String, List<StaffShift>> shiftsByKey = yesterdayShifts.stream()
                .collect(Collectors.groupingBy(ss -> ss.getShift().getId() + "_" + ss.getWorkDate()));

        for (Map.Entry<String, List<StaffShift>> entry : shiftsByKey.entrySet()) {
            List<StaffShift> shiftStaff = entry.getValue();
            String shiftName = shiftStaff.get(0).getShift().getShiftName().name();
            LocalDate workDate = shiftStaff.get(0).getWorkDate();

            ShiftCoverageService.CoverageMetrics metrics = shiftCoverageService.calculateCoverageMetrics(shiftStaff);
            shiftCoverageService.evaluateCoverage(metrics, workDate);
        }

        int processedCount = 0;
        for (StaffShift ss : yesterdayShifts) {
            if (ss.getStatus() == StaffShift.Status.SCHEDULED && ss.getCheckIn() == null) {
                ss.setStatus(StaffShift.Status.ABSENT);
                ss.setPenaltyPercent(100);
                String noteDetail = String.format("ABSENT - Daily cleanup (Processed: %s)\n",
                        now.toLocalTime());
                ss.setNote((ss.getNote() != null ? ss.getNote() : "") + noteDetail);
                staffShiftRepository.save(ss);
                processedCount++;

                log.warn("Daily cleanup: Marked ABSENT for staff {} - shift {} on {}",
                        ss.getStaff().getName(),
                        ss.getShift().getShiftName().name(),
                        ss.getWorkDate());
            }

            if (ss.getCheckIn() != null && ss.getCheckOut() == null) {
                ss.setCheckOut(ss.getShift().getEndTime());
                ss.setStatus(StaffShift.Status.COMPLETED);
                String noteDetail = "AUTO-COMPLETED - Daily cleanup\n";
                ss.setNote((ss.getNote() != null ? ss.getNote() : "") + noteDetail);
                staffShiftRepository.save(ss);
                processedCount++;

                log.warn("Daily cleanup: Auto-completed shift for staff {} - forgot to logout",
                        ss.getStaff().getName());
            }
        }

        if (processedCount > 0) {
            log.info("Daily cleanup: Processed {} shifts", processedCount);
        }

        absentCheckTasks.entrySet().removeIf(entry -> entry.getValue().isDone());
        autoCompleteTasks.entrySet().removeIf(entry -> entry.getValue().isDone());

        log.info("Daily cleanup completed. Active tasks: absent={}, autoComplete={}",
                absentCheckTasks.size(), autoCompleteTasks.size());
    }
}