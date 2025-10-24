package com.group1.swp.pizzario_swp391.service;


import com.group1.swp.pizzario_swp391.repository.OrderRepository;
import com.group1.swp.pizzario_swp391.repository.SessionRepository;
import com.group1.swp.pizzario_swp391.repository.TableRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class GuestService {

    TableRepository tableRepository;
    SessionRepository sessionRepository;
    OrderRepository orderRepository;
    WebSocketService webSocketService;
    SimpMessagingTemplate messagingTemplate;


}
