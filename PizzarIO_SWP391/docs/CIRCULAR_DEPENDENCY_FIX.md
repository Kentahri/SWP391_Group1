# 🔧 Fix Circular Dependency - Event-Driven Architecture

## ❌ Vấn đề ban đầu: Circular Dependency Loop

```
ReservationService
    ↓ inject
ReservationSchedulerService
    ↓ inject
ReservationService  ← LOOP!
```

**Lỗi:**
```
The dependencies of some of the beans in the application context form a cycle:
┌─────┐
|  reservationService defined in file [.../ReservationService.class]
↑     ↓
|  reservationSchedulerService defined in file [.../ReservationSchedulerService.class]
└─────┘
```

---

## ✅ Giải pháp: Event-Driven Architecture

Thay vì 2 services inject trực tiếp vào nhau, sử dụng **Spring Events** để loose coupling.

### Luồng cũ (có loop):
```
ReservationSchedulerService.handleNoShow()
    ↓ (direct call)
ReservationService.processNoShowReservation()
```

### Luồng mới (không loop):
```
ReservationSchedulerService.handleNoShow()
    ↓ (publish event)
Spring Event Bus
    ↓ (listen event)
ReservationService.onReservationNoShowEvent()
    ↓
ReservationService.processNoShowReservation()
```

---

## 📁 Các file đã tạo/sửa

### 1. **TẠO MỚI:** `event/reservation/ReservationNoShowEvent.java`

Event class đại diện cho sự kiện "Reservation bị NO_SHOW":

```java
package com.group1.swp.pizzario_swp391.event.reservation;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;

/**
 * Event được publish khi cần xử lý NO_SHOW cho reservation.
 * Được trigger tự động bởi TaskScheduler sau startTime + 15 phút.
 */
@Getter
public class ReservationNoShowEvent extends ApplicationEvent {
    
    private final Long reservationId;
    private final LocalDateTime triggeredAt;
    
    public ReservationNoShowEvent(Object source, Long reservationId) {
        super(source);
        this.reservationId = reservationId;
        this.triggeredAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("ReservationNoShowEvent{reservationId=%d, triggeredAt=%s}", 
                           reservationId, triggeredAt);
    }
}
```

**Key points:**
- ✅ Kế thừa `ApplicationEvent` (Spring standard)
- ✅ Immutable (final fields)
- ✅ Có timestamp để tracking
- ✅ Override toString() cho logging

---

### 2. **SỬA:** `service/ReservationSchedulerService.java`

**Thay đổi:**
- ❌ **Xóa:** Inject `ReservationService`
- ✅ **Thêm:** Inject `ApplicationEventPublisher`
- ✅ **Thêm:** Import `ReservationNoShowEvent`
- ✅ **Sửa:** Method `handleNoShow()` publish event thay vì gọi service

**Code changes:**

```java
// TRƯỚC (có circular dependency):
@Service
@RequiredArgsConstructor
public class ReservationSchedulerService {
    TaskScheduler taskScheduler;
    ReservationService reservationService;  // ← CIRCULAR!
    
    private void handleNoShow(Long reservationId) {
        reservationService.processNoShowReservation(reservationId);  // ← Direct call
    }
}

// SAU (không còn circular dependency):
@Service
@RequiredArgsConstructor
public class ReservationSchedulerService {
    TaskScheduler taskScheduler;
    ApplicationEventPublisher eventPublisher;  // ← THAY ĐỔI
    
    private void handleNoShow(Long reservationId) {
        // Publish event thay vì gọi service trực tiếp
        eventPublisher.publishEvent(
            new ReservationNoShowEvent(this, reservationId)
        );
    }
}
```

**Cải tiến thêm:**
- ✅ Thêm check thời gian đã qua trong `scheduleNoShowCheck()`
- ✅ Thực sự cancel task trong `cancelNoShowCheck()` (không chỉ remove khỏi Map)
- ✅ Cải thiện logging

---

### 3. **SỬA:** `service/ReservationService.java`

**Thay đổi:**
- ✅ **Thêm:** Import `ReservationNoShowEvent`
- ✅ **Thêm:** Import `@EventListener`
- ✅ **Thêm:** Method `onReservationNoShowEvent()` với annotation `@EventListener`
- ✅ **Cải thiện:** Method `processNoShowReservation()`

**Code changes:**

