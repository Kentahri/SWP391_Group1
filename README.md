# 🍕 PizzarIO - Pizza Restaurant Management System

[![Java](https://img.shields.io/badge/Java-24-ED8B00?style=flat-square&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-6DB33F?style=flat-square&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![SQL Server](https://img.shields.io/badge/SQL%20Server-2019+-CC2927?style=flat-square&logo=microsoft-sql-server)](https://www.microsoft.com/en-us/sql-server)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=flat-square&logo=docker)](https://www.docker.com/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=flat-square&logo=apache-maven)](https://maven.apache.org/)

---

## 🎯 Giới Thiệu

**PizzarIO** là một hệ thống quản lý quán pizza toàn diện, được xây dựng bằng Spring Boot với Java 24. Ứng dụng cung cấp giải pháp hoàn chỉnh cho các nhà quản lý quán ăn, bao gồm quản lý đơn hàng, đặt bàn, nhân viên, khách hàng, thanh toán và phân tích dữ liệu trong thời gian thực.

Hệ thống sử dụng kiến trúc MVC với Thymeleaf để render trên máy chủ, Spring Security cho xác thực, và WebSocket cho cập nhật thời gian thực.

---

## ⭐ Tính Năng Chính

### 🛒 Quản Lý Đơn Hàng
- Hỗ trợ đơn hàng dine-in (ăn tại quán) và take-away
- Trạng thái đơn hàng: Chuẩn bị, Hoàn thành, Hủy
- Nhiều phương thức thanh toán: Tiền mặt, Mã QR
- Tính toán và áp dụng thuế

### 🍕 Quản Lý Sản Phẩm
- Danh mục sản phẩm: Pizza, Đồ uống, Khai vị, Tdessert, Combo
- Lưu trữ ảnh trên Cloudinary
- Giá theo kích thước (Small, Medium, Large, Default)
- Flash sale với thời gian giới hạn

### 🪑 Quản Lý Bàn Ăn
- Theo dõi trạng thái bàn: Sẵn sàng, Đang sử dụng, Đặt trước, Chờ thanh toán
- Quản lý sức chứa bàn
- Optimistic Locking cho các cập nhật đồng thời

### 📅 Hệ Thống Đặt Bàn
- Trạng thái đặt bàn: Đã xác nhận, Đã đến, Đã hủy, No-show
- Phát hiện xung đột tự động
- Lên lịch đặt bàn tự động
- Theo dõi no-show

### 👥 Quản Lý Nhân Viên
- Quản lý thông tin nhân viên (tên, ngày sinh, điện thoại, địa chỉ)
- Gán vai trò: Quản lý, Nhân viên bán hàng, Nhân viên bếp
- Kích hoạt/Vô hiệu hóa tài khoản

### ⏰ Quản Lý Ca Làm Việc
- Lên lịch ca làm việc
- Trạng thái ca: Theo lịch, Có mặt, Trễ, Vắng mặt, Hoàn thành
- Tính toán lương hàng giờ
- Theo dõi hình phạt
- Xuất Excel

### 🎁 Hệ Thống Mã Voucher
- Giảm giá theo phần trăm hoặc số tiền cố định
- Giới hạn số lần sử dụng
- Yêu cầu số tiền đơn hàng tối thiểu
- Quản lý thời gian hiệu lực

### 💳 Chương Trình Thành Viên
- Xác định thành viên dựa trên số điện thoại
- Hệ thống điểm tích lũy
- Theo dõi lịch sử đơn hàng

### ⚡ Giao Tiếp Thời Gian Thực
- WebSocket cho cập nhật live
- Cập nhật trạng thái đơn hàng cho bếp
- Thông báo tính sẵn có của bàn
- Cập nhật xử lý thanh toán

### 🤖 Chatbot AI
- Tích hợp Google Gemini API 2.5 Flash
- Hỗ trợ trò chuyện cho khách hàng
- Widget chatbot trên trang khách

### 📊 Phân Tích và Báo Cáo
- Dashboard phân tích dữ liệu
- Phân tích đơn hàng
- Theo dõi doanh thu
- Phân tích hiệu suất nhân viên
- Cập nhật phân tích real-time qua WebSocket

---

### Phần Mềm Cần Thiết

| Phần Mềm | Phiên Bản | Tải Về |
|----------|-----------|--------|
| Java JDK | 21+ (Java 24 khuyên dùng) | [Oracle Java](https://www.oracle.com/java/technologies/downloads/) |
| SQL Server | 2019 hoặc mới hơn | [Microsoft SQL Server](https://www.microsoft.com/en-us/sql-server/sql-server-downloads) |
| Docker | 20.10+ (tùy chọn) | [Docker Desktop](https://www.docker.com/products/docker-desktop) |
| Maven | 3.8+ (có thể bỏ qua nếu dùng mvnw) | [Apache Maven](https://maven.apache.org/download.cgi) |
| Git | Bất kỳ phiên bản | [Git](https://git-scm.com/download) |

---

## 🚀 Cài Đặt và Khởi Chạy

### 1️⃣ Tải Về Mã Nguồn

```bash
# Clone repository
git clone https://github.com/Kentahri/SWP391_Group1.git
cd PizzarIO_SWP391
```

### 3️⃣ Cấu Hình Biến Môi Trường

Tạo hoặc chỉnh sửa file `env.properties` trong thư mục gốc:

```properties
# ========== DATABASE CONFIGURATION ==========
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=swp_test;encrypt=true;trustServerCertificate=true
DB_USERNAME=sa
DB_PASSWORD=1234

# ========== SERVER CONFIGURATION ==========
SERVER_PORT=8080
SERVER_CONTEXT_PATH=/pizzario

# ========== MAIL CONFIGURATION ==========
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# ========== GEMINI AI CONFIGURATION ==========
GEMINI_API_KEY=your_gemini_api_key
GEMINI_MODEL=gemini-2.5-flash

# ========== CLOUDINARY CONFIGURATION ==========
CLOUD_NAME=your_cloud_name
CLOUD_API_KEY=your_api_key
CLOUD_API_SECRET=your_api_secret
```

**Lưu Ý quan trọng:**
- Gmail: [Tạo App Password](https://support.google.com/accounts/answer/185833)
- Gemini API: [Lấy từ Google AI Studio](https://ai.google.dev/tutorials/setup)
- Cloudinary: [Tạo tài khoản miễn phí](https://cloudinary.com/users/register/free)


### 4️⃣ Xây Dựng Ứng Dụng

#### Dùng Maven Wrapper (Khuyên dùng)
```bash
# Windows
mvnw.cmd clean package

# Linux/Mac
./mvnw clean package
```

#### Hoặc dùng Maven (nếu đã cài đặt)
```bash
mvn clean package
```

**Kết quả:** File `target/PizzarIO_SWP391-V1.jar` sẽ được tạo

### 5️⃣ Chạy Ứng Dụng

#### Phương Pháp 1: Chạy JAR trực tiếp
```bash
java -jar target/PizzarIO_SWP391-V1.jar
```

#### Phương Pháp 2: Chạy trong IDE (IntelliJ IDEA)
```bash
# 1. Mở project trong IntelliJ
# 2. Chuột phải vào PizzarIoSwp391Application.java
# 3. Chọn "Run"
# Hoặc nhấn: Shift + F10 (Windows) hoặc Ctrl + R (Mac)
```

#### Phương Pháp 3: Chạy ở chế độ Development
```bash
./mvnw spring-boot:run
```

#### Phương Pháp 4: Chạy với Docker Compose (Khuyên dùng)
```bash
# Xây dựng và chạy container
docker compose up --build

# Chỉ chạy nếu đã xây dựng trước
docker compose up

# Dừng containers
docker compose down
```

### 7️⃣ Truy Cập Ứng Dụng

Sau khi ứng dụng khởi động thành công, mở trình duyệt và truy cập:

```
http://localhost:8080/pizzario
```

**Trang chủ:** Trang đăng nhập (Login Page)


#### `config/manager-settings.yaml`
Cấu hình runtime cho người quản lý (được lưu động)

### Cấu Hình Bảo Mật

Ứng dụng có 2 cấu hình bảo mật:

**Dev Mode** (`ProdSecurityConfig.java` không được load khi `dev` profile):
```bash
java -jar app.jar --spring.profiles.active=dev
```

**Production Mode** (`ProdSecurityConfig.java` được kích hoạt):
```bash
java -jar app.jar --spring.profiles.active=prod
```

---

## 📁 Cấu Trúc Dự Án

```
PizzarIO_SWP391/
│
├── src/main/
│   ├── java/com/group1/swp/pizzario_swp391/
│   │   ├── annotation/                 # Custom annotations
│   │   ├── config/                     # Spring configurations
│   │   │   ├── CloudinaryConfig.java
│   │   │   ├── SecurityConfig.java
│   │   │   ├── WebSocketConfig.java
│   │   │   └── ...
│   │   │
│   │   ├── controller/                 # Controller classes
│   │   │   ├── manager/               # Manager endpoints
│   │   │   ├── cashier/               # Cashier endpoints
│   │   │   ├── kitchen/               # Kitchen endpoints
│   │   │   ├── guest/                 # Guest endpoints
│   │   │   └── websocket/             # WebSocket handlers
│   │   │
│   │   ├── service/                   # Business logic services
│   │   │   ├── OrderService.java
│   │   │   ├── PaymentService.java
│   │   │   ├── StaffShiftService.java
│   │   │   ├── ReservationService.java
│   │   │   └── ...
│   │   │
│   │   ├── repository/                # JPA repositories
│   │   │
│   │   ├── entity/                    # JPA entities
│   │   │
│   │   ├── dto/                       # Data Transfer Objects
│   │   │   ├── order/
│   │   │   ├── payment/
│   │   │   ├── staff/
│   │   │   └── ...
│   │   │
│   │   ├── mapper/                    # MapStruct mappers
│   │   │
│   │   ├── exception/                 # Custom exceptions
│   │   │
│   │   ├── event/                     # Event handling
│   │   │
│   │   ├── utils/                     # Utility classes
│   │   │
│   │   └── PizzarIoSwp391Application.java
│   │
│   └── resources/
│       ├── application.yaml           # Main config
│       ├── env.properties             # Environment variables
│       ├── data.sql                   # Initial data
│       ├── static/                    # CSS, JS, images
│       │   ├── css/
│       │   ├── js/
│       │   └── images/
│       └── templates/                 # Thymeleaf HTML templates
│           ├── admin_page/
│           ├── cashier-page/
│           ├── guest-page/
│           ├── kitchen-page/
│           └── authenticate/
│
├── src/test/
│   └── java/                          # Unit & Integration tests
│
│
├── config/
│   └── manager-settings.yaml          # Runtime settings
│
├── pom.xml                            # Maven configuration
├── Dockerfile                         # Docker build
├── compose.yaml                       # Docker Compose
├── mvnw & mvnw.cmd                   # Maven wrapper
├── data.sql                           # Sample data
├── generate-test-data.sql             # Test data (19k+ lines)
└── README.md                          # This file
```

---

## 👤 Các Vai Trò Người Dùng

### 1. 🏢 Quản Lý (Manager)
**Quyền Hạn:**
- Truy cập toàn bộ hệ thống
- Quản lý sản phẩm, danh mục, kích thước
- Quản lý bàn ăn, cấu hình bàn
- Quản lý nhân viên, ca làm việc
- Xem báo cáo và phân tích
- Quản lý mã voucher
- Cấu hình hệ thống chung

**Đường dẫn truy cập:** `/pizzario/manager/*`

### 2. 💳 Nhân Viên Bán Hàng (Cashier)
**Quyền Hạn:**
- Xem bàn ăn, trạng thái bàn
- Xử lý thanh toán đơn hàng
- Quản lý đơn hàng take-away
- In hóa đơn, biên lai
- Xem báo cáo ngày hôm nay

**Đường dẫn truy cập:** `/pizzario/cashier/*`

### 3. 👨‍🍳 Nhân Viên Bếp (Kitchen)
**Quyền Hạn:**
- Xem danh sách đơn hàng cần nấu
- Cập nhật trạng thái đơn hàng (Chuẩn bị → Hoàn thành)
- Xem chi tiết mỗi đơn hàng
- Nhận thông báo real-time qua WebSocket

**Đường dẫn truy cập:** `/pizzario/kitchen/*`

### 4. 👥 Khách Hàng (Guest)
**Quyền Hạn:**
- Xem menu sản phẩm
- Đặt bàn
- Gọi món (nếu online)
- Trò chuyện với chatbot AI
- Xem lịch sử đơn hàng (nếu là thành viên)

**Đường dẫn truy cập:** `/pizzario/guest/*`

---

## 🔗 API và Endpoints

### Endpoints Chính

#### Xác Thực
```
GET  /pizzario/login              - Trang đăng nhập
POST /pizzario/login              - Xử lý đăng nhập
POST /pizzario/logout             - Đăng xuất
GET  /pizzario/send-otp           - Gửi OTP reset mật khẩu
POST /pizzario/verify-otp         - Xác thực OTP
```

#### Manager - Quản Lý Sản Phẩm
```
GET    /pizzario/manager/products       - Danh sách sản phẩm
POST   /pizzario/manager/products       - Thêm sản phẩm
PUT    /pizzario/manager/products/{id}  - Cập nhật sản phẩm
DELETE /pizzario/manager/products/{id}  - Xóa sản phẩm
```

#### Manager - Quản Lý Bàn
```
GET    /pizzario/manager/tables         - Danh sách bàn
POST   /pizzario/manager/tables         - Thêm bàn
PUT    /pizzario/manager/tables/{id}    - Cập nhật bàn
DELETE /pizzario/manager/tables/{id}    - Xóa bàn
GET    /pizzario/manager/tables/{id}    - Chi tiết bàn
```

#### Manager - Quản Lý Nhân Viên
```
GET    /pizzario/manager/staff          - Danh sách nhân viên
POST   /pizzario/manager/staff          - Thêm nhân viên
PUT    /pizzario/manager/staff/{id}     - Cập nhật nhân viên
DELETE /pizzario/manager/staff/{id}     - Xóa nhân viên
```

#### Manager - Quản Lý Ca Làm Việc
```
GET    /pizzario/manager/shifts         - Danh sách ca
POST   /pizzario/manager/shifts         - Tạo ca
GET    /pizzario/manager/staff-shifts   - Lịch ca nhân viên
POST   /pizzario/manager/staff-shifts   - Gán nhân viên ca
```

#### Cashier - Dashboard
```
GET    /pizzario/cashier/dashboard      - Dashboard bán hàng
GET    /pizzario/cashier/tables         - Danh sách bàn
POST   /pizzario/cashier/orders/pay     - Xử lý thanh toán
GET    /pizzario/cashier/sales-report   - Báo cáo doanh số
```

#### Kitchen - Hiển Thị Bếp
```
GET    /pizzario/kitchen/orders         - Danh sách đơn hàng
PUT    /pizzario/kitchen/orders/{id}    - Cập nhật trạng thái đơn
GET    /pizzario/kitchen/order-items/{id} - Chi tiết đơn hàng
```

#### Guest - Menu
```
GET    /pizzario/guest/menu             - Menu sản phẩm
GET    /pizzario/guest/categories       - Danh mục
POST   /pizzario/guest/reservation      - Đặt bàn
```

#### WebSocket
```
WS     /pizzario/ws                     - WebSocket connection
```

---

## 🛠️ Công Nghệ Sử Dụng

### Backend Framework
- **Spring Boot 3.5.6** - Framework chính
- **Spring Data JPA** - ORM (Object-Relational Mapping)
- **Spring Security 6** - Xác thực và ủy quyền
- **Spring WebSocket** - Giao tiếp real-time
- **Spring Mail** - Gửi email OTP
- **Spring Batch** - Xử lý dữ liệu hàng loạt

### Database
- **Microsoft SQL Server** - CSDL chính
- **Hibernate** - ORM provider

### Frontend
- **Thymeleaf** - Rendering HTML server-side
- **Bootstrap 5.3.3** - CSS framework
- **JavaScript/HTML5** - Client-side interactivity

### Code Generation & Mapping
- **Lombok 1.18.x** - Giảm boilerplate code
- **MapStruct 1.5.5** - DTO mapping

### Cloud Services
- **Cloudinary 1.32.0** - Lưu trữ ảnh
- **Google Gemini API 2.5** - AI chatbot

### Excel & Reporting
- **Apache POI 5.2.4** - Xuất dữ liệu Excel

### Build & Tools
- **Maven 3.9+** - Build automation
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **JaCoCo** - Code coverage analysis

### Containerization
- **Docker** - Container images
- **Docker Compose** - Orchestration

### Utilities
- **Apache Commons Text 1.14.0** - String utilities
- **Lombok** - Annotation processing

---

## 📚 Tài Liệu Thiết Kế

Dự án bao gồm tài liệu thiết kế chi tiết trong thư mục `/docs` và trong link Drive(https://drive.google.com/drive/u/0/folders/1YavohRQh3Jv1iwMba-Mb0RCPdQMjsPkF):

### PlantUML Diagrams

```
docs/
├── cashier-dashboard-class-diagram.puml
├── create-staff-sequence.puml
├── edit-staff-sequence.puml
├── guest-payment-session-close.puml
├── guest-select-table.puml
├── kitchen-update-order-item.puml
├── pizzario-package-diagram.puml
├── staff-management-manager-class-diagram.puml
├── view-staff-list-sequence.puml
├── view-table-detail-class-diagram.puml
├── view-table-detail-sequence.puml
└── [Thư mục bổ sung]
    ├── manager/
    ├── staff-sequence/
    ├── table-sequence/
    ├── reservation-sequence/
    └── voucher-sequence/
```

### Xem Diagrams
```bash
# Cài đặt PlantUML viewer
# VSCode extension: PlantUML

# Hoặc dùng online: https://www.plantuml.com/plantuml/uml/
```

## 📞 Liên Hệ

### Thông Tin Dự Án

| Thông Tin | Chi Tiết |
|-----------|---------|
| **Dự Án** | PizzarIO SWP391 |
| **Trường** | FPT University |
| **Môn Học** | SWP391 |
| **Nhóm** | SWP391_Group1 |

### Liên Hệ Nhóm

```
📧 Email: ...
📱 Phone: ...
```
---
## 🎉 Lời Cảm Ơn

Cảm ơn bạn đã sử dụng **PizzarIO**!

Nếu dự án này hữu ích, hãy:
- ⭐ **Star** repository này
- 🔄 **Share** với bạn bè
- 💬 **Feedback** ý kiến của bạn

**Happy Coding! 🚀**

---

**Cập nhật lần cuối:** 2025-12-3 | **Phiên bản:** V1.0
