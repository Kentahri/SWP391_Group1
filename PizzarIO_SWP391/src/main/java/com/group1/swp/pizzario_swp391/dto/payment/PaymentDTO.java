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
    Double orderTotal;       // map từ Order.totalPrice
    Double originalTotal;    // tổng tiền gốc trước khi áp dụng voucher
    Double discountAmount;   // số tiền được giảm từ voucher
    Long membershipId;       // map từ order.membership.id
    Integer membershipPoints; // điểm hiện có của membership
    Integer pointsUsed;      // điểm đã dùng cho lần thanh toán này
    Long appliedVoucherId;   // voucher được áp dụng (map từ order.voucher.id)
    VoucherDTO appliedVoucher; // thông tin chi tiết voucher đã áp dụng
    List<VoucherDTO> availableVouchers; // để view hiển thị danh sách voucher có thể áp dụng
    String paymentStatus;    // map từ order.paymentStatus
    String paymentMethod;    // map từ order.paymentMethod
    String customerName;     // tên khách hàng
    String customerPhone;    // số điện thoại khách hàng
    Integer tableNumber;     // số bàn
    String orderType;        // loại order (DINE_IN, TAKE_AWAY)
    String orderStatus;      // trạng thái order
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}