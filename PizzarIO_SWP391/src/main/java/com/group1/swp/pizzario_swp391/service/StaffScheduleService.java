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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffScheduleService {

    private final StaffShiftRepository staffShiftRepository;

    // ✅ FIX: Inject TaskScheduler riêng với @Qualifier
    @Qualifier("staffTaskScheduler")
    private final TaskScheduler taskScheduler;

    private final ApplicationEventPublisher eventPublisher;

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
            } else if (staffShift.getStatus() == StaffShift.Status.PRESENT ||
                    staffShift.getStatus() == StaffShift.Status.LATE) {
                // ✅ CHỈ SCHEDULE auto-complete khi đã check-in
                log.info("✅ Check-in detected - scheduling auto-complete only for shift {}", staffShift.getId());
                scheduleAutoCompleteCheck(staffShift);
            } else if (staffShift.getStatus() == StaffShift.Status.COMPLETED ||
                    staffShift.getStatus() == StaffShift.Status.LEFT_EARLY) {
                // ✅ ĐÃ CHECK-OUT - không cần schedule gì nữa
                log.info("✅ Check-out detected - no need to schedule tasks for shift {}", staffShift.getId());
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
//        LocalDateTime checkTime = shiftStart.plusMinutes(1);
//        LocalDateTime checkTime = shiftStart.plusMinutes(61);
        LocalDateTime checkTime = shiftStart.plusHours(1);



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

        log.info("🔍 scheduleAutoCompleteCheck called for shift ID: {}, status: {}",
                staffShift.getId(), staffShift.getStatus());

        // ✅ SỬA: Thêm LATE status
        if (staffShift.getStatus() != StaffShift.Status.SCHEDULED &&
                staffShift.getStatus() != StaffShift.Status.PRESENT &&
                staffShift.getStatus() != StaffShift.Status.LATE) {
            log.warn("❌ Skip scheduling auto-complete check for shift {} - invalid status: {}",
                    staffShift.getId(), staffShift.getStatus());
            return;
        }

        // ✅ NEW: Schedule NOT_CHECKOUT check 2 minutes after shift end time (for testing, should be 45 minutes in production)
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime now = LocalDateTime.now(zoneId);

        LocalDateTime shiftEnd = staffShift.getWorkDate()
                .atTime(staffShift.getShift().getEndTime().toLocalTime());
//        LocalDateTime autoCompleteTime = shiftEnd.plusMinutes(1);
        LocalDateTime autoCompleteTime = shiftEnd.plusMinutes(45);


        if (autoCompleteTime.isBefore(now)) {
            log.warn("⚠️ Skip scheduling auto-complete check for shift {} - time already passed. ShiftEnd: {}, Scheduled: {}, Now: {}",
                    staffShift.getId(), shiftEnd, autoCompleteTime, now);
            return;
        }

        Instant scheduledInstant = autoCompleteTime.atZone(zoneId).toInstant();

        Integer staffShiftId = staffShift.getId();  // Capture ngay
        String staffName = staffShift.getStaff().getName();

        ScheduledFuture<?> task = taskScheduler.schedule(
                () -> handleAutoComplete(staffShiftId),
                scheduledInstant);

        autoCompleteTasks.put(staffShift.getId(), task);
        log.info("✅ Scheduled NOT_CHECKOUT check for staff shift {} at {} (Now: {}, Instant: {})",
                staffShift.getId(), autoCompleteTime, now, scheduledInstant);
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
            log.info("🚀🚀🚀 EXECUTING NOT_CHECKOUT CHECK FOR STAFF SHIFT {} 🚀🚀🚀", staffShiftId);

            Optional<StaffShift> shiftOpt = staffShiftRepository.findById(staffShiftId);
            if (shiftOpt.isEmpty()) {
                log.error("❌ StaffShift not found for ID: {}", staffShiftId);
                return;
            }

            StaffShift ss = shiftOpt.get();
            log.info("📋 Current shift status: {}, checkIn: {}, checkOut: {}",
                    ss.getStatus(), ss.getCheckIn(), ss.getCheckOut());

            // ✅ NEW: Mark as NOT_CHECKOUT instead of COMPLETED if staff didn't checkout
            if (ss.getCheckIn() != null && ss.getCheckOut() == null) {
                log.info("✅ Conditions met - marking as NOT_CHECKOUT");
                ss.setStatus(StaffShift.Status.NOT_CHECKOUT);
                ss.setPenaltyPercent(0); // No penalty, but requires explanation later
                String noteDetail = "NOT_CHECKOUT - Quên checkout sau 45 phút\n";
                ss.setNote((ss.getNote() != null ? ss.getNote() : "") + noteDetail);
                staffShiftRepository.save(ss);

                log.warn("✅ Successfully marked NOT_CHECKOUT for staff {} - shift ID: {}",
                        ss.getStaff().getName(), staffShiftId);
            } else {
                log.info("⏭️ Skipping NOT_CHECKOUT - checkIn: {}, checkOut: {}",
                        ss.getCheckIn(), ss.getCheckOut());
            }

            autoCompleteTasks.remove(staffShiftId);
        } catch (Exception e) {
            log.error(" Error executing NOT_CHECKOUT check for shift {}: {}",
                    staffShiftId, e.getMessage(), e);
            e.printStackTrace();
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

    @Scheduled(cron = "0 50 4 * * *", zone = "Asia/Bangkok")
    @Transactional
    public void dailyCleanup() {
        log.info("🔄 Daily cleanup: Checking for missed shifts...");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime now = LocalDateTime.now();

        var yesterdayShifts = staffShiftRepository.findByWorkDateBetween(yesterday, yesterday);

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

            // ✅ NEW: Mark as NOT_CHECKOUT in daily cleanup instead of auto-completing
            if (ss.getCheckIn() != null && ss.getCheckOut() == null &&
                    ss.getStatus() != StaffShift.Status.NOT_CHECKOUT) {
                ss.setStatus(StaffShift.Status.NOT_CHECKOUT);
                ss.setPenaltyPercent(0);
                String noteDetail = "NOT_CHECKOUT - Daily cleanup\n";
                ss.setNote((ss.getNote() != null ? ss.getNote() : "") + noteDetail);
                staffShiftRepository.save(ss);
                processedCount++;

                log.warn("Daily cleanup: Marked NOT_CHECKOUT for staff {} - forgot to checkout",
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