# 📚 Tổng quan Hệ thống Reservation (Đặt bàn)

## 🎯 Mục tiêu hệ thống

Hệ thống đặt bàn tự động cho phép:
1. ✅ **Đặt bàn trước** cho khách hàng
2. ✅ **Tự động khóa bàn** trước 90 phút khi có reservation
3. ✅ **Tự động xử lý NO_SHOW** sau 15 phút nếu khách không đến
4. ✅ **Mở bàn cho khách** khi khách đã đến
5. ✅ **Quản lý reservation** (CRUD operations)

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────┐
│                    CASHIER DASHBOARD                         │
│  (Giao diện quản lý bàn và reservation)                     │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│              CASHIER DASHBOARD CONTROLLER                    │
│  - Tạo reservation                                           │
│  - Xem danh sách reservations                                │
│  - Sửa/Hủy reservation                                       │
│  - Mở bàn cho khách đã đặt trước                            │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│                 RESERVATION SERVICE                          │
│  - Business logic validation                                 │
│  - Quản lý reservation lifecycle                             │
│  - Tương tác với Scheduler Service                          │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│          RESERVATION SCHEDULER SERVICE                       │
│  - Schedule NO_SHOW check tasks                              │
│  - Cancel tasks khi cần                                      │
│  - Reschedule khi update reservation                         │
└────────────┬────────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│         TASK SCHEDULER (Spring ThreadPoolTaskScheduler)      │
│  - Quản lý scheduled tasks                                   │
│  - Tự động chạy tasks đúng thời gian                         │
│  - Thread pool với 5 threads                                 │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 Entity Models

### 1. Reservation

```java
public class Reservation {
    private Long id;
    private DiningTable diningTable;
    private LocalDateTime startTime;
    private LocalDateTime createdAt;
    private Status status;  // CONFIRMED, ARRIVED, CANCELED, NO_SHOW
    private String phone;
    private String name;
    private int capacityExpected;
    private String note;
}
```

### 2. DiningTable

```java
public class DiningTable {
    private Integer id;
    private Integer version;  // Optimistic locking
    private TableStatus tableStatus;  // AVAILABLE, OCCUPIED, RESERVED, WAITING_PAYMENT
    private TableCondition tableCondition;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int capacity;
    private List<Reservation> reservations;
    private List<Session> sessionList;
}
```

---

## 🔄 Luồng hoạt động chính

### 1️⃣ TẠO RESERVATION (Create Reservation)

```
Cashier nhập thông tin
         ↓
Validate: table exists, capacity, conflicts
         ↓
Save reservation (status = CONFIRMED)
         ↓
Table status → RESERVED
         ↓
Schedule NO_SHOW check @ (startTime + 15 phút)
         ↓
Return ReservationDTO
```

**Validation rules:**
- ✅ Bàn phải tồn tại
- ✅ Số khách <= capacity của bàn
- ✅ Không trùng thời gian với reservation khác
- ✅ Cách nhau ít nhất 90 phút
- ✅ Nếu bàn đang OCCUPIED, phải đặt cách ít nhất 90 phút

**Code endpoint:**
```java
POST /cashier/reservations
```

---

### 2️⃣ TỰ ĐỘNG KHÓA BÀN (Auto Lock Table)

```
Scheduled task chạy mỗi 1 phút
         ↓
Tìm reservations trong khoảng (now → now + 90 phút)
         ↓
For each reservation:
    Nếu table status = AVAILABLE
         ↓
    Set table status → RESERVED
         ↓
    Broadcast WebSocket
```

**Tại sao 90 phút?**
- Đảm bảo bàn được khóa trước khi khách đến
- Khách có thể đến sớm
- Rule business: reservation phải cách nhau 90 phút

**Code:**
```java
@Scheduled(fixedRate = 60_000)  // 1 phút
public void closeTable() {
    // ... logic ...
}
```

---

### 3️⃣ TỰ ĐỘNG XỬ LÝ NO_SHOW (Auto NO_SHOW Check)

```
Khi tạo reservation
         ↓
Schedule task @ (startTime + 15 phút)
         ↓
Task tự động chạy đúng giờ
         ↓
Kiểm tra: status vẫn = CONFIRMED?
         ↓ YES
Set reservation status → NO_SHOW
         ↓
Kiểm tra: còn reservation active nào khác?
         ↓ NO
Set table status → AVAILABLE
         ↓
Broadcast WebSocket
```

**Tại sao 15 phút?**
- Grace period cho khách đến trễ
- Không quá gắt gao
- Có thể config trong `application.yaml`

**Code:**
```java
TaskScheduler.schedule(
    () -> handleNoShow(reservationId),
    startTime.plusMinutes(15)
);
```

---

### 4️⃣ MỞ BÀN CHO KHÁCH (Open Table)

```
Khách đến nhà hàng
         ↓
Cashier xác nhận thông tin
         ↓
Click "🔓 Mở bàn cho khách"
         ↓
Cancel scheduled NO_SHOW task
         ↓
Set reservation status → ARRIVED
         ↓
Set table status → OCCUPIED
         ↓
Broadcast WebSocket
         ↓
Show success message
```

