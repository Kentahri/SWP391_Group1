package com.group1.swp.pizzario_swp391.service;

import com.group1.swp.pizzario_swp391.dto.websocket.TableSelectionRequest;
import com.group1.swp.pizzario_swp391.entity.DiningTable;
import com.group1.swp.pizzario_swp391.repository.TableRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WebSocketCashierService {

    SimpMessagingTemplate messagingTemplate;
    TableRepository tableRepository;

    @Transactional
    public void handleTableSelection(TableSelectionRequest request) {
        try {
            DiningTable table = tableRepository.findById(request.getTableId()).orElse(null);
        }catch (Exception e) {

        }
    }
}
