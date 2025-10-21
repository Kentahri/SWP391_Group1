package com.group1.swp.pizzario_swp391.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponseDTO {
    
    private boolean success;
    private String message;
    private ReservationDTO reservation;
    
    public static ReservationResponseDTO success(String message, ReservationDTO reservation) {
        return new ReservationResponseDTO(true, message, reservation);
    }
    
    public static ReservationResponseDTO error(String message) {
        return new ReservationResponseDTO(false, message, null);
    }
}

