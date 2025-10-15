package com.group1.swp.pizzario_swp391.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.group1.swp.pizzario_swp391.dto.reservation.ReservationCreateDTO;
import com.group1.swp.pizzario_swp391.dto.reservation.ReservationDTO;
import com.group1.swp.pizzario_swp391.entity.Reservation;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    
    @Mapping(source = "diningTable.id", target = "tableId")
    @Mapping(target = "tableName", expression = "java(\"Bàn #\" + reservation.getDiningTable().getId())")
    @Mapping(source = "name", target = "customerName")
    @Mapping(source = "phone", target = "customerPhone")
    @Mapping(target = "status", expression = "java(reservation.getStatus().name())")
    @Mapping(source = "note", target = "note")
    ReservationDTO toReservationDTO(Reservation reservation);

    @Mapping(target = "id", ignore = true) // để DB tự tăng
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "status", expression = "java(com.group1.swp.pizzario_swp391.entity.Reservation.Status.CONFIRMED)")
    @Mapping(target = "diningTable", ignore = true)
    @Mapping(source = "customerName", target = "name")
    @Mapping(source = "customerPhone", target = "phone")
    @Mapping(source = "note", target = "note")
    Reservation toReservationEntity(ReservationCreateDTO reservationCreateDTO);
}