**Tại sao cancel task?**
- Khách đã đến rồi
- Task NO_SHOW không cần chạy nữa
- Tiết kiệm tài nguyên

**Code endpoint:**
```java
POST /cashier/reservations/{id}/open
```

---

### 5️⃣ HỦY RESERVATION (Cancel Reservation)

```
Cashier click "Hủy đặt bàn"
         ↓
Cancel scheduled NO_SHOW task
         ↓
Set reservation status → CANCELED
         ↓
Kiểm tra: còn reservation active nào khác?
         ↓ NO
Set table status → AVAILABLE
         ↓
Broadcast WebSocket
```

**Code endpoint:**
```java
POST /cashier/reservations/{id}/delete
```

---

### 6️⃣ CẬP NHẬT RESERVATION (Update Reservation)

```
Cashier sửa thông tin
         ↓
Validate (giống như create)
         ↓
Nếu thay đổi thời gian:
    Cancel old scheduled task
    Schedule new task @ (newStartTime + 15 phút)
         ↓
Update reservation
         ↓
Return success
```

**Code endpoint:**
```java
POST /cashier/reservations/{id}/update
```

---

## 🎨 UI Components

### 1. Table Grid (Bên trái)

```
┌─────────────────────────┐
│   🪑 Table map          │
├─────────────────────────┤
│  ┌───┐ ┌───┐ ┌───┐     │
│  │ 1 │ │ 2 │ │ 3 │     │  Green = AVAILABLE
│  └───┘ └───┘ └───┘     │  Red = OCCUPIED
│  ┌───┐ ┌───┐ ┌───┐     │  Orange = WAITING_PAYMENT
│  │ 4 │ │ 5 │ │ 6 │     │  Blue = RESERVED
│  └───┘ └───┘ └───┘     │
└─────────────────────────┘
```

### 2. Reservation Card (Bên phải)

```
┌─────────────────────────────────────┐
│ Bàn 3                   ✅ Đã xác nhận│
├─────────────────────────────────────┤
│ 🕐 Thời gian: Thứ 6, 19 Oct 18:00   │
│                                      │
│ 👤 Khách hàng: Nguyễn Văn A         │
│ 📞 Số điện thoại: 0901234567        │
│ 👥 Số khách: 4 người                │
│ 📝 Ghi chú: Ngồi gần cửa sổ         │
│                                      │
│ ┌─────────────────────────────────┐ │
│ │  🔓 Mở bàn cho khách           │ │  ← NÚT CHÍNH
│ └─────────────────────────────────┘ │
│ ┌────────────┐ ┌──────────────────┐ │
│ │ ✏️ Sửa     │ │ ❌ Hủy đặt bàn   │ │  ← CÁC NÚT PHỤ
│ └────────────┘ └──────────────────┘ │
└─────────────────────────────────────┘
```

---

## 🔧 Configuration

### SchedulerConfig.java

```java
@Configuration
public class SchedulerConfig {
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);                         // 5 threads
        scheduler.setThreadNamePrefix("reservation-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();
        return scheduler;
    }
}
```

### Application Properties (Optional)

```yaml
reservation:
  no-show-grace-period-minutes: 15
  auto-lock-before-minutes: 90
  task-check-interval-ms: 60000
```

---

## 📈 State Machine

### Table Status Flow

```
[AVAILABLE] ──create reservation──> [RESERVED]
                                         │
                      open table ────────┤
                      (khách đến)        │
                                         ▼
                                   [OCCUPIED]
                                         │
                      request payment────┤
                                         ▼
                                [WAITING_PAYMENT]
                                         │
                      complete payment───┤
                                         ▼
                                   [AVAILABLE]
                                   
[RESERVED] ──cancel/no_show──> [AVAILABLE]
```

### Reservation Status Flow

```
       [CONFIRMED]
           │
           ├─── open table ────> [ARRIVED]
           │    (khách đến)
           │
           ├─── cancel ────────> [CANCELED]
           │    (Cashier hủy)
           │
           └─── auto after 15m ─> [NO_SHOW]
                (scheduled task)
```

---

## 🎯 Key Features

### ✅ Validation mạnh mẽ
- Kiểm tra capacity
- Kiểm tra conflict thời gian
- Kiểm tra 90 phút rule
- Kiểm tra table status

### ✅ Auto Scheduling
- Tự động khóa bàn trước 90 phút
- Tự động xử lý NO_SHOW sau 15 phút
- Task management với cancel/reschedule

### ✅ Real-time Updates
- WebSocket broadcast cho Cashiers
- WebSocket broadcast cho Guest page
- Instant UI updates

### ✅ User-friendly UI
- Thymeleaf templates (ít JS)
- Confirmation dialogs
- Flash messages (success/error)
- Responsive design

### ✅ Transaction Safety
- `@Transactional` cho data consistency
- Optimistic locking (version field)
- Rollback on error

