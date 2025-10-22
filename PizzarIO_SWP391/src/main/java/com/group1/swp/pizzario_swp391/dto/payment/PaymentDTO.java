package com.group1.swp.pizzario_swp391.dto.payment;

import com.group1.swp.pizzario_swp391.dto.voucher.VoucherDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentDTO {
    Long id;                 // sẽ giữ id của Order (sử dụng Order làm nguồn dữ liệu)
    Long orderId;            // nếu bạn muốn phân biệt, có thể map từ order.id
    Double orderTotal;       // map từ Order.totalPrice (hoặc tên tương ứng)
    Long membershipId;       // map từ order.membership.id
    Integer membershipPoints; // điểm hiện có (set trong service)
    Integer pointsUsed;      // điểm đã dùng cho lần thanh toán
    Long appliedVoucherId;   // voucher được áp dụng (map từ order.voucher.id)
    List<VoucherDTO> availableVouchers; // để view hiển thị danh sách voucher
    String paymentStatus;    // map từ order.paymentStatus
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}