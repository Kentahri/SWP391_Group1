package com.group1.swp.pizzario_swp391.dto.table;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.group1.swp.pizzario_swp391.entity.DiningTable.TableCondition;
import com.group1.swp.pizzario_swp391.entity.DiningTable.TableStatus;
import com.group1.swp.pizzario_swp391.entity.DiningTable.TableType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableResponseDTO {
    int id;
    TableType tableType;
    TableStatus tableStatus;
    TableCondition tableCondition;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    
    // Formatted fields for display
    public String getCreatedAtFormatted() {
        return createdAt != null ? createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }
    
    public String getUpdatedAtFormatted() {
        return updatedAt != null ? updatedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }
    
    public String getTableTypeText() {
        if (tableType == null) return "";
        switch (tableType) {
            case SMALL: return "Bàn nhỏ";
            case BIG: return "Bàn lớn";
            default: return tableType.toString();
        }
    }
    
    public String getTableStatusText() {
        if (tableStatus == null) return "";
        switch (tableStatus) {
            case AVAILABLE: return "Trống";
            case OCCUPIED: return "Đang sử dụng";
            case RESERVED: return "Đã đặt";
            case WAITING_PAYMENT: return "Chờ thanh toán";
            default: return tableStatus.toString();
        }
    }
    
    public String getTableConditionText() {
        if (tableCondition == null) return "";
        switch (tableCondition) {
            case NEW: return "Mới";
            case GOOD: return "Tốt";
            case WORN: return "Cũ";
            case DAMAGED: return "Hư hỏng";
            case UNDER_REPAIR: return "Đang sửa chữa";
            case RETIRED: return "Ngừng sử dụng";
            default: return tableCondition.toString();
        }
    }
}
