# KIẾN TRÚC HỆ THỐNG PIZZARIO - TÍCH HỢP WEBSOCKET

## 📋 KIẾN TRÚC HIỆN TẠI (Trước khi thêm WebSocket)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER (Browser)                             │
├─────────────────────────────────────────────────────────────────────────────┤
│  Thymeleaf Templates                                                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │  Guest   │  │ Cashier  │  │ Kitchen  │  │ Manager  │  │  Admin   │    │
│  │  Page    │  │  Page    │  │  Page    │  │  Page    │  │  Page    │    │
│  └─────┬────┘  └─────┬────┘  └─────┬────┘  └─────┬────┘  └─────┬────┘    │
└────────┼─────────────┼─────────────┼─────────────┼─────────────┼──────────┘
         │             │             │             │             │
         │ HTTP/HTTPS  │             │             │             │
         │ Request     │             │             │             │
         ▼             ▼             ▼             ▼             ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       SPRING BOOT APPLICATION                               │
│                                                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐│
│  │                      SECURITY LAYER                                    ││
│  │  ┌──────────────────────────────────────────────────────────────────┐ ││
│  │  │  Spring Security (Authentication & Authorization)                │ ││
│  │  │  - DevSecurityConfig / ProdSecurityConfig                        │ ││
│  │  │  - JpaUserDetailsService                                         │ ││
│  │  │  - Role-based access control                                     │ ││
│  │  └──────────────────────────────────────────────────────────────────┘ ││
│  └────────────────────────────────────────────────────────────────────────┘│
│                                                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐│
│  │                      CONTROLLER LAYER                                 ││
│  │                                                                        ││
│  │  GuestController    CashierController    KitchenController            ││
│  │  LoginController    DashboardController                               ││
│  │  Manager:                                                             ││
│  │    - CategoryController    - ProductController                        ││
│  │    - StaffController        - TableController                         ││
│  │    - VoucherController                                                ││
│  │                                                                        ││
│  │  Xử lý HTTP Request → Response (Synchronous)                          ││
│  └───────────────────────────┬────────────────────────────────────────────┘│
│                              │                                             │
│  ┌───────────────────────────▼────────────────────────────────────────────┐│
│  │                      SERVICE LAYER                                     ││
│  │                                                                        ││
│  │  CategoryService    ProductService    StaffService                    ││
│  │  TableService       VoucherService    ShiftService                    ││
│  │  LoginService       OtpMailService                                    ││
│  │                                                                        ││
│  │  Business Logic & Validation                                          ││
│  └───────────────────────────┬────────────────────────────────────────────┘│
│                              │                                             │
│  ┌───────────────────────────▼────────────────────────────────────────────┐│
│  │                      REPOSITORY LAYER                                  ││
│  │                                                                        ││
│  │  CategoryRepo    ProductRepo    StaffRepo    TableRepo                ││
│  │  VoucherRepo     ShiftRepo      StaffShiftRepo                        ││
│  │                                                                        ││
│  │  Spring Data JPA (CRUD Operations)                                    ││
│  └───────────────────────────┬────────────────────────────────────────────┘│
│                              │                                             │
│  ┌───────────────────────────▼────────────────────────────────────────────┐│
│  │                      ENTITY LAYER                                      ││
│  │                                                                        ││
│  │  Category    Product    Staff    DiningTable    Voucher               ││
│  │  Order       OrderItem  Session  Shift          Membership            ││
│  │                                                                        ││
│  │  JPA Entities (ORM Mapping)                                           ││
│  └────────────────────────────────────────────────────────────────────────┘│
└───────────────────────────────┬─────────────────────────────────────────────┘
                                │
                                ▼
                    ┌──────────────────────┐
                    │  SQL Server Database │
                    │                      │
                    │  Tables:             │
                    │  - staff             │
                    │  - product           │
                    │  - category          │
                    │  - dining_table      │
                    │  - order             │
                    │  - order_item        │
                    │  - voucher           │
                    │  - shift             │
                    │  - session           │
                    └──────────────────────┘