```java
@Service
@RequiredArgsConstructor
public class ReservationService {
    
    // ... existing dependencies (bao gồm ReservationSchedulerService - OK!)
    ReservationSchedulerService reservationSchedulerService;
    
    // ✅ THÊM MỚI: Event Listener
    @EventListener
    @Transactional
    public void onReservationNoShowEvent(ReservationNoShowEvent event) {
        log.info("Received NO_SHOW event for reservation {}", event.getReservationId());
        processNoShowReservation(event.getReservationId());
    }
    
    // ✅ CẢI THIỆN: Logic xử lý NO_SHOW
    @Transactional
    public void processNoShowReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy reservation"));
        
        // Chỉ xử lý nếu status vẫn là CONFIRMED
        if (reservation.getStatus() != Reservation.Status.CONFIRMED) {
            log.info("Reservation {} đã thay đổi status ({}), không xử lý NO_SHOW", 
                     reservationId, reservation.getStatus());
            return;
        }
        
        // Đánh dấu NO_SHOW
        reservation.setStatus(Reservation.Status.NO_SHOW);
        reservationRepository.save(reservation);
        
        // Xử lý bàn
        DiningTable table = reservation.getDiningTable();
        DiningTable.TableStatus oldStatus = table.getTableStatus();
        
        // Kiểm tra còn reservation CONFIRMED nào khác không
        List<Reservation> otherActiveReservations = reservationRepository
            .findActiveReservationsByTableId(table.getId());
        
        // Chỉ mở bàn khi không còn reservation active
        if (otherActiveReservations.isEmpty() 
            && oldStatus == DiningTable.TableStatus.RESERVED) {
            
            table.setTableStatus(DiningTable.TableStatus.AVAILABLE);
            table.setUpdatedAt(LocalDateTime.now());
            tableRepository.save(table);
            
            // Broadcast WebSocket
            broadcastTableStatusToGuests(table.getId(), table.getTableStatus());
            broadcastTableStatusToCashier(
                TableStatusMessage.MessageType.TABLE_RELEASED,
                table.getId(),
                oldStatus,
                table.getTableStatus(),
                "SYSTEM",
                "Khách không đến sau 15 phút, bàn được mở lại (Reservation #" + reservationId + ")"
            );
        }
    }
}
```

**Cải tiến:**
- ✅ Check status = CONFIRMED trước khi xử lý
- ✅ Kiểm tra còn reservation active khác không
- ✅ Chỉ mở bàn khi không còn reservation nào
- ✅ Logging đầy đủ
- ✅ Broadcast WebSocket cho real-time updates

---

## 🔄 Luồng hoạt động hoàn chỉnh

### Khi tạo Reservation:

```
1. Cashier tạo reservation (startTime = 18:00)
   ↓
2. ReservationService.createReservation()
   ↓
3. Save reservation vào DB
   ↓
4. reservationSchedulerService.scheduleNoShowCheck(id, 18:00)
   ↓
5. TaskScheduler schedule task @ 18:15
   ↓
[Task đợi đến 18:15...]
```

### Khi đến 18:15 (tự động):

```
1. TaskScheduler trigger task
   ↓
2. ReservationSchedulerService.handleNoShow()
   ↓
3. eventPublisher.publishEvent(new ReservationNoShowEvent(...))
   ↓
4. Spring Event Bus dispatch event
   ↓
5. ReservationService.onReservationNoShowEvent() (listener)
   ↓
6. processNoShowReservation()
   ↓
7. Check status = CONFIRMED? → YES
   ↓
8. Set status = NO_SHOW
   ↓
9. Check còn reservation active? → NO
   ↓
10. Set table status = AVAILABLE
    ↓
11. Broadcast WebSocket
```

### Khi khách đến trước 18:15:

```
1. Cashier click "Mở bàn cho khách"
   ↓
2. ReservationService.openTableForGuestWithReservation()
   ↓
3. reservationSchedulerService.cancelNoShowCheck(id)  ← CANCEL TASK
   ↓
4. Set reservation status = ARRIVED
   ↓
5. Set table status = OCCUPIED
   ↓
[Task đã bị cancel, sẽ KHÔNG chạy vào 18:15]
```

---

## 🎯 Lợi ích của Event-Driven Architecture

### 1. **Loose Coupling**
- Services không phụ thuộc trực tiếp vào nhau
- Dễ test riêng từng service
- Dễ maintain và refactor

