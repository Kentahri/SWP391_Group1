package com.group1.swp.pizzario_swp391.event.reservation;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class AutoLockTableEvent extends ApplicationEvent {

    private final Long reservationId;
    private final LocalDateTime triggeredAt;

    public AutoLockTableEvent(Object source, Long reservationId) {
        super(source);
        this.reservationId = reservationId;
        this.triggeredAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("AutoLockTableEvent{reservationId=%d, triggeredAt=%s}",
                           reservationId, triggeredAt);
    }
}
