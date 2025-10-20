package com.group1.swp.pizzario_swp391.config;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

/**
 * WebSocket Configuration for Real-time Communication
 * 
 * Endpoints:
 * - /ws: For Cashier, Guest and Kitchen
 *
 * Topics:
 * - /topic/tables-cashier: Broadcast table status to cashier
 * - /topic/tables-guest: Broadcast table availability to all tablets
 * - /topic/order-items: Broadcast order updates to cashier
 * - /queue/guest-{sessionId}: Personal messages for each guest tablet
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker
        // /topic for broadcast, /queue for targeted messages
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages from clients
        config.setApplicationDestinationPrefixes("/app");
        // Prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new DefaultHandshakeHandler(){
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes){
                        // Tạo unique ID cho mỗi WebSocket connection
                        // Không phụ thuộc HTTP Session để tránh bị logout ảnh hưởng
                        String uniqueId = UUID.randomUUID().toString();
                        return new WebSocketPrincipal(uniqueId);
                    }
                })
                .withSockJS()
                // Disable HTTP session cho SockJS
                .setSessionCookieNeeded(false);
    }

    /**
         * Custom Principal để WebSocket không phụ thuộc HTTP Session
         */
        private record WebSocketPrincipal(String name) implements Principal {

        @Override
            public String getName() {
                return name;
            }
        }
}

