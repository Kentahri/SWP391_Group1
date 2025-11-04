package com.group1.swp.pizzario_swp391.mapper;

import com.group1.swp.pizzario_swp391.dto.data_analytics.StaffShiftCalendarDTO;
import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftDTO;
import com.group1.swp.pizzario_swp391.dto.staffshift.StaffShiftResponseDTO;
import com.group1.swp.pizzario_swp391.entity.StaffShift;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// removed unused import

@Mapper(componentModel = "spring")
public interface StaffShiftMapper {

    // Convert Entity to DTO
    @Mapping(target = "staffId", source = "staff.id")
    @Mapping(target = "shiftId", source = "shift.id")
    @Mapping(target = "workDate", source = "workDate")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "checkIn", source = "checkIn")
    @Mapping(target = "checkOut", source = "checkOut")
    @Mapping(target = "hourlyWage", source = "hourlyWage")
    @Mapping(target = "note", source = "note")
    @Mapping(target = "penaltyPercent", source = "penaltyPercent")
    StaffShiftDTO toStaffShiftDTO(StaffShift staffShift);

    // Convert DTO to Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "staff", ignore = true)
    @Mapping(target = "shift", ignore = true)
    @Mapping(target = "workDate", source = "workDate")
    @Mapping(target = "note", source = "note")
    @Mapping(target = "penaltyPercent", source = "penaltyPercent")
    StaffShift toStaffShift(StaffShiftDTO staffShiftDTO);

    // Update Entity from DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "staff", ignore = true)
    @Mapping(target = "shift", ignore = true)
    @Mapping(target = "workDate", source = "workDate")
    @Mapping(target = "note", source = "note")
    @Mapping(target = "penaltyPercent", source = "penaltyPercent")
    void updateStaffShift(@MappingTarget StaffShift staffShift, StaffShiftDTO staffShiftDTO);

    // NEW: Convert Entity to Response DTO
    @Mapping(target = "staffShiftId", source = "id")
    @Mapping(target = "workDate", source = "workDate")
    @Mapping(target = "shiftName", source = "shift.shiftName")
    @Mapping(target = "startTime", source = "shift.startTime")
    @Mapping(target = "endTime", source = "shift.endTime")
    @Mapping(target = "staffName", source = "staff.name")
    @Mapping(target = "staffId", source = "staff.id")
    @Mapping(target = "shiftStatus", source = "status")
    @Mapping(target = "checkIn", source = "checkIn")
    @Mapping(target = "checkOut", source = "checkOut")
    @Mapping(target = "hourlyWage", source = "shift.salaryPerShift")
    @Mapping(target = "note", source = "note")
    @Mapping(target = "penaltyPercent", source = "penaltyPercent")
    StaffShiftResponseDTO toResponseDTO(StaffShift staffShift);

    // NEW: Convert Response DTO to Calendar DTO
    @Mapping(target = "id", source = "staffShiftId")
    @Mapping(target = "shiftName", source = "shiftName")
    @Mapping(target = "shiftType", source = "startTime", qualifiedByName = "determineShiftType")
    @Mapping(target = "staffName", source = "staffName")
    @Mapping(target = "timeRange", source = "source", qualifiedByName = "formatTimeRange")
    @Mapping(target = "statusText", source = "shiftStatus", qualifiedByName = "getStatusText")
    @Mapping(target = "statusClass", source = "shiftStatus", qualifiedByName = "getStatusClass")
    @Mapping(target = "totalWage", source = "source", qualifiedByName = "calculateTotalWage")
    @Mapping(target = "note", source = "note")
    @Mapping(target = "status", source = "shiftStatus")
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "formatEndTime")
    @Mapping(target = "workDate", source = "workDate")
    StaffShiftCalendarDTO toCalendarDTO(StaffShiftResponseDTO source);

    @Named("determineShiftType")
    default String determineShiftType(LocalDateTime startTime) {
        if (startTime == null)
            return "unknown";
        int hour = startTime.getHour();
        if (hour < 12)
            return "morning";
        else if (hour < 18)
            return "afternoon";
        else
            return "evening";
    }

    @Named("formatTimeRange")
    default String formatTimeRange(StaffShiftResponseDTO dto) {
        if (dto.getStartTime() == null || dto.getEndTime() == null)
            return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return dto.getStartTime().format(formatter) + " - " + dto.getEndTime().format(formatter);
    }

    @Named("formatEndTime")
    default String formatEndTime(LocalDateTime endTime) {
        if (endTime == null)
            return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return endTime.format(formatter);
    }

    @Named("getStatusText")
    default String getStatusText(String status) {
        if (status == null)
            return "Không xác định";
        return switch (status) {
            case "COMPLETED" -> "Hoàn thành";
            case "SCHEDULED" -> "Đã lên lịch";
            case "PRESENT" -> "Có mặt";
            case "LATE" -> "Đi muộn";
            case "ABSENT" -> "Vắng mặt";
            case "LEFT_EARLY" -> "Về sớm";
            case "NOT_CHECKOUT" -> "Chưa checkout";
            default -> "Không xác định";
        };
    }

    @Named("getStatusClass")
    default String getStatusClass(String status) {
        if (status == null)
            return "unknown";
        return switch (status) {
            case "COMPLETED" -> "completed";
            case "SCHEDULED" -> "scheduled";
            case "PRESENT" -> "present";
            case "LATE" -> "late";
            case "ABSENT" -> "absent";
            case "LEFT_EARLY" -> "left-early";
            case "NOT_CHECKOUT" -> "not_checkout";
            default -> "unknown";
        };
    }

    @Named("calculateTotalWage")
    default Double calculateTotalWage(StaffShiftResponseDTO shift) {
        if (shift.getShiftStatus() == null) return 0.0;
        
        // Case 1: COMPLETED - Fixed salary with penalty
        if ("COMPLETED".equals(shift.getShiftStatus())) {
            double fixedSalary = shift.getHourlyWage().doubleValue() * Duration.between(shift.getStartTime(), shift.getEndTime()).toHours();
            return fixedSalary * (100 - shift.getPenaltyPercent()) / 100;
        }
        
        // Case 2: LEFT_EARLY - Hourly rate based on actual hours worked
        if ("LEFT_EARLY".equals(shift.getShiftStatus()) 
            && shift.getCheckIn() != null && shift.getCheckOut() != null) {
            
            // Calculate total shift hours (for hourly rate calculation)
            long totalShiftHours = Duration.between(shift.getStartTime(), shift.getEndTime()).toHours();
            if (totalShiftHours == 0) totalShiftHours = 1; // Prevent division by zero
            
            // Calculate hourly rate from fixed salary
            double hourlyRate = shift.getHourlyWage().doubleValue() / totalShiftHours;
            
            // Calculate actual hours worked
            long actualHours = Duration.between(shift.getCheckIn(), shift.getCheckOut()).toHours();
            
            // Total wage = hourly rate × actual hours × penalty adjustment
            return hourlyRate * actualHours * (100 - shift.getPenaltyPercent()) / 100;
        }

        if ("LATE".equals(shift.getShiftStatus())) {
            double fixedSalary = shift.getHourlyWage().doubleValue() * Duration.between(shift.getStartTime(), shift.getEndTime()).toHours();
            return fixedSalary * (100 - shift.getPenaltyPercent()) / 100;
        }
        
        return 0.0;
    }
}