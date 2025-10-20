package com.group1.swp.pizzario_swp391.event.reservation;

import java.time.LocalDateTime;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event được publish khi cần xử lý NO_SHOW cho reservation.
 * Được trigger tự động bởi TaskScheduler sau startTime + 15 phút.
 */
@Getter
public class ReservationNoShowEvent extends ApplicationEvent {
    
    private final Long reservationId;
    private final LocalDateTime triggeredAt;
    
    public ReservationNoShowEvent(Object source, Long reservationId) {
        super(source);
        this.reservationId = reservationId;
        this.triggeredAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("ReservationNoShowEvent{reservationId=%d, triggeredAt=%s}", 
                           reservationId, triggeredAt);
    }
}