```

**Đặc điểm:**
- ✅ Kiến trúc 3-Layer: Controller → Service → Repository
- ✅ Communication: **Request-Response (Synchronous)**
- ✅ Client phải refresh page hoặc polling để cập nhật data
- ❌ Không có real-time updates
- ❌ Kitchen/Cashier không nhận thông báo tức thì khi có order mới

---

## 🚀 KIẾN TRÚC MỚI (Sau khi tích hợp WebSocket)

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                            CLIENT LAYER (Browser)                                    │
├──────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                      │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐   │
│  │  Guest Page    │  │ Cashier Page   │  │ Kitchen Page   │  │ Manager Page   │   │
│  │                │  │                │  │                │  │                │   │
│  │  - Thymeleaf   │  │  - Thymeleaf   │  │  - Thymeleaf   │  │  - Thymeleaf   │   │
│  │  - Static HTML │  │  - WebSocket JS│  │  - WebSocket JS│  │  - WebSocket JS│   │
│  │                │  │  - STOMP Client│  │  - STOMP Client│  │  - STOMP Client│   │
│  └───┬────────────┘  └───┬────────┬───┘  └───┬────────┬───┘  └───┬────────┬───┘   │
│      │                   │        │          │        │          │        │       │
└──────┼───────────────────┼────────┼──────────┼────────┼──────────┼────────┼───────┘
       │                   │        │          │        │          │        │
       │ HTTP              │ HTTP   │WS        │ HTTP   │WS        │ HTTP   │WS
       │ Request           │Request │          │Request │          │Request │
       ▼                   ▼        ▼          ▼        ▼          ▼        ▼
┌──────────────────────────────────────────────────────────────────────────────────────┐
│                         SPRING BOOT APPLICATION                                      │
│                                                                                      │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐│
│  │                            SECURITY LAYER                                       ││
│  │  ┌───────────────────────────────────────────────────────────────────────────┐ ││
│  │  │  Spring Security                                                          │ ││
│  │  │  - HTTP Security (Controllers)                                            │ ││
│  │  │  - WebSocket Security (STOMP endpoints)  ◄──── MỚI THÊM                  │ ││
│  │  │  - Role-based channel authorization      ◄──── MỚI THÊM                  │ ││
│  │  └───────────────────────────────────────────────────────────────────────────┘ ││
│  └─────────────────────────────────────────────────────────────────────────────────┘│
│                                                                                      │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐│
│  │                     COMMUNICATION LAYER (2 Protocols)                           ││
│  │                                                                                 ││
│  │  ┌────────────────────────┐      ┌────────────────────────────────────────┐   ││
│  │  │   HTTP/HTTPS           │      │   WebSocket/STOMP  ◄──── MỚI THÊM     │   ││
│  │  │   (Traditional)        │      │   (Real-time)                          │   ││
│  │  │                        │      │                                        │   ││
│  │  │  - Page requests       │      │  - Persistent connection               │   ││
│  │  │  - Form submissions    │      │  - Bi-directional                      │   ││
│  │  │  - AJAX calls          │      │  - Push notifications                  │   ││
│  │  │  - File uploads        │      │  - Real-time updates                   │   ││
│  │  └────────────────────────┘      └────────────────────────────────────────┘   ││
│  └─────────────────────────────────────────────────────────────────────────────────┘│
│                                                                                      │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐│
│  │                    WEBSOCKET INFRASTRUCTURE  ◄──── MỚI THÊM                     ││
│  │                                                                                 ││
│  │  ┌───────────────────────────────────────────────────────────────────────────┐ ││
│  │  │  WebSocket Configuration                                                  │ ││
│  │  │  - WebSocketConfig                                                        │ ││
│  │  │  - Endpoint: /ws (SockJS)                                                 │ ││
│  │  │  - Message Broker: /topic, /queue                                        │ ││
│  │  │  - Application Prefix: /app                                              │ ││
│  │  └───────────────────────────────────────────────────────────────────────────┘ ││
│  │                                                                                 ││
│  │  ┌───────────────────────────────────────────────────────────────────────────┐ ││
│  │  │  STOMP Message Broker                                                     │ ││
│  │  │                                                                           │ ││
│  │  │  Topics (Broadcast):                                                      │ ││
│  │  │  ├─ /topic/kitchen   → Notifications cho Kitchen staff                   │ ││
│  │  │  ├─ /topic/cashier   → Notifications cho Cashier                         │ ││
│  │  │  ├─ /topic/manager   → Notifications cho Manager                         │ ││
│  │  │  └─ /topic/table/{tableId} → Updates cho bàn cụ thể                     │ ││
│  │  │                                                                           │ ││
│  │  │  Queues (Point-to-Point):                                                 │ ││
│  │  │  └─ /queue/staff/{staffId} → Personal notifications                      │ ││
│  │  └───────────────────────────────────────────────────────────────────────────┘ ││
│  │                                                                                 ││
│  │  ┌───────────────────────────────────────────────────────────────────────────┐ ││
│  │  │  WebSocket Event Listener                                                 │ ││
│  │  │  - Session connect/disconnect tracking                                    │ ││
│  │  │  - Online staff management                                                │ ││
│  │  │  - Auto cleanup on disconnect                                             │ ││
│  │  └───────────────────────────────────────────────────────────────────────────┘ ││
│  └─────────────────────────────────────────────────────────────────────────────────┘│
│                                                                                      │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐│
│  │                       CONTROLLER LAYER (Mở rộng)                                ││
│  │                                                                                 ││
│  │  ┌────────────────────────┐    ┌────────────────────────────────────────────┐ ││
│  │  │  HTTP Controllers      │    │  WebSocket Controllers  ◄──── MỚI THÊM     │ ││
│  │  │  (Existing)            │    │                                            │ ││
│  │  │                        │    │  - OrderNotificationController             │ ││
│  │  │  - GuestController     │    │    @MessageMapping("/order.create")        │ ││
│  │  │  - CashierController   │    │    @SendTo("/topic/kitchen")              │ ││
│  │  │  - KitchenController   │    │                                            │ ││
│  │  │  - Manager Controllers │    │  - TableStatusController                   │ ││
│  │  │  - LoginController     │    │    @MessageMapping("/table.update")        │ ││
│  │  │                        │    │    @SendTo("/topic/cashier")              │ ││
│  │  │  Return: Views/JSON    │    │                                            │ ││
│  │  │                        │    │  - StaffPresenceController                 │ ││
│  │  │                        │    │    Track online/offline status             │ ││
│  │  │                        │    │                                            │ ││
│  │  │                        │    │  Return: Real-time messages                │ ││
│  │  └────────────────────────┘    └────────────────────────────────────────────┘ ││
│  └─────────────────────────────────────────────────────────────────────────────────┘│
│                                                                                      │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐│
│  │                       SERVICE LAYER (Mở rộng)                                   ││
│  │                                                                                 ││
│  │  Existing Services:                    New/Enhanced Services:                  ││
│  │  - CategoryService                     - OrderService (enhanced)               ││
│  │  - ProductService                        + sendOrderToKitchen()  ◄─ WebSocket ││
│  │  - StaffService                        - TableService (enhanced)               ││
│  │  - TableService                          + broadcastTableStatus() ◄─ WebSocket││
│  │  - VoucherService                      - NotificationService (NEW)             ││
│  │  - ShiftService                          + sendToStaff()                       ││
│  │  - LoginService                          + broadcastToRole()                   ││
│  │  - OtpMailService                                                              ││
│  │                                                                                 ││
│  │  Inject: SimpMessagingTemplate để gửi WebSocket messages  ◄──── MỚI THÊM      ││
│  └─────────────────────────────────────────────────────────────────────────────────┘│
│                                                                                      │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐│
│  │                      REPOSITORY & ENTITY LAYER (Không đổi)                      ││
│  │                                                                                 ││
│  │  Repository: CategoryRepo, ProductRepo, StaffRepo, OrderRepo...                ││
│  │  Entity: Category, Product, Staff, Order, OrderItem, DiningTable...            ││
│  └─────────────────────────────────────────────────────────────────────────────────┘│
└──────────────────────────────────┬───────────────────────────────────────────────────┘
                                   │
                                   ▼
                       ┌────────────────────────┐
                       │  SQL Server Database   │
                       │  (Không thay đổi)      │
                       └────────────────────────┘
```

