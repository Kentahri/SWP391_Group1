package com.group1.swp.pizzario_swp391.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracker để quản lý các reservation đang chờ bàn trống
 * Mỗi bàn chỉ có thể có 1 reservation đang chờ tại một thời điểm
 */
@Component
@Slf4j
public class PendingReservationTracker {
    // tableId -> reservationId (chỉ 1 reservation cho mỗi bàn)
    private final Map<Integer, Long> pendingReservations = new ConcurrentHashMap<>();

    /**
     * Thêm reservation vào pending list cho bàn
     * Nếu đã có reservation khác đang chờ, sẽ bị ghi đè (nhưng thực tế không nên xảy ra)
     */
    public void addPendingReservation(int tableId, long reservationId) {
        Long previousReservation = pendingReservations.put(tableId, reservationId);

        if (previousReservation != null) {
            log.warn("⚠️ Bàn {} đã có reservation {} đang chờ, bị ghi đè bởi reservation {}",
                    tableId, previousReservation, reservationId);
        }

        log.info("Added pending reservation {} for table {}", reservationId, tableId);
    }

    /**
     * Lấy reservation đang chờ cho bàn (nếu có)
     */
    public Long getPendingReservation(int tableId) {
        return pendingReservations.get(tableId);
    }

    /**
     * Remove reservation khỏi pending list
     */
    public void removePendingReservation(int tableId, long reservationId) {
        Long currentReservation = pendingReservations.get(tableId);

        if (currentReservation != null && currentReservation.equals(reservationId)) {
            pendingReservations.remove(tableId);
            log.info("Removed pending reservation {} for table {}", reservationId, tableId);
        } else if (currentReservation != null) {
            log.warn("⚠️ Attempt to remove reservation {} from table {}, but current pending is {}",
                    reservationId, tableId, currentReservation);
        }
    }

    /**
     * Kiểm tra xem bàn có reservation đang chờ không
     */
    public boolean hasPendingReservation(int tableId) {
        return pendingReservations.containsKey(tableId);
    }


}