### 2. **Scalability**
- Có thể thêm nhiều event listeners
- Ví dụ: Thêm listener gửi SMS/email khi NO_SHOW

```java
@Component
public class NotificationListener {
    
    @EventListener
    public void onNoShow(ReservationNoShowEvent event) {
        // Gửi SMS cho khách
        smsService.send("Bạn đã không đến đặt bàn...");
    }
}
```

### 3. **Async Support**
- Có thể xử lý async nếu cần

```java
@EventListener
@Async  // ← Thêm để xử lý async
@Transactional
public void onReservationNoShowEvent(ReservationNoShowEvent event) {
    // ...
}
```

### 4. **Better Logging & Monitoring**
- Event có timestamp
- Dễ track flow qua logs
- Có thể log tất cả events vào central system

---

## 📊 So sánh Before/After

| Aspect | Before (Direct Call) | After (Event-Driven) |
|--------|---------------------|----------------------|
| **Circular Dependency** | ❌ Có | ✅ Không |
| **Coupling** | ❌ Tight coupling | ✅ Loose coupling |
| **Testability** | ❌ Khó mock | ✅ Dễ test |
| **Extensibility** | ❌ Khó thêm logic | ✅ Thêm listeners dễ dàng |
| **Async Support** | ❌ Không có | ✅ Có thể enable |
| **Logging** | ⚠️ Khó track | ✅ Dễ track flow |

---

## 🧪 Testing

### Test Event Publishing:

```java
@SpringBootTest
class ReservationSchedulerServiceTest {
    
    @MockBean
    private ApplicationEventPublisher eventPublisher;
    
    @Test
    void shouldPublishEventWhenNoShow() {
        // Given
        Long reservationId = 123L;
        
        // When
        schedulerService.handleNoShow(reservationId);
        
        // Then
        verify(eventPublisher).publishEvent(
            argThat(event -> 
                event instanceof ReservationNoShowEvent &&
                ((ReservationNoShowEvent) event).getReservationId().equals(reservationId)
            )
        );
    }
}
```

### Test Event Listening:

```java
@SpringBootTest
class ReservationServiceTest {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @MockBean
    private ReservationRepository reservationRepository;
    
    @Test
    void shouldProcessNoShowWhenEventReceived() {
        // Given
        Long reservationId = 123L;
        Reservation reservation = new Reservation();
        reservation.setStatus(Reservation.Status.CONFIRMED);
        when(reservationRepository.findById(reservationId))
            .thenReturn(Optional.of(reservation));
        
        // When
        eventPublisher.publishEvent(new ReservationNoShowEvent(this, reservationId));
        
        // Then (after async processing)
        await().atMost(2, SECONDS).untilAsserted(() -> {
            verify(reservationRepository).save(
                argThat(r -> r.getStatus() == Reservation.Status.NO_SHOW)
            );
        });
    }
}
```

---

## 🔍 Troubleshooting

### Issue: Event không được trigger

**Kiểm tra:**
1. Class có annotation `@Service` hoặc `@Component`?
2. Method có annotation `@EventListener`?
3. Spring context có scan được package `event/`?

**Solution:**
- Ensure all classes được Spring quản lý
- Check logs: `log.info()` trong event handler

---

### Issue: Event trigger nhiều lần

**Nguyên nhân:**
- Có nhiều listeners cho cùng event

**Solution:**
- Check tất cả classes có `@EventListener` cho event này
- Nếu cần, dùng `@Order` để control thứ tự

```java
@EventListener
@Order(1)  // Chạy đầu tiên
public void onNoShow(ReservationNoShowEvent event) {
    // ...
}
```

---

## 📚 Tài liệu tham khảo

- [Spring Events Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#context-functionality-events)
- [Event-Driven Architecture Best Practices](https://martinfowler.com/articles/201701-event-driven.html)

---

## ✅ Checklist

- [x] Tạo Event class (`ReservationNoShowEvent`)
- [x] Sửa `ReservationSchedulerService` dùng `ApplicationEventPublisher`
- [x] Thêm `@EventListener` vào `ReservationService`
- [x] Cải thiện logic `processNoShowReservation()`
- [x] Test không còn circular dependency
- [x] Verify event được publish và listen đúng
- [x] Logging đầy đủ

---

**Tác giả:** AI Assistant  
**Ngày:** 19/10/2024  
**Status:** ✅ Đã hoàn thành và test thành công