---

## 🔄 CÁC LUỒNG HOẠT ĐỘNG REAL-TIME

### 1️⃣ LUỒNG TẠO ORDER MỚI (Guest → Kitchen)

```
┌─────────┐       ┌─────────┐       ┌──────────┐       ┌─────────┐       ┌─────────┐
│ Guest   │       │ Cashier │       │ Backend  │       │  STOMP  │       │ Kitchen │
│ (Web)   │       │ (Web)   │       │ Service  │       │ Broker  │       │ (Web)   │
└────┬────┘       └────┬────┘       └────┬─────┘       └────┬────┘       └────┬────┘
     │                 │                 │                  │                 │
     │ 1. Chọn món,    │                 │                  │                 │
     │    Submit order │                 │                  │                 │
     ├────────────────>│                 │                  │                 │
     │                 │                 │                  │                 │
     │                 │ 2. HTTP POST    │                  │                 │
     │                 │   /order/create │                  │                 │
     │                 ├────────────────>│                  │                 │
     │                 │                 │                  │                 │
     │                 │                 │ 3. Save to DB    │                 │
     │                 │                 │    (OrderService)│                 │
     │                 │                 │                  │                 │
     │                 │                 │ 4. Send WebSocket│                 │
     │                 │                 │    message       │                 │
     │                 │                 ├─────────────────>│                 │
     │                 │                 │  /topic/kitchen  │                 │
     │                 │                 │                  │                 │
     │                 │                 │                  │ 5. Broadcast    │
     │                 │                 │                  ├────────────────>│
     │                 │                 │                  │ "New Order #123"│
     │                 │                 │                  │                 │
     │                 │                 │                  │ 6. Popup hiện   │
     │                 │                 │                  │    ngay lập tức │
     │                 │                 │                  │    (không F5)   │
```

### 2️⃣ LUỒNG CẬP NHẬT TRẠNG THÁI BÀN (Cashier ↔ Manager)

```
┌─────────┐       ┌──────────┐       ┌─────────┐       ┌─────────┐
│ Cashier │       │ Backend  │       │  STOMP  │       │ Manager │
│         │       │          │       │ Broker  │       │         │
└────┬────┘       └────┬─────┘       └────┬────┘       └────┬────┘
     │                 │                  │                 │
     │ 1. Click "Bàn 5│                  │                 │
     │    đang phục vụ"                  │                 │
     ├────────────────>│                  │                 │
     │                 │                  │                 │
     │                 │ 2. Update DB     │                 │
     │                 │   (TableService) │                 │
     │                 │                  │                 │
     │                 │ 3. Broadcast     │                 │
     │                 ├─────────────────>│                 │
     │                 │ /topic/table/5   │                 │
     │                 │                  │                 │
     │                 │                  │ 4. Real-time    │
     │ 5. Cập nhật UI  │                  │    update       │
     │<────────────────┼──────────────────┼────────────────>│
     │ (màu bàn đổi)   │                  │ (màu bàn đổi)   │
```

### 3️⃣ LUỒNG THÔNG BÁO CA LÀM VIỆC (Manager → Staff)