---

## 📚 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/cashier` | Dashboard chính |
| `GET` | `/cashier/tables/{id}/order` | Chi tiết order của bàn |
| `POST` | `/cashier/reservations` | Tạo reservation mới |
| `GET` | `/cashier/reservations/upcoming` | Danh sách reservation sắp tới |
| `GET` | `/cashier/tables/{id}/reservations` | Reservations của bàn cụ thể |
| `GET` | `/cashier/reservations/{id}/edit` | Form edit reservation |
| `POST` | `/cashier/reservations/{id}/update` | Update reservation |
| `POST` | `/cashier/reservations/{id}/delete` | Cancel reservation |
| `POST` | `/cashier/reservations/{id}/open` | **Mở bàn cho khách** |

---

## 🐛 Common Issues & Solutions

### Issue 1: Scheduled task không chạy

**Nguyên nhân:**
- Thiếu `@EnableScheduling` trong Application class
- Method không phải là public
- Exception trong method

**Solution:**
```java
@SpringBootApplication
@EnableScheduling  // ← Important!
public class Application {
    // ...
}
```

---

### Issue 2: Task vẫn chạy sau khi cancel

**Nguyên nhân:**
- Không gọi `cancelNoShowCheck()`
- ScheduledFuture bị null
- Task đã chạy rồi mới cancel

**Solution:**
- Đảm bảo gọi cancel trước khi task chạy
- Check `future.isDone()` trước khi cancel
- Log để debug

---

### Issue 3: Server restart mất scheduled tasks

**Nguyên nhân:**
- Tasks chỉ tồn tại trong memory
- Không persist vào database

**Solution:**
```java
@PostConstruct
public void initScheduledTasks() {
    // Reload tất cả reservations active
    List<Reservation> active = repository.findUpcomingReservations(now);
    for (Reservation r : active) {
        scheduleNoShowCheck(r.getId(), r.getStartTime());
    }
}
```

---

## 📊 Performance Considerations

### Tần suất chạy scheduled tasks

| Task | Frequency | Lý do |
|------|-----------|-------|
| Auto lock tables | 1 phút | Không cần quá real-time, 1 phút là đủ |
| NO_SHOW check | On-demand | Chỉ chạy đúng giờ cần thiết |

### Thread pool sizing

```java
scheduler.setPoolSize(5);  // Cho app nhỏ-vừa
```

**Công thức:**
- App nhỏ: 5 threads
- App vừa: 10-20 threads
- App lớn: CPU cores * 2

### Database queries

- Index trên: `table_id`, `start_time`, `status`
- Pagination cho danh sách reservations
- Eager loading cho relationships cần thiết

---

## 🔐 Security

### CSRF Protection
- Tất cả POST forms có CSRF token
- Validation trên server

### Authorization
- Chỉ Cashier role mới truy cập được
- Check role trong Security Config

### Input Validation
- Bean Validation annotations
- Custom validators
- Server-side validation

---

## 🧪 Testing Checklist

### Unit Tests
- [ ] ReservationService validation logic
- [ ] SchedulerService schedule/cancel/reschedule
- [ ] NO_SHOW handler logic

### Integration Tests
- [ ] Full reservation creation flow
- [ ] Auto lock table task
- [ ] Auto NO_SHOW task
- [ ] Open table flow
- [ ] Cancel reservation flow

### UI Tests
- [ ] Reservation modal form
- [ ] Validation error messages
- [ ] Success flash messages
- [ ] Open table button
- [ ] Responsive design

---

## 📖 Documentation Files

1. `OPEN_RESERVED_TABLE_FEATURE.md` - Chi tiết tính năng Open Table
2. `open-reserved-table-flow.puml` - Sequence diagram Open Table
3. `table-reservation-state-diagram.puml` - State machine
4. `reservation-auto-no-show.puml` - Luồng tự động NO_SHOW
5. `reservation-scheduler-architecture.puml` - Kiến trúc Scheduler
6. `reservation-state-machine.puml` - Reservation lifecycle
7. `reservation-timeline-example.puml` - Timeline example
8. `RESERVATION_SYSTEM_OVERVIEW.md` - **File này** (tổng quan)

---

## 🎓 Technologies Used

- **Backend:** Spring Boot, Spring MVC, Spring Scheduling
- **Database:** JPA/Hibernate, SQL Server
- **Frontend:** Thymeleaf, HTML/CSS, minimal JavaScript
- **Real-time:** WebSocket (STOMP over SockJS)
- **Task Scheduling:** Spring TaskScheduler (ThreadPoolTaskScheduler)
- **Validation:** Bean Validation (JSR-380)

---

## 👥 Team Members

- **Backend Developer:** [Your name]
- **Frontend Developer:** [Your name]
- **Database Designer:** [Your name]

---

## 📅 Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2024-10-19 | Initial release với đầy đủ tính năng |

---

**Last Updated:** 19/10/2024  
**Status:** ✅ Completed & Documented



