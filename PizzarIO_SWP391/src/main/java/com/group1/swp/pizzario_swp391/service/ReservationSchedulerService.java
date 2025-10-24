package com.group1.swp.pizzario_swp391.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.group1.swp.pizzario_swp391.event.reservation.AutoLockTableEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.group1.swp.pizzario_swp391.event.reservation.ReservationNoShowEvent;

/**
 * Service kiểm soát việc tự động khóa bàn sau 15p nếu khách không đến
 */
@Service
@Slf4j
public class ReservationSchedulerService {

    @Qualifier("taskScheduler")
    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>(); // Map để lưu những reservation cần thực hiện

    /**
     * Lên kế hoạch xử lý tự động khóa bàn trước thời điểm khách đặt
     */
    public void ScheduleAutoLockTable(Long reservationId, LocalDateTime startTime) {
        LocalDateTime executionTime = startTime.minusMinutes(15);

        // Nếu thời gian đã qua, không schedule
        if (executionTime.isBefore(LocalDateTime.now())) {
            log.warn("Reservation {} startTime đã gần, không schedule auto lock table", reservationId);
            return;
        }

        Instant scheduledInstant = executionTime.atZone(ZoneId.systemDefault()).toInstant();

        ScheduledFuture<?> scheduledTask = taskScheduler.schedule(
                () -> handleAutoLockTable(reservationId),
                scheduledInstant
        );

        scheduledTasks.put(reservationId, scheduledTask);
        log.info("Scheduled auto lock table for reservation {} at {}", reservationId, executionTime);
    }

    /**
     * Hủy kế hoạch xử lý tự động khóa bàn trước thời điểm khách đặt
     */
    public void cancelAutoLockTable(Long reservationId) {
        ScheduledFuture<?> task = scheduledTasks.remove(reservationId);
        if (task != null && !task.isDone()) {
            task.cancel(false);
            log.info("Cancelled auto lock table for reservation {}", reservationId);
        }
    }

    /**
     * Cập nhật kế hoạch xử lý tự động khóa bàn trước thời điểm khách đặt
     */
    public void updateAutoLockTable(Long reservationId, LocalDateTime startTime) {
        cancelAutoLockTable(reservationId);
        ScheduleAutoLockTable(reservationId, startTime);
    }

    /**
     * Logic xử lý tự động khóa bàn, được gọi tự động khi đến giờ
     */
    private void handleAutoLockTable(Long reservationId) {
        try {
            log.info("Executing auto lock table for reservation {}", reservationId);
            eventPublisher.publishEvent(new AutoLockTableEvent(this, reservationId));
            scheduledTasks.remove(reservationId);
        } catch (Exception e){
            log.error("Error executing auto lock table for reservation {}: {}", reservationId, e.getMessage());
        }
    }

    /**
     * Lên kế hoạch xử lý NO_SHOW cho reservation, Task sẽ chạy vào startTime + 15p
     */
    public void scheduleNoShowCheck(Long reservationId, LocalDateTime startTime) {
//        LocalDateTime executionTime = startTime.plusMinutes(15);
        LocalDateTime executionTime = startTime.plusSeconds(5);

        // Nếu thời gian đã qua, không schedule
        if (executionTime.isBefore(LocalDateTime.now())) {
            log.warn("Reservation {} startTime đã qua, không schedule NO_SHOW check", reservationId);
            return;
        }

        Instant scheduledInstant = executionTime.atZone(ZoneId.systemDefault()).toInstant();

        ScheduledFuture<?> scheduledTask = taskScheduler.schedule(
                () -> handleNoShow(reservationId),
                scheduledInstant
        );

        scheduledTasks.put(reservationId, scheduledTask);
        log.info("Scheduled NO_SHOW check for reservation {} at {}", reservationId, executionTime);
    }

    /**
     * Hủy scheduled task khi reservation bị cancel hoặc khách đã đến
     */
    public void cancelNoShowCheck(Long reservationId) {
        ScheduledFuture<?> task = scheduledTasks.remove(reservationId);
        if (task != null && !task.isDone()) {
            task.cancel(false);
            log.info("Cancelled NO_SHOW check for reservation {}", reservationId);
        }
    }

    /**
     * Update khi reservation được chỉnh sửa thời gian
     */
    public void updateNoShowCheck(Long reservationId, LocalDateTime startTime) {
        cancelNoShowCheck(reservationId);
        scheduleNoShowCheck(reservationId, startTime);
    }

    /**
     * Logic xử lý NO_SHOW check, được gọi tự động khi đến giờ
     */
    private void handleNoShow(Long reservationId) {
        try {
            log.info("Executing NO_SHOW check for reservation {}", reservationId);
            eventPublisher.publishEvent(new ReservationNoShowEvent(this, reservationId));
            scheduledTasks.remove(reservationId);
        } catch (Exception e){
            log.error("Error executing NO_SHOW check for reservation {}: {}", reservationId, e.getMessage());
        }
    }

}