```
┌─────────┐       ┌──────────┐       ┌─────────┐       ┌──────────────┐
│ Manager │       │ Backend  │       │  STOMP  │       │ All Staff    │
│         │       │          │       │ Broker  │       │ (Online)     │
└────┬────┘       └────┬─────┘       └────┬────┘       └──────┬───────┘
     │                 │                  │                   │
     │ 1. Tạo ca mới   │                  │                   │
     │    "Sáng 8-12"  │                  │                   │
     ├────────────────>│                  │                   │
     │                 │                  │                   │
     │                 │ 2. Save shift    │                   │
     │                 │   (ShiftService) │                   │
     │                 │                  │                   │
     │                 │ 3. Broadcast     │                   │
     │                 ├─────────────────>│                   │
     │                 │ /topic/staff     │                   │
     │                 │                  │                   │
     │                 │                  │ 4. Push notify    │
     │                 │                  ├──────────────────>│
     │                 │                  │ "Ca mới: Sáng 8-12"
     │                 │                  │                   │
     │                 │                  │ 5. Toast/Alert    │
     │                 │                  │    hiển thị       │
```


## 🎯 CÁC COMPONENT MỚI CẦN THÊM

### Backend:

```
src/main/java/.../
├── config/
│   └── WebSocketConfig.java                ◄──── Cấu hình WebSocket
│
├── controller/websocket/
│   ├── OrderNotificationController.java    ◄──── Order real-time
│   ├── TableStatusController.java          ◄──── Table updates
│   ├── StaffPresenceController.java        ◄──── Staff online/offline
│   └── ChatController.java                 ◄──── Staff chat (optional)
│
├── dto/websocket/
│   ├── OrderNotificationDTO.java           ◄──── Data cho notifications
│   ├── TableStatusUpdateDTO.java
│   └── StaffPresenceDTO.java
│
├── service/
│   └── NotificationService.java            ◄──── Service gửi WebSocket
│       (inject SimpMessagingTemplate)
│
└── listener/
    └── WebSocketEventListener.java         ◄──── Track connections
```

### Frontend:

```
src/main/resources/
├── static/
│   └── js/
│       ├── websocket-client.js             ◄──── STOMP client setup
│       ├── kitchen-notifications.js        ◄──── Kitchen-specific
│       ├── cashier-notifications.js        ◄──── Cashier-specific
│       └── table-realtime-updates.js       ◄──── Real-time table UI
│
└── templates/
    ├── kitchen-page/
    │   └── kitchen.html                    ◄──── Thêm WebSocket script
    ├── cashier-page/
    │   └── cashier.html                    ◄──── Thêm WebSocket script
    └── admin_page/
        └── dashboard.html                  ◄──── Live analytics
```

---

## 🔐 BẢO MẬT WEBSOCKET

```
┌─────────────────────────────────────────────────────────────┐
│              WebSocket Security Configuration               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Channel Authorization:                                     │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  /topic/kitchen    → ROLE_KITCHEN, ROLE_MANAGER       │ │
│  │  /topic/cashier    → ROLE_CASHIER, ROLE_MANAGER       │ │
│  │  /topic/manager    → ROLE_MANAGER                     │ │
│  │  /queue/staff/{id} → Owner only (CSRF token check)    │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  Authentication:                                            │
│  - Dùng chung session với HTTP (Spring Security)           │
│  - WebSocket handshake check authentication                │
│  - StompHeaderAccessor để lấy Principal                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 💡 TỔNG KẾT

### Kiến trúc mới có 2 tầng giao tiếp song song:

1. **HTTP/HTTPS Layer** (Traditional)
   - CRUD operations
   - Page navigation
   - Form submissions
   - File uploads

2. **WebSocket Layer** (Real-time)
   - Order notifications
   - Table status updates
   - Staff presence
   - Live analytics
   - Chat (optional)

