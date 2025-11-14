package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.cart.CartItemDTO;
import com.group1.swp.pizzario_swp391.entity.Product;
import com.group1.swp.pizzario_swp391.entity.ProductSize;
import com.group1.swp.pizzario_swp391.repository.ProductRepository;
import com.group1.swp.pizzario_swp391.repository.ProductSizeRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService{

    ProductRepository productRepository;

    private static final String SESSION_CART_KEY = "sessionCart";
    private final ProductSizeService productSizeService;
    private final ProductSizeRepository productSizeRepository;

    public void clearCart(HttpSession session) {
        session.removeAttribute(SESSION_CART_KEY);
    }

    @SuppressWarnings("unchecked")
    protected Map<Long, CartItemDTO> getCart(HttpSession session) {
        Map<Long, CartItemDTO> cart = (Map<Long, CartItemDTO>) session.getAttribute(SESSION_CART_KEY);
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute(SESSION_CART_KEY, cart);
        }
        return cart;
    }

    public void addToCart(HttpSession session, Long productId, int quantity, String note, Long productSizeId) {
        Map<Long, CartItemDTO> cart = getCart(session);
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        ProductSize productSize;
        if (productSizeId != null) {
            productSize = productSizeService.getById(productSizeId);
        } else {
            // nếu ấn chọn đồ mà không ko chọn size, mặc định là size thứ nhất
            productSize = productSizeService.findByProductId(productId).get(0);
            productSizeId = productSize.getId();
        }

        CartItemDTO cartItem = cart.get(productSizeId);
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setTotalPrice(cartItem.getUnitPrice() * cartItem.getQuantity());
            if (note != null && !note.trim().isEmpty()) {
                if (cartItem.getNote() == null || cartItem.getNote().trim().isEmpty()) {
                    cartItem.setNote(note.trim());
                } else {
                    cartItem.setNote(cartItem.getNote().trim() + ", " + note.trim());
                }
            }
            cart.put(productSizeId, cartItem);
        } else {
            double currentPrice = getCurrentPrice(productSize);
            cartItem = CartItemDTO.builder().productId(productId).productName(product.getName()).productImageUrl(product.getImageURL()).quantity(quantity).unitPrice(currentPrice).totalPrice(currentPrice * quantity).note(note).productSize(productSize).build();
            cart.put(productSizeId, cartItem);
        }
    }

    public void updateCartItem(HttpSession session,
                               Long productId,
                               int quantity,
                               String note,
                               Long newProductSizeId,   // size MỚI muốn đổi sang
                               Long oldProductSizeId) { // size CŨ đang có

        Map<Long, CartItemDTO> cart = getCart(session);

        // 1. LẤY ITEM CŨ
        CartItemDTO oldItem = cart.get(oldProductSizeId);
        if (oldItem == null) {
            return; // Không tồn tại
        }

        // 2. XỬ LÝ XÓA NẾU quantity <= 0
        if (quantity <= 0) {
            cart.remove(oldProductSizeId);
            return;
        }

        // 3. Chuẩn hóa note
        note = (note != null) ? note.trim() : "";

        // 4. GỘP NOTE: note cũ + note mới
        String mergedNote = Stream.of(oldItem.getNote(), note)
                .filter(n -> n != null && !n.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));

        // 5. XÓA ITEM CŨ NGAY (sẽ thêm lại sau nếu cần)
        cart.remove(oldProductSizeId);

        // === ĐỔI SANG SIZE KHÁC ===
        if (!oldProductSizeId.equals(newProductSizeId)) {

            ProductSize newSize = productSizeService.getById(newProductSizeId);
            if (newSize == null) return;

            double newUnitPrice = getCurrentPrice(newSize);
            CartItemDTO existingItem = cart.get(newProductSizeId);

            if (existingItem != null) {
                // GỘP VÀO SIZE MỚI ĐÃ TỒN TẠI
                int totalQty = existingItem.getQuantity() + quantity;
                existingItem.setQuantity(totalQty);
                existingItem.setTotalPrice(totalQty * existingItem.getUnitPrice());

                String finalNote = Stream.of(existingItem.getNote(), mergedNote)
                        .filter(n -> n != null && !n.isBlank())
                        .distinct()
                        .collect(Collectors.joining(", "));

                existingItem.setNote(finalNote);
                cart.put(newProductSizeId, existingItem);

            } else {
                // TẠO MỚI CHO SIZE MỚI
                CartItemDTO newItem = CartItemDTO.builder()
                        .productId(productId)
                        .productName(oldItem.getProductName())
                        .productImageUrl(oldItem.getProductImageUrl())
                        .quantity(quantity)
                        .unitPrice(newUnitPrice)
                        .totalPrice(newUnitPrice * quantity)
                        .note(mergedNote)
                        .productSize(newSize)
                        .build();

                cart.put(newProductSizeId, newItem);
            }

        } else {
            // === KHÔNG ĐỔI SIZE → CẬP NHẬT TRỰC TIẾP ===
            oldItem.setQuantity(quantity);
            oldItem.setTotalPrice(oldItem.getUnitPrice() * quantity);
            oldItem.setNote(mergedNote);

            cart.put(oldProductSizeId, oldItem); // key = newProductSizeId = oldProductSizeId
        }
    }


    public void removeFromCart(HttpSession session, Long productId, Long productSizeId) {
        getCart(session).remove(productSizeId);
    }

    public List<CartItemDTO> getCartForView(HttpSession session) {
        List<CartItemDTO> cartItems = new ArrayList<>();

        getCart(session).values().forEach(item -> {
            ProductSize fullPs = productSizeRepository.findById(item.getProductSize().getId()).orElse(null);

            cartItems.add(CartItemDTO.builder().productId(item.getProductId()).productName(item.getProductName()).productImageUrl(item.getProductImageUrl()).quantity(item.getQuantity()).unitPrice(item.getUnitPrice()).totalPrice(item.getQuantity() * item.getUnitPrice()).productSize(fullPs).note(item.getNote()).productSizes(fullPs != null ? fullPs.getProduct().getProductSizes() : List.of()).build());
        });
        return cartItems;
    }

    private double getCurrentPrice(ProductSize productSize) {
        if (productSize.getFlashSaleStart() != null && productSize.getFlashSaleEnd() != null && productSize.getFlashSalePrice() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(productSize.getFlashSaleStart()) && now.isBefore(productSize.getFlashSaleEnd())) {
                return productSize.getFlashSalePrice() > 0 ? productSize.getFlashSalePrice() : productSize.getBasePrice();
            }
        }
        return productSize.getBasePrice();
    }

}