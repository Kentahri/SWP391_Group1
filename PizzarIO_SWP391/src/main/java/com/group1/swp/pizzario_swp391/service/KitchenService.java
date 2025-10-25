package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.OrderItem;
import com.group1.swp.pizzario_swp391.repository.OrderItemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service để xử lý các thao tác của kitchen
 * Chỉ cập nhật trạng thái của từng item, không cập nhật order status
 */
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class KitchenService {

    OrderItemRepository orderItemRepository;

    /**
     * Cập nhật trạng thái item trong order
     * Kitchen chỉ cập nhật status của từng món, không cập nhật order status
     */
    @Transactional
    public void updateItemStatus(Long itemId, String status, String note) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Order item not found: " + itemId));

        OrderItem.OrderItemStatus newStatus = OrderItem.OrderItemStatus.valueOf(status);
        item.setOrderItemStatus(newStatus);
        
        if (note != null && !note.trim().isEmpty()) {
            item.setNote(note);
        }
        
        orderItemRepository.save(item);

        log.info("Kitchen updated item {} to status {}", itemId, status);
    }
}
