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