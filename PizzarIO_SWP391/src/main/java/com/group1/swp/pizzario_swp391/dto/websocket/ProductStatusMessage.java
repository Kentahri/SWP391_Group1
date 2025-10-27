package com.group1.swp.pizzario_swp391.dto.websocket;

import com.group1.swp.pizzario_swp391.dto.product.ProductWebSocketDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusMessage {
    private MessageType type;
    private ProductWebSocketDTO product; // Gửi toàn bộ object Product
    private String updatedBy;
    private LocalDateTime timestamp;

    public enum MessageType {
        PRODUCT_CREATED, // Thêm sản phẩm mới
        PRODUCT_UPDATED, // Cập nhật sản phẩm
        PRODUCT_TOGGLED
    }
}