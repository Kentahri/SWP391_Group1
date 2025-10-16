package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.cart.CartItemDTO;
import com.group1.swp.pizzario_swp391.entity.Product;
import com.group1.swp.pizzario_swp391.repository.ProductRepository;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService{

    ProductRepository productRepository;

    private static final String SESSION_CART_KEY = "sessionCart";

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

    public void addToCart(HttpSession session, Long productId, int quantity) {
        Map<Long, CartItemDTO> cart = getCart(session);
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

        CartItemDTO cartItem = cart.get(productId);
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setTotalPrice(cartItem.getUnitPrice() * cartItem.getQuantity());
            cart.put(productId, cartItem);
        } else {
            double currentPrice = getCurrentPrice(product);
            cartItem = new CartItemDTO(productId, product.getName(), product.getImageURL(), quantity, currentPrice, currentPrice * quantity, null);
            cart.put(productId, cartItem);
        }
    }

    public void updateCartItem(HttpSession session, Long productId, int quantity) {
        Map<Long, CartItemDTO> cart = getCart(session);
        CartItemDTO cartItem = cart.get(productId);
        if (cartItem != null) {
            if (quantity > 0) {
                cartItem.setQuantity(quantity);
                cartItem.setTotalPrice(cartItem.getUnitPrice() * cartItem.getQuantity());
                cart.put(productId, cartItem);
            } else {
                cart.remove(productId);
            }
        }
    }

    public void removeFromCart(HttpSession session, Long productId) {
        getCart(session).remove(productId);
    }

    public List<CartItemDTO> getCartForView(HttpSession session) {
        List<CartItemDTO> cartItems = new ArrayList<>();
        getCart(session).values().forEach(item -> cartItems.add(CartItemDTO.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productImageUrl(item.getProductImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getQuantity() * item.getUnitPrice())
                .note(null)
                .build()));
        return cartItems;
    }

    private double getCurrentPrice(Product product) {
        if (product.getFlashSaleStart() != null && product.getFlashSaleEnd() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(product.getFlashSaleStart()) && now.isBefore(product.getFlashSaleEnd())) {
                return product.getFlashSalePrice() > 0 ? product.getFlashSalePrice() : product.getBasePrice();
            }
        }
        return product.getBasePrice();
    }
}