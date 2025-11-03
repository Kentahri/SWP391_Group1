package com.group1.swp.pizzario_swp391.mapper;

import com.group1.swp.pizzario_swp391.dto.cart.CartItemDTO;
import com.group1.swp.pizzario_swp391.dto.order.OrderItemDTO;
import com.group1.swp.pizzario_swp391.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper{

    OrderItem toOrderItem(OrderItemDTO orderItemDTO);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "totalPrice", target = "totalPrice")
    OrderItem toOrderItem(CartItemDTO cartItemDTO);

    @Mapping(target = "productName", source = "productSize.product.name")
    @Mapping(target = "productImageUrl", source = "productSize.product.imageURL")
    @Mapping(target = "status", source = "orderItemStatus")
//    @Mapping(target = "type", source = "orderItemType")
    OrderItemDTO toOrderItemDTO(OrderItem orderItem);
}
