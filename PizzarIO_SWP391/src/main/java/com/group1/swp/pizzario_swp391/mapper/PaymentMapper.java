package com.group1.swp.pizzario_swp391.mapper;

import com.group1.swp.pizzario_swp391.dto.payment.PaymentDTO;
import com.group1.swp.pizzario_swp391.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "orderTotal", source = "totalPrice")
    @Mapping(target = "originalTotal", ignore = true) // Sẽ được tính toán trong service
    @Mapping(target = "discountAmount", ignore = true) // Sẽ được tính toán trong service
    @Mapping(target = "membershipId", source = "membership.id")
    @Mapping(target = "membershipPoints", source = "membership.points")
    @Mapping(target = "pointsUsed", ignore = true) // Sẽ được set trong PaymentService từ pointsUsedBySession map
    @Mapping(target = "appliedVoucherId", source = "voucher.id")
    @Mapping(target = "appliedVoucher", ignore = true) // Sẽ được map riêng nếu cần
    @Mapping(target = "availableVouchers", ignore = true) // Sẽ được set riêng trong service
    @Mapping(target = "paymentStatus", source = "paymentStatus")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "customerName", ignore = true) // Sẽ được set riêng trong service
    @Mapping(target = "customerPhone", source = "membership.phoneNumber")
    @Mapping(target = "tableNumber", source = "session.table.id")
    @Mapping(target = "orderType", source = "orderType")
    @Mapping(target = "orderStatus", source = "orderStatus")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    PaymentDTO toDTO(Order order);  
}