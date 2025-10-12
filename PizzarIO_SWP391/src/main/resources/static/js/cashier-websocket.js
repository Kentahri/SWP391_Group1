'use strict';

window.currentStaffId = /*[[${staff != null ? staff.id : 'cashier-01'}]]*/ 'cashier-01';
window.currentStaffName = /*[[${staff != null ? staff.name : 'Staff'}]]*/ 'Staff';
let stompClient = null;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;

window.addEventListener('DOMContentLoaded', function () {
    connectWebSocket();
});

/**
 *  Kết nối WebSocket và thiết lập STOMP client
 */
function connectWebSocket() {
    const base = '/pizzario';
    const socket = new SockJS(base.replace(/\/$/, '') + '/ws');
    stompClient = Stomp.over(socket);
    if (window.location.hostname !== 'localhost') {
        stompClient.debug = null;
    }
    stompClient.connect({}, onConnected, onError);
}

/**
 * Kêt nối và đăng ký các topic mà cashier cần theo dõi
 */
function subscribeToTopics() {
    // Kênh chung cho Guest và Cashier để xem trạng thái của bàn
    stompClient.subscribe('/topic/tables-cashier', function (message) {
        const update = JSON.parse(message.body);
        console.log('Table update received:', update);
        handleTableUpdate(update);
    });

    // Kênh riêng cho nhân viên thu ngân để nhận thông báo
    stompClient.subscribe('/queue/cashier-' + window.currentStaffId, function (message) {
        const notification = JSON.parse(message.body);
        console.log('Personal notification:', notification);
        showToast(notification.message || notification, 'info');
    });
}

/**
 *  Hiện thông báo khi đã thành công kết nối
 */
function onConnected(frame) {
    console.log('WebSocket connected:', frame);
    reconnectAttempts = 0;
    subscribeToTopics();
    showToast('Kết nối real-time thành công!', 'success');
    addNotification('Hệ thống real-time đã sẵn sàng', 'success');
}

/**
 * Khi bị mất kết nối, cố gắng kết nối lại
 */
function onError(error) {
    console.error('WebSocket error:', error);

    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
        const delay = Math.pow(2, reconnectAttempts) * 1000;
        console.log(`Reconnecting in ${delay}ms... (Attempt ${reconnectAttempts + 1}/${MAX_RECONNECT_ATTEMPTS})`);

        setTimeout(function () {
            reconnectAttempts++;
            connectWebSocket();
        }, delay);
    } else {
        showToast('Mất kết nối. Vui lòng refresh trang.', 'error');
        addNotification('Mất kết nối WebSocket. Vui lòng refresh trang.', 'error');
    }
}

/**
 * Xử lý khi thay đổi status của bàn
 */
function handleTableUpdate(update) {
    console.log('Handling table update:', update);

    // Update table UI
    const tableCard = document.getElementById('table-' + update.tableId);
    if (tableCard) {
        // Kiểm tra xem bàn có đang selected không trước khi update className
        const isSelected = tableCard.classList.contains('selected');

        // Update status class (giữ lại selected nếu có)
        tableCard.className = 'table-card ' + update.newStatus.toLowerCase();
        if (isSelected) {
            tableCard.classList.add('selected');
        }

        // Update data attribute
        tableCard.dataset.status = update.newStatus;

        // Update status text (sửa selector cho đúng)
        const statusElement = tableCard.querySelector('.table-status-text');
        if (statusElement) {
            // Map status sang tiếng Việt
            const statusMap = {
                'AVAILABLE': 'Trống',
                'OCCUPIED': 'Có khách',
                'WAITING_PAYMENT': 'Chờ thanh toán',
                'RESERVED': 'Đã đặt trước'
            };
            statusElement.textContent = statusMap[update.newStatus] || update.newStatus;
        }

        // Nếu bàn này đang được chọn, update luôn panel chi tiết bên phải
        if (isSelected) {
            const ds = tableCard.dataset;
            const container = document.getElementById('table-detail');
            console.log('Updating detail panel for selected table:', update.tableId);
            container.innerHTML = `
                    <div class="detail-card">
                        <div class="detail-row"><strong>Bàn số:</strong> ${ds.id || '—'}</div>
                        <div class="detail-row"><strong>Trạng thái:</strong> ${ds.status || '—'}</div>
                        <div class="detail-row"><strong>Số người tối đa:</strong> ${ds.capacity || '—'}</div>
                    </div>
                `;
        }

        // Animation
        tableCard.style.animation = 'none';
        setTimeout(() => {
            tableCard.style.animation = 'slideIn 0.3s ease';
        }, 10);
    }

    // Show notification
    const messages = {
        'TABLE_OCCUPIED': `🟢 Bàn ${update.tableId} đã được khách chọn`,
        'TABLE_RELEASED': `✅ Bàn ${update.tableId} đã được giải phóng`,
        'TABLE_RESERVED': `📅 Bàn ${update.tableId} đã được đặt trước`,
        'TABLE_PAYMENT_PENDING': `💰 Bàn ${update.tableId} đang chờ thanh toán`
    };

    const message = messages[update.type] || update.message || `Bàn ${update.tableId} cập nhật`;
    showToast(message, 'info');
    addNotification(message, 'info');
}

/**
 * Xử lý để cho n hiện cái bàn và chi tiết của bàn ở bên phải khi nhấn vào bàn ở bên trái
 */
window.handleTableClick = function (tableId) {
    // Lấy tất cả các bàn, bỏ selected
    const allCards = document.querySelectorAll('.table-card');
    allCards.forEach(c => c.classList.remove('selected'));

    const card = document.getElementById('table-' + tableId);
    if (!card) return;
    card.classList.add('selected');

    const ds = card.dataset;
    const container = document.getElementById('table-detail');
    if (!container) return;

    // Kiểm tra nếu bàn đang có khách hoặc chờ thanh toán, gọi API lấy order detail
    const status = ds.status;
    if (status === 'OCCUPIED' || status === 'WAITING_PAYMENT') {
        // Hiệu ứng load
        container.innerHTML = `
            <div class="detail-loading">
                <div class="loading-spinner"></div>
                <p>Đang tải thông tin đơn hàng...</p>
            </div>
        `;

        // Fetch chi tiết của order
        const base = '/pizzario';
        fetch(base.replace(/\/$/, '') + '/api/cashier/tables/' + tableId + '/order')
            .then(response => response.json())
            .then(orderDetail => {
                if (orderDetail && orderDetail.items) {
                    displayOrderDetail(orderDetail);
                } else {
                    displayEmptyTable(ds);
                }
            })
            .catch(error => {
                console.error('Error fetching order detail:', error);
                container.innerHTML = `
                    <div class="detail-error">
                        <p>❌ Không thể tải thông tin đơn hàng</p>
                        <button class="btn small" onclick="handleTableClick(${tableId})">Thử lại</button>
                    </div>
                `;
            });
    } else {
        // Bàn trống hoặc đã đặt trước - chỉ hiển thị thông tin cơ bản
        displayEmptyTable(ds);
    }
}

/**
 * Hiển thị order detail
 */
function displayOrderDetail(orderDetail) {
    const container = document.getElementById('table-detail');

    // Map status to Vietnamese
    const statusMap = {
        'PREPARING': 'Đang chuẩn bị',
        'SERVED': 'Đã phục vụ',
        'COMPLETED': 'Hoàn thành',
        'CANCELLED': 'Đã hủy',
        'PENDING': 'Chờ xử lý'
    };

    const paymentStatusMap = {
        'UNPAID': 'Chưa thanh toán',
        'PENDING': 'Đang xử lý',
        'PAID': 'Đã thanh toán'
    };

    const paymentMethodMap = {
        'CASH': 'Tiền mặt',
        'QR': 'QR Code',
        'CREDIT_CARD': 'Thẻ tín dụng'
    };

    // Build items HTML
    let itemsHtml = '';
    if (orderDetail.items && orderDetail.items.length > 0) {
        itemsHtml = orderDetail.items.map(item => `
            <div class="order-item">
                <div class="order-item-info">
                    <div class="item-name">${item.productName}</div>
                    <div class="item-detail">
                        <span class="item-quantity">x${item.quantity}</span>
                        <span class="item-price">${formatCurrency(item.unitPrice)}</span>
                    </div>
                    ${item.note ? `<div class="item-note">📝 ${item.note}</div>` : ''}
                </div>
                <div class="item-total">${formatCurrency(item.totalPrice)}</div>
            </div>
        `).join('');
    } else {
        itemsHtml = '<div class="no-items">Chưa có món nào</div>';
    }

    const totalItems = orderDetail.items ? orderDetail.items.reduce((sum, item) => sum + item.quantity, 0) : 0;

    container.innerHTML = `
        <div class="order-detail-card">
            <div class="order-header">
                <h3>${orderDetail.tableName}</h3>
                <span class="order-status ${orderDetail.orderStatus.toLowerCase()}">${statusMap[orderDetail.orderStatus] || orderDetail.orderStatus}</span>
            </div>
            
            <div class="order-meta">
                <div class="meta-row">
                    <span class="meta-label">Mã đơn:</span>
                    <span class="meta-value">#${orderDetail.orderId}</span>
                </div>
                <div class="meta-row">
                    <span class="meta-label">Trạng thái thanh toán:</span>
                    <span class="meta-value payment-${orderDetail.paymentStatus.toLowerCase()}">${paymentStatusMap[orderDetail.paymentStatus] || orderDetail.paymentStatus}</span>
                </div>
                ${orderDetail.paymentMethod ? `
                <div class="meta-row">
                    <span class="meta-label">Phương thức:</span>
                    <span class="meta-value">${paymentMethodMap[orderDetail.paymentMethod] || orderDetail.paymentMethod}</span>
                </div>
                ` : ''}
                ${orderDetail.createdByStaffName ? `
                <div class="meta-row">
                    <span class="meta-label">Nhân viên:</span>
                    <span class="meta-value">${orderDetail.createdByStaffName}</span>
                </div>
                ` : ''}
            </div>

            <div class="order-items-section">
                <div class="section-header-small">
                    <span>Món đã gọi</span>
                    <span class="item-count">${totalItems} món</span>
                </div>
                <div class="order-items-list">
                    ${itemsHtml}
                </div>
            </div>

            <div class="order-summary">
                ${orderDetail.voucherCode ? `
                <div class="summary-row">
                    <span>Voucher:</span>
                    <span class="voucher-code">${orderDetail.voucherCode}</span>
                </div>
                <div class="summary-row discount">
                    <span>Giảm giá:</span>
                    <span>-${formatCurrency(orderDetail.discountAmount || 0)}</span>
                </div>
                ` : ''}
                <div class="summary-row total">
                    <span>Tổng cộng:</span>
                    <span class="total-amount">${formatCurrency(orderDetail.totalPrice)}</span>
                </div>
            </div>

            ${orderDetail.note ? `
            <div class="order-note">
                <strong>Ghi chú:</strong> ${orderDetail.note}
            </div>
            ` : ''}
        </div>
    `;
}

/**
 * Hiển thị thông tin bàn trống
 */
function displayEmptyTable(ds) {
    const container = document.getElementById('table-detail');
    container.innerHTML = `
        <div class="detail-card">
            <div class="detail-row"><strong>Bàn số:</strong> ${ds.id || '—'}</div>
            <div class="detail-row"><strong>Trạng thái:</strong> ${ds.status || '—'}</div>
            <div class="detail-row"><strong>Số người tối đa:</strong> ${ds.capacity || '—'}</div>
        </div>
    `;
}

/**
 * Format lại tiền tệ
 */
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}


/**
 * Hiện thông báo đẩy
 */
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = 'toast ' + type;
    toast.textContent = message;

    container.appendChild(toast);

    // Auto remove after 5 seconds
    setTimeout(() => {
        toast.style.animation = 'slideInRight 0.3s ease reverse';
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 5000);
}

/**
 * Add notification to panel
 */
function addNotification(message, type = 'info') {
    const list = document.getElementById('notifications-list');
    if (!list) return;

    // Remove "no notifications" message
    const noNotif = list.querySelector('.no-notifications');
    if (noNotif) {
        noNotif.remove();
    }

    // Create notification item
    const item = document.createElement('div');
    item.className = 'notification-item ' + type;

    const now = new Date();
    const timeString = now.toLocaleTimeString('vi-VN');

    item.innerHTML = `
            <div class="notification-time">${timeString}</div>
            <div class="notification-message">${message}</div>
        `;

    // Add to top
    list.insertBefore(item, list.firstChild);

    // Limit to 20 notifications
    while (list.children.length > 20) {
        list.removeChild(list.lastChild);
    }
}

/**
 * Cleanup on page unload
 */
window.addEventListener('beforeunload', function () {
    if (stompClient && stompClient.connected) {
        stompClient.disconnect(function () {
            console.log('WebSocket disconnected on page unload');
        });
    }
});



