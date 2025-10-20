# Tính năng: Mở bàn cho khách đã đặt trước

## 📋 Tổng quan

Tính năng này cho phép Cashier mở bàn (chuyển từ **RESERVED** → **OCCUPIED**) khi khách hàng đã đặt bàn trước đến nhà hàng.

## 🎯 Mục đích

1. **Xác nhận khách đến**: Đánh dấu khách hàng đã check-in
2. **Mở bàn cho khách**: Chuyển bàn sang trạng thái OCCUPIED để bắt đầu phục vụ
3. **Hủy scheduled task**: Cancel task tự động đánh dấu NO_SHOW (vì khách đã đến)
4. **Theo dõi**: Cập nhật status reservation thành ARRIVED

## 🔧 Cài đặt kỹ thuật

### 1. Controller Layer

**File:** `CashierDashboardController.java`

**Endpoint:** `POST /cashier/reservations/{id}/open`

```java
@PostMapping("/reservations/{id}/open")
public String openTableForReservation(
        @PathVariable Long id,                      // Reservation ID
        RedirectAttributes redirectAttributes,      // Flash messages
        @RequestParam(required = false) String returnUrl) {  // Redirect URL
    try {
        reservationService.openTableForGuestWithReservation(id);
        redirectAttributes.addFlashAttribute("successMessage", 
            "Đã mở bàn cho khách. Chúc phục vụ vui vẻ!");
        
        if (returnUrl != null && !returnUrl.isEmpty()) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/cashier";
    } catch (RuntimeException e) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/cashier";
    }
}
```

**Tham số:**
- `id`: Reservation ID
- `returnUrl`: URL để redirect sau khi xử lý (optional)

**Return:**
- Redirect về trang trước đó hoặc `/cashier`
- Kèm flash message thành công/lỗi

---

### 2. Service Layer

**File:** `ReservationService.java`

**Method:** `openTableForGuestWithReservation(Long reservationId)`

```java
@Transactional
public void openTableForGuestWithReservation(Long reservationId) {
    // 1. Cancel scheduled NO_SHOW task
    reservationSchedulerService.cancelNoShowCheck(reservationId);
    
    // 2. Find reservation
    Reservation reservation = reservationRepository.findById(reservationId)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy reservation"));
    
    // 3. Update reservation status
    reservation.setStatus(Reservation.Status.ARRIVED);
    
    // 4. Update table status
    DiningTable table = reservation.getDiningTable();
    table.setTableStatus(DiningTable.TableStatus.OCCUPIED);
    
    // 5. Save changes
    tableRepository.save(table);
    reservationRepository.save(reservation);
    
    // 6. Broadcast via WebSocket (optional)
    broadcastTableStatus(...);
}
```

**Logic flow:**
1. ✅ Cancel scheduled NO_SHOW task (quan trọng!)
2. ✅ Lấy thông tin reservation
3. ✅ Cập nhật reservation status: `CONFIRMED` → `ARRIVED`
4. ✅ Cập nhật table status: `RESERVED` → `OCCUPIED`
5. ✅ Lưu vào database
6. ✅ Broadcast WebSocket (real-time update)

---

### 3. View Layer (Thymeleaf)

**File:** `cashier-dashboard.html`

#### 3.1. Nút mở bàn trong "Upcoming Reservations View"

```html
<div th:if="${reservation.status.toString() == 'CONFIRMED'}" 
     class="reservation-actions" 
     style="display: flex; flex-direction: column; gap: 8px;">
    
    <!-- Nút mở bàn cho khách (nút chính) -->
    <form th:action="@{/cashier/reservations/{id}/open(id=${reservation.id})}" 
          method="post" 
          onsubmit="return confirm('Xác nhận khách đã đến và mở bàn?');">
        <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
        <input type="hidden" name="returnUrl" value="/cashier/reservations/upcoming"/>
        <button type="submit" class="btn-open-table">
            🔓 Mở bàn cho khách
        </button>
    </form>
    
    <!-- Các nút phụ (Sửa, Hủy) -->
    <div style="display: flex; gap: 8px;">
        <a href="..." class="btn-edit-reservation">✏️ Sửa</a>
        <form action="..." method="post">
            <button type="submit" class="btn-cancel-reservation">❌ Hủy đặt bàn</button>
        </form>
    </div>
</div>
```

#### 3.2. Nút mở bàn trong "Table Reservations View"

Tương tự như trên, chỉ khác `returnUrl`:

```html
<input type="hidden" name="returnUrl" 
       th:value="'/cashier/tables/' + ${selectedTableId} + '/reservations'"/>
```

---

### 4. CSS Styling

**File:** `cashier-dashboard.css`

```css
.btn-open-table {
    width: 100%;
    padding: 12px;
    background-color: #2196F3;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-weight: bold;
    font-size: 15px;
    transition: all 0.3s ease;
    box-shadow: 0 2px 4px rgba(33, 150, 243, 0.3);
}

.btn-open-table:hover {
    background-color: #1976D2 !important;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(33, 150, 243, 0.4);
}

.btn-open-table:active {
    transform: translateY(0);
    box-shadow: 0 2px 4px rgba(33, 150, 243, 0.3);
}
```

---

## 🚀 Cách sử dụng

### Luồng sử dụng từ Cashier:

1. **Khách hàng đến quầy**
   - Khách: "Tôi đã đặt bàn trước"
   - Cashier kiểm tra thông tin

2. **Xem danh sách đặt bàn**
   - Click "Xem đơn hàng đặt trước" trên header
   - Hoặc click vào bàn RESERVED → "Xem đơn đặt trước"

3. **Tìm reservation của khách**
   - Dùng search box: nhập tên, SĐT, hoặc số bàn
   - Tìm reservation card tương ứng

4. **Mở bàn**
   - Click nút **"🔓 Mở bàn cho khách"** (nút màu xanh dương)
   - Xác nhận dialog: "Xác nhận khách đã đến và mở bàn?"
   - Click "OK"

5. **Kết quả**
   - ✅ Thông báo: "Đã mở bàn cho khách. Chúc phục vụ vui vẻ!"
   - Bàn chuyển sang màu **đỏ** (OCCUPIED)
   - Reservation status: ARRIVED
   - Scheduled NO_SHOW task đã bị cancel

---

## 🎨 UI/UX

### Vị trí nút:
- **Nút chính** (màu xanh dương, to hơn):
  - 🔓 Mở bàn cho khách

- **Các nút phụ** (nhỏ hơn, cùng hàng):
  - ✏️ Sửa (màu xanh lá)
  - ❌ Hủy đặt bàn (màu đỏ)

### Màu sắc:
- **Mở bàn**: `#2196F3` (xanh dương) → hover: `#1976D2`
- **Sửa**: `#4CAF50` (xanh lá)
- **Hủy**: `#F44336` (đỏ)

### Hiệu ứng:
- Hover: Nút nhấc lên (`translateY(-2px)`)
- Click: Nút ấn xuống
- Transition mượt mà (0.3s)

---

## 📊 State Transitions

### Table Status:
```
RESERVED → OCCUPIED
   ↓
(khi click "Mở bàn")
```

### Reservation Status:
```
CONFIRMED → ARRIVED
   ↓
(khi click "Mở bàn")
```

### Scheduled Task:
```
ACTIVE → CANCELED
   ↓
(khi click "Mở bàn")
```

---

## ⚠️ Lưu ý quan trọng

1. **Chỉ hiển thị với status CONFIRMED**
   - Nút chỉ hiện khi `reservation.status == 'CONFIRMED'`
   - Không hiện với CANCELED, ARRIVED, NO_SHOW

2. **CSRF Protection**
   - Tất cả forms đều có CSRF token
   - Bảo mật chống tấn công CSRF

3. **Confirmation Dialog**
   - User phải xác nhận trước khi mở bàn
   - Tránh click nhầm

4. **Transaction Rollback**
   - Method có `@Transactional`
   - Nếu có lỗi, tất cả thay đổi sẽ rollback

5. **Cancel Scheduled Task**
   - **Rất quan trọng**: Phải cancel task NO_SHOW
   - Nếu không, sau 15 phút task vẫn chạy và đánh dấu NO_SHOW

---

## 🐛 Troubleshooting

### Vấn đề 1: Nút không hiển thị
**Nguyên nhân:**
- Reservation status không phải CONFIRMED
- Thymeleaf condition sai

**Giải pháp:**
- Kiểm tra `reservation.status` trong database
- Debug: `th:text="${reservation.status}"` để xem giá trị

---

### Vấn đề 2: Click nút không có phản ứng
**Nguyên nhân:**
- CSRF token thiếu hoặc sai
- URL endpoint sai

**Giải pháp:**
- Kiểm tra CSRF token trong form
- Kiểm tra console browser có lỗi 403 không
- Verify URL: `POST /cashier/reservations/{id}/open`

---

### Vấn đề 3: Task NO_SHOW vẫn chạy sau khi mở bàn
**Nguyên nhân:**
- Không gọi `cancelNoShowCheck()`

**Giải pháp:**
- Đảm bảo `reservationSchedulerService.cancelNoShowCheck(id)` được gọi
- Check log xem task có bị cancel không

---

## 📚 Related Files

- `/docs/open-reserved-table-flow.puml` - Sequence diagram
- `/docs/table-reservation-state-diagram.puml` - State machine diagram
- `/docs/reservation-auto-no-show.puml` - Auto NO_SHOW flow
- `/docs/reservation-scheduler-architecture.puml` - Architecture diagram

---

## ✅ Testing Checklist

- [ ] Click nút "Mở bàn" thành công
- [ ] Reservation status chuyển thành ARRIVED
- [ ] Table status chuyển thành OCCUPIED
- [ ] Scheduled task bị cancel
- [ ] Flash message hiển thị đúng
- [ ] Redirect về trang đúng (returnUrl)
- [ ] CSRF protection hoạt động
- [ ] Confirmation dialog hiển thị
- [ ] UI responsive trên mobile
- [ ] WebSocket broadcast (nếu có)

---

## 🎓 Kiến thức mở rộng

### Tại sao cần cancel scheduled task?

Khi tạo reservation, hệ thống tự động schedule một task chạy sau 15 phút để:
- Kiểm tra xem khách có đến không
- Nếu không đến → đánh dấu NO_SHOW → mở lại bàn

**Nhưng nếu khách đã đến:**
- Task không cần chạy nữa
- Phải cancel để tiết kiệm tài nguyên
- Tránh logic conflict

### Tại sao dùng POST không dùng GET?

- Thay đổi state (RESERVED → OCCUPIED)
- Theo RESTful convention, thay đổi state dùng POST
- Bảo mật hơn (CSRF protection)
- Không bị cache bởi browser

### Tại sao cần returnUrl?

- Cashier có thể mở bàn từ nhiều màn hình khác nhau:
  - Danh sách đặt bàn sắp tới
  - Danh sách đặt bàn của bàn cụ thể
- `returnUrl` giúp redirect về đúng màn hình mà user đang xem

---

## 📝 Changelog

**2024-10-19:**
- ✅ Hoàn thiện controller endpoint
- ✅ Thêm nút "Mở bàn" vào 2 views (upcoming & table reservations)
- ✅ Thêm CSS styling với hover effect
- ✅ Tạo documentation và UML diagrams

---

**Tác giả:** AI Assistant  
**Ngày tạo:** 19/10/2024  
**Phiên bản:** 1.0



