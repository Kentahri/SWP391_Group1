package com.group1.swp.pizzario_swp391.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group1.swp.pizzario_swp391.dto.cart.CartItemDTO;
import com.group1.swp.pizzario_swp391.dto.kitchen.KitchenOrderDTO;
import com.group1.swp.pizzario_swp391.dto.order.OrderItemDTO;
import com.group1.swp.pizzario_swp391.dto.order.UpdateOrderItemsDTO;
import com.group1.swp.pizzario_swp391.dto.websocket.KitchenOrderMessage;
import com.group1.swp.pizzario_swp391.entity.Order;
import com.group1.swp.pizzario_swp391.entity.OrderItem;
import com.group1.swp.pizzario_swp391.entity.ProductSize;
import com.group1.swp.pizzario_swp391.mapper.OrderItemMapper;
import com.group1.swp.pizzario_swp391.repository.OrderItemRepository;
import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import com.group1.swp.pizzario_swp391.repository.ProductSizeRepository;
import com.group1.swp.pizzario_swp391.repository.SessionRepository;

import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class OrderService {

    OrderItemRepository orderItemRepository;
    OrderRepository orderRepository;
    SessionRepository sessionRepository;
    CartService cartService;
    OrderItemMapper orderItemMapper;
    WebSocketService webSocketService;
    SessionService sessionService;
    ProductSizeRepository productSizeRepository;

    public List<OrderItemDTO> getOrderedItemsForView(Long sessionId) {
        List<OrderItemDTO> orderedItems = new ArrayList<>();
        Order order = getOrderForSession(sessionId);

        if (order != null) {
            order.getOrderItems().forEach(item -> orderedItems.add(orderItemMapper.toOrderItemDTO(item)));
        }
        return orderedItems;
    }

    @Transactional
    public void placeOrder(HttpSession session, Long sessionId) {
        Map<Long, CartItemDTO> cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            return; // Cannot place an empty order
        }

        Order order = getOrderForSession(sessionId);
        boolean isNewOrder = (order == null);
        if (order == null) {
            order = new Order();
//          Tạo mới 1 order và lưu vào DB
            order.setSession(sessionService.getSessionById(sessionId));
            order.setCreatedAt(LocalDateTime.now());
            order.setOrderStatus(Order.OrderStatus.PREPARING);
            order.setOrderType(Order.OrderType.DINE_IN);
            order.setPaymentStatus(Order.PaymentStatus.UNPAID);
            order.setNote("");
            order.setTotalPrice(0.0);
            order.setTaxRate(0.1); // 10% tax
            orderRepository.save(order);
        }
        Order finalOrder = order;
        cart.values().forEach(item -> {
            OrderItem orderItem = orderItemMapper.toOrderItem(item);
            orderItem.setOrderItemStatus(OrderItem.OrderItemStatus.PENDING);
            orderItem.setOrderItemType(OrderItem.OrderItemType.DINE_IN);
            orderItem.setProductSize(item.getProductSize());
            finalOrder.setTotalPrice(finalOrder.getTotalPrice() + item.getTotalPrice());
            orderItemRepository.save(orderItem);
            finalOrder.addOrderItem(orderItem);
        });
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        cartService.clearCart(session);

        // Gửi thông báo đến kitchen: NEW_ORDER nếu là order mới, ORDER_UPDATED nếu là order đã tồn tại
        notifyKitchenOrderChange(order, isNewOrder);
    }

    @Transactional
    public com.group1.swp.pizzario_swp391.entity.Order placeTakeAwayOrder(HttpSession session, com.group1.swp.pizzario_swp391.entity.Staff staff) {
        Map<Long, CartItemDTO> cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            return null;
        }

        com.group1.swp.pizzario_swp391.entity.Order order = new com.group1.swp.pizzario_swp391.entity.Order();
        order.setOrderType(com.group1.swp.pizzario_swp391.entity.Order.OrderType.TAKE_AWAY);
        order.setOrderStatus(com.group1.swp.pizzario_swp391.entity.Order.OrderStatus.PREPARING);
        order.setPaymentStatus(com.group1.swp.pizzario_swp391.entity.Order.PaymentStatus.UNPAID);
        order.setTaxRate(0.1);
        order.setStaff(staff);
        order.setTotalPrice(0);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        for (CartItemDTO item : cart.values()) {
            System.out.println("🔍 OrderService: CartItemDTO note = [" + item.getNote() + "]");
            System.out.println("🔍 OrderService: CartItemDTO productId = " + item.getProductId() + ", productName = " + item.getProductName());

            OrderItem orderItem = orderItemMapper.toOrderItem(item);
            System.out.println("🔍 OrderService: OrderItem note after mapping = [" + orderItem.getNote() + "]");

            orderItem.setOrderItemStatus(OrderItem.OrderItemStatus.PENDING);
            orderItem.setOrderItemType(OrderItem.OrderItemType.TAKE_AWAY);
            orderItem.setProductSize(item.getProductSize());
            orderItemRepository.save(orderItem);

            System.out.println("💾 OrderService: OrderItem saved with ID = " + orderItem.getId() + ", note = [" + orderItem.getNote() + "]");

            order.addOrderItem(orderItem);
            order.setTotalPrice(order.getTotalPrice() + item.getTotalPrice());
        }
        orderRepository.save(order);
        cartService.clearCart(session);

        notifyKitchenNewOrder(order);
        return order;
    }

    /**
     * Gửi thông báo order mới đến kitchen
     */
    private void notifyKitchenNewOrder(Order order) {
        notifyKitchenOrderChange(order, true);
    }
    
    /**
     * Gửi thông báo order thay đổi đến kitchen
     * @param order Order đã thay đổi
     * @param isNewOrder true nếu là order mới, false nếu là order đã tồn tại được cập nhật
     */
    private void notifyKitchenOrderChange(Order order, boolean isNewOrder) {
        try {
            KitchenOrderMessage.MessageType messageType = isNewOrder 
                    ? KitchenOrderMessage.MessageType.NEW_ORDER 
                    : KitchenOrderMessage.MessageType.ORDER_UPDATED;
            
            String messageText = isNewOrder 
                    ? "Có order mới từ " + (order.getSession() != null && order.getSession().getTable() != null ?
                            "Bàn " + order.getSession().getTable().getId() : "Take away")
                    : "Order đã được cập nhật - có thêm món mới";
            
            // Lấy danh sách items để tính toán
            List<OrderItem> orderItems = order.getOrderItems();
            int totalItems = orderItems != null ? orderItems.size() : 0;
            int completedItems = orderItems != null 
                    ? (int) orderItems.stream()
                            .filter(item -> item.getOrderItemStatus() == OrderItem.OrderItemStatus.SERVED)
                            .count()
                    : 0;
            
            KitchenOrderMessage orderMessage = KitchenOrderMessage.builder()
                    .type(messageType)
                    .orderId(order.getId())
                    .code(String.format("ORD-%05d", order.getId())) // Tạo code với padding 0
                    .tableName(order.getSession() != null && order.getSession().getTable() != null ?
                            "Bàn " + order.getSession().getTable().getId() : "Take away")
                    .orderType(order.getOrderType() != null ? order.getOrderType().toString() : "DINE_IN")
                    .status(order.getOrderStatus() != null ? order.getOrderStatus().toString() : "PREPARING")
                    .priority("NORMAL") // Có thể thêm logic để xác định priority
                    .totalPrice(order.getTotalPrice())
                    .totalItems(totalItems)
                    .completedItems(completedItems)
                    .note(order.getNote())
                    .message(messageText)
                    .build();

            webSocketService.broadcastNewOrderToKitchen(orderMessage);
        } catch (Exception e) {
            // Log error nhưng không làm fail transaction
            System.err.println("Error notifying kitchen of order change: " + e.getMessage());
        }
    }

    public Order getOrderForSession(Long sessionId) {
        if (sessionId == null) return null;
        return sessionRepository.findById(sessionId)
                .map(com.group1.swp.pizzario_swp391.entity.Session::getOrder)
                .orElse(null);
    }

    /**
     * Backend tối ưu filter order đơn giản (status và/hoặc type)
     */
    public List<KitchenOrderDTO> getKitchenOrdersByFilter(String status, String type) {
        List<Order> orders = new ArrayList<>();
        if (status != null && !status.isBlank() && type != null && !type.isBlank()) {
            Order.OrderStatus statusEnum = Order.OrderStatus.valueOf(status);
            Order.OrderType typeEnum = Order.OrderType.valueOf(type);
            orders = orderRepository.findByOrderStatusAndOrderType(statusEnum, typeEnum);
        } else if (status != null && !status.isBlank()) {
            Order.OrderStatus statusEnum = Order.OrderStatus.valueOf(status);
            orders = orderRepository.findByOrderStatus(statusEnum);
        } else if (type != null && !type.isBlank()) {
            Order.OrderType typeEnum = Order.OrderType.valueOf(type);
            orders = orderRepository.findByOrderType(typeEnum);
        } else {
            // Mặc định: PREPARING
            orders = orderRepository.findAll().stream()
                    .filter(order -> order.getOrderStatus() == Order.OrderStatus.PREPARING)
                    .toList();
        }
        orders = orders.stream()
                .sorted((o1, o2) -> {
                    if (o1.getCreatedAt() == null && o2.getCreatedAt() == null) return 0;
                    if (o1.getCreatedAt() == null) return 1;
                    if (o2.getCreatedAt() == null) return -1;
                    return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                })
                .toList();
        return orders.stream().map(KitchenOrderDTO::fromOrder).toList();
    }

    public KitchenOrderDTO getKitchenOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        return (order != null) ? KitchenOrderDTO.fromOrder(order) : null;
    }

    /**
     * Lấy order theo ID
     */
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    /**
     * Lưu order
     */
    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    /**
     * Lấy danh sách lịch sử hóa đơn (bao gồm cả đơn đã thanh toán và chưa hoàn thành)
     */
    public List<com.group1.swp.pizzario_swp391.dto.order.OrderDetailDTO> getPaymentHistory() {
        List<Order> orders = orderRepository.findAll().stream()
                .sorted((o1, o2) -> {
                    // Sắp xếp theo ID đơn hàng, order ID lớn hơn trước (mới hơn)
                    if (o1.getId() == null && o2.getId() == null) return 0;
                    if (o1.getId() == null) return 1;
                    if (o2.getId() == null) return -1;
                    return o2.getId().compareTo(o1.getId());
                })
                .toList();

        return orders.stream()
                .map(order -> {
                    com.group1.swp.pizzario_swp391.dto.order.OrderDetailDTO dto = new com.group1.swp.pizzario_swp391.dto.order.OrderDetailDTO();
                    dto.setOrderId(order.getId());
                    dto.setOrderStatus(order.getOrderStatus());
                    dto.setOrderType(order.getOrderType());
                    // Set payment status thực tế của order (có thể là PAID, UNPAID, hoặc PENDING)
                    dto.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus() : Order.PaymentStatus.UNPAID);
                    dto.setPaymentMethod(order.getPaymentMethod());
                    dto.setTaxRate(order.getTaxRate());
                    dto.setNote(order.getNote());
                    dto.setCreatedAt(order.getCreatedAt());
                    dto.setUpdatedAt(order.getUpdatedAt());

                    // Calculate original total from order items
                    double originalTotal = order.getOrderItems().stream()
                            .mapToDouble(OrderItem::getTotalPrice)
                            .sum();

                    // Calculate discount from voucher if exists
                    double discountAmount = 0.0;
                    if (order.getVoucher() != null) {
                        dto.setVoucherCode(order.getVoucher().getCode());
                        // Calculate discount based on voucher type
                        if (order.getVoucher().getType() == com.group1.swp.pizzario_swp391.entity.Voucher.VoucherType.FIXED_AMOUNT) {
                            discountAmount = order.getVoucher().getValue();
                        } else if (order.getVoucher().getType() == com.group1.swp.pizzario_swp391.entity.Voucher.VoucherType.PERCENTAGE) {
                            discountAmount = originalTotal * order.getVoucher().getValue() / 100;
                        }
                        dto.setDiscountAmount(discountAmount);
                    }

                    // Calculate final total with tax (finalTotal = (originalTotal - discount) * 1.1)
                    double finalTotal = (originalTotal - discountAmount) * 1.1;
                    dto.setTotalPrice(finalTotal);

                    // Set session info
                    if (order.getSession() != null) {
                        dto.setSessionId(order.getSession().getId());
                        if (order.getSession().getTable() != null) {
                            dto.setTableId(order.getSession().getTable().getId());
                            dto.setTableName("Bàn " + order.getSession().getTable().getId());
                        }
                    }

                    // Set staff name
                    if (order.getStaff() != null) {
                        dto.setCreatedByStaffName(order.getStaff().getName());
                    }

                    // Set customer info from membership
                    if (order.getMembership() != null) {
                        dto.setCustomerName(order.getMembership().getName());
                        dto.setCustomerPhone(order.getMembership().getPhoneNumber());
                    } else {
                        dto.setCustomerName("Khách vãng lai");
                        dto.setCustomerPhone(null);
                    }

                    // Convert order items (not needed for payment history display)
                    List<com.group1.swp.pizzario_swp391.dto.order.OrderItemDTO> items = order.getOrderItems().stream()
                            .map(orderItemMapper::toOrderItemDTO)
                            .toList();
                    dto.setItems(items);

                    return dto;
                })
                .toList();
    }

    /**
     * Cập nhật order items cho cashier (thêm món vào order đang có)
     */
    @Transactional
    public void updateOrderItemsForCashier(Long orderId, List<UpdateOrderItemsDTO.OrderItemUpdate> newItems) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getOrderStatus() != Order.OrderStatus.PREPARING) {
            throw new RuntimeException("Chỉ có thể cập nhật order đang chuẩn bị");
        }

        for (UpdateOrderItemsDTO.OrderItemUpdate newItem : newItems) {
            final ProductSize productSize;
            if (newItem.getSizeId() != null && newItem.getProductId() != null) {

                productSize = productSizeRepository.findByProductIdAndSizeId(
                        newItem.getProductId(),
                        newItem.getSizeId()
                ).orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy ProductSize với productId: " + newItem.getProductId() + " và sizeId: " + newItem.getSizeId()));
            } else if (newItem.getProductId() != null) {
                List<ProductSize> productSizes = productSizeRepository.findByProductId(newItem.getProductId());
                if (productSizes.isEmpty()) {
                    throw new RuntimeException("Product không có size nào: " + newItem.getProductId());
                }
                productSize = productSizes.getFirst();
            } else {
                throw new RuntimeException("ProductId không được để trống");
            }

            // Check if item already exists in order (kiểm tra cả productId và sizeId)
            OrderItem existingItem = order.getOrderItems().stream()
                    .filter(item -> item.getProductSize() != null
                            && item.getProductSize().getProduct().getId().equals(newItem.getProductId())
                            && item.getProductSize().getSize().getId().equals(productSize.getSize().getId()))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {
                // Update quantity of existing item
                existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity());
                existingItem.setTotalPrice(existingItem.getUnitPrice() * existingItem.getQuantity());
            } else {
                // Add new item
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setQuantity(newItem.getQuantity());
                orderItem.setUnitPrice(newItem.getPrice());
                orderItem.setTotalPrice(newItem.getPrice() * newItem.getQuantity());
                orderItem.setOrderItemStatus(OrderItem.OrderItemStatus.PENDING);
                orderItem.setOrderItemType(OrderItem.OrderItemType.DINE_IN);
                orderItem.setProductSize(productSize); // Set ProductSize

                order.getOrderItems().add(orderItem);
                orderItemRepository.save(orderItem);
            }
        }

        double newTotal = order.getOrderItems().stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
        order.setTotalPrice(newTotal);

        orderRepository.save(order);
        notifyKitchenOrderChange(order,false);
    }

}
