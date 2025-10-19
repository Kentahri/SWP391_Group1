package com.group1.swp.pizzario_swp391.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ReservationSchedulerService {

    TaskScheduler taskScheduler;
    ReservationService reservationService;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>(); // Map để lưu những reservation cần thực hiện

    /**
     * Lên kế hoạch xử lý NO_SHOW cho reservation, Task sẽ chạy vào startTime + 15p
     *
     * @param reservationId
     * @param startTime
     */
    public void scheduleNoShowCheck(Long reservationId, LocalDateTime startTime) {
        LocalDateTime executionTime = startTime.plusMinutes(15);

        Instant scheduledInstant = executionTime.atZone(ZoneId.systemDefault()).toInstant();

        ScheduledFuture<?> scheduledTask = taskScheduler.schedule(
                () -> handleNoShow(reservationId),
                scheduledInstant
        );

        scheduledTasks.put(reservationId, scheduledTask);
        log.info("Scheduled NO_SHOW check for reservation {} at {}", reservationId, executionTime);
    }

    /**
     * Hủy scheduled task khi reservation bị cancel
     *
     * @param reservationId
     */
    public void cancelNoShowCheck(Long reservationId){
        scheduledTasks.remove(reservationId);
    }

    /**
     * Update khi reservation được chỉnh sửa thời gian
     *
     * @param reservationId
     * @param startTime
     */
    public void updateNoShowCheck(Long reservationId, LocalDateTime startTime) {
        cancelNoShowCheck(reservationId);
        scheduleNoShowCheck(reservationId, startTime);
    }

    /**
     * Logic xử lý NO_SHOW check, được gọi tự động khi đến giờ
     *
     * @param reservationId
     */
    private void handleNoShow(Long reservationId) {
        try {
            log.info("Executing NO_SHOW check for reservation {}", reservationId);
//            reservationService
            scheduledTasks.remove(reservationId);
        } catch (Exception e){
            log.error("Error executing NO_SHOW check for reservation {}: {}", reservationId, e.getMessage());
        }
    }

}
