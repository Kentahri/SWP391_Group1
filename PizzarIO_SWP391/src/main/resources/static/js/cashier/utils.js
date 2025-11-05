// ==================== NOTIFICATION SYSTEM ====================
const NOTIFICATION_STORAGE_KEY = 'cashier_notifications';
const MAX_NOTIFICATIONS = 50;

/**
 * Lưu notification vào localStorage
 * @param {string} message - Nội dung thông báo
 * @param {string} type - Loại thông báo (success, error, warning, info)
 * @param {object} metadata - Thông tin bổ sung (tableId, reservationId, orderId, etc.)
 */
function saveNotification(message, type = 'info', metadata = {}) {
    const notifications = getNotifications();
    const notification = {
        id: Date.now(),
        message: message,
        type: type,
        timestamp: new Date().toISOString(),
        read: false,
        metadata: metadata // Lưu thêm metadata (tableId, reservationId, orderId, etc.)
    };

    notifications.unshift(notification);

    if (notifications.length > MAX_NOTIFICATIONS) {
        notifications.pop();
    }

    localStorage.setItem(NOTIFICATION_STORAGE_KEY, JSON.stringify(notifications));
    updateNotificationBadge();
    return notification;
}

/**
 * Lấy tất cả notifications
 */
function getNotifications() {
    const stored = localStorage.getItem(NOTIFICATION_STORAGE_KEY);
    return stored ? JSON.parse(stored) : [];
}

/**
 * Cập nhật badge số lượng thông báo chưa đọc
 */
function updateNotificationBadge() {
    const notifications = getNotifications();
    const unreadCount = notifications.filter(n => !n.read).length;
    const badge = document.getElementById('notification-badge');

    if (badge) {
        if (unreadCount > 0) {
            badge.textContent = unreadCount > 99 ? '99+' : unreadCount;
            badge.style.display = 'inline-block';
        } else {
            badge.style.display = 'none';
        }
    }
}

/**
 * Mở modal notifications
 */
function openNotificationModal() {
    const modal = document.getElementById('notification-modal');
    if (modal) {
        modal.classList.add('show');
        renderNotifications();
        markAllAsRead();
    }
}

/**
 * Đóng modal notifications
 */
function closeNotificationModal() {
    const modal = document.getElementById('notification-modal');
    if (modal) {
        modal.classList.remove('show');
    }
}

/**
 * Render danh sách notifications
 */
function renderNotifications() {
    const notifications = getNotifications();
    const listContainer = document.getElementById('notification-list');

    if (!listContainer) return;

    if (notifications.length === 0) {
        listContainer.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #999;">
                <div style="font-size: 48px; margin-bottom: 16px;">🔔</div>
                <div>Chưa có thông báo nào</div>
            </div>
        `;
        return;
    }

    listContainer.innerHTML = notifications.map(notification => {
        // Tạo metadata display string
        let metadataDisplay = '';
        if (notification.metadata && Object.keys(notification.metadata).length > 0) {
            const meta = notification.metadata;
            const metaParts = [];
            if (meta.tableId) metaParts.push(`Bàn ${meta.tableId}`);
            if (meta.reservationId) metaParts.push(`#${meta.reservationId}`);
            if (meta.orderId) metaParts.push(`Đơn #${meta.orderId}`);
            if (metaParts.length > 0) {
                metadataDisplay = `<div class="notification-meta">${metaParts.join(' • ')}</div>`;
            }
        }

        return `
            <div class="notification-item ${notification.type}" data-id="${notification.id}">
                <div class="notification-content">
                    <div class="notification-message">${notification.message}</div>
                    ${metadataDisplay}
                    <div class="notification-time">${formatNotificationTime(notification.timestamp)}</div>
                </div>
                <button class="notification-delete" onclick="deleteNotification(${notification.id})">
                    ✕
                </button>
            </div>
        `;
    }).join('');
}

/**
 * Format thời gian hiển thị
 */
function formatNotificationTime(timestamp) {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;

    if (diff < 60000) {
        return 'Vừa xong';
    }
    if (diff < 3600000) {
        const minutes = Math.floor(diff / 60000);
        return minutes + ' phút trước';
    }
    if (diff < 86400000) {
        const hours = Math.floor(diff / 3600000);
        return hours + ' giờ trước';
    }
    return date.toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Đánh dấu tất cả là đã đọc
 */
function markAllAsRead() {
    const notifications = getNotifications();
    notifications.forEach(n => n.read = true);
    localStorage.setItem(NOTIFICATION_STORAGE_KEY, JSON.stringify(notifications));
    updateNotificationBadge();
}

/**
 * Xóa một notification
 */
function deleteNotification(id) {
    const notifications = getNotifications();
    const filtered = notifications.filter(n => n.id !== id);
    localStorage.setItem(NOTIFICATION_STORAGE_KEY, JSON.stringify(filtered));
    renderNotifications();
    updateNotificationBadge();
}

/**
 * Xóa tất cả notifications
 */
function clearAllNotifications() {
    if (confirm('Bạn có chắc muốn xóa tất cả thông báo?')) {
        localStorage.removeItem(NOTIFICATION_STORAGE_KEY);
        renderNotifications();
        updateNotificationBadge();
    }
}

/**
 * Khởi tạo notification system
 */
function initNotificationSystem() {
    // Cập nhật badge khi load trang
    updateNotificationBadge();

    // Click outside modal to close
    document.addEventListener('click', function(event) {
        const modal = document.getElementById('notification-modal');
        if (event.target === modal) {
            closeNotificationModal();
        }
    });

    // Log để debug
    console.log('Notification system initialized. Total notifications:', getNotifications().length);
}

// ==================== TOAST SYSTEM ====================
/**
 * Hiện thông báo đẩy
 * @param {string} message - Nội dung thông báo
 * @param {string} type - Loại thông báo (success, error, warning, info)
 * @param {object} metadata - Thông tin bổ sung (tableId, reservationId, orderId, etc.)
 */
function showToast(message, type = 'info', metadata = {}) {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = 'toast ' + type;
    toast.textContent = message;

    container.appendChild(toast);

    // Lưu vào notification history với metadata
    saveNotification(message, type, metadata);

    setTimeout(() => {
        toast.style.animation = 'slideInRight 0.3s ease reverse';
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 5000);
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
 * Format date to datetime-local input format
 */
function formatDateTimeLocal(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${year}-${month}-${day}T${hours}:${minutes}`;
}

/**
 * Cập nhật trạng thái kết nối server
 */
function updateServerStatus(status, message = '') {
    const statusElement = document.getElementById('server-status');
    if (!statusElement) return;
    
    const statusConfig = {
        connecting: {
            icon: '🔄',
            text: 'Đang kết nối...',
            class: 'status-connecting'
        },
        connected: {
            icon: '✅',
            text: 'Đã kết nối',
            class: 'status-connected'
        },
        disconnected: {
            icon: '⚠️',
            text: 'Mất kết nối',
            class: 'status-disconnected'
        },
        error: {
            icon: '❌',
            text: 'Lỗi kết nối',
            class: 'status-error'
        }
    };
    
    const config = statusConfig[status] || statusConfig.disconnected;
    
    statusElement.innerHTML = `
        <span class="status-label">Trạng thái kết nối đến server:</span>
        <span class="status-indicator ${config.class}">
            <span class="status-icon">${config.icon}</span>
            <span class="status-text">${config.text}</span>
            ${message ? `<span class="status-message">${message}</span>` : ''}
        </span>
    `;
    
    // Thêm class vào element
    statusElement.className = 'server-status ' + config.class;
}