/**
 * Xử lý khi thay đổi status của bàn qua WebSocket
 */
function handleTableUpdate(update) {
    console.log('Handling table update:', update);

    // Xử lý trường hợp bàn bị retired
    if (update.type === 'TABLE_RETIRED') {
        handleTableRetired(update);
        return;
    }

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

        // Update status text
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

        // Animation
        tableCard.style.animation = 'none';
        setTimeout(() => {
            tableCard.style.animation = 'slideIn 0.3s ease';
        }, 10);
    }

    // Show notification - Ưu tiên message từ backend
    let message;

    if (update.message) {
        // Dùng message từ backend (ưu tiên cao nhất)
        message = update.message;
    } else {
        // Fallback: Dùng message mặc định theo type
        const defaultMessages = {
            'TABLE_OCCUPIED': `🟢 Bàn ${update.tableId} đã được khách chọn`,
            'TABLE_RELEASED': `✅ Bàn ${update.tableId} đã được giải phóng`,
            'TABLE_RESERVED': `📅 Bàn ${update.tableId} đã được đặt trước`,
            'TABLE_PAYMENT_PENDING': `💰 Bàn ${update.tableId} đang chờ thanh toán`
        };
        message = defaultMessages[update.type] || `Bàn ${update.tableId} cập nhật`;
    }

    showToast(message, 'info');
}

/**
 * Xử lý khi bàn được đánh dấu retired
 */
function handleTableRetired(update) {
    console.log('Handling table retired:', update);

    const tableCard = document.getElementById('table-' + update.tableId);
    if (tableCard) {
        // Thêm animation fade out trước khi xóa
        tableCard.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
        tableCard.style.opacity = '0';
        tableCard.style.transform = 'scale(0.8)';

        // Xóa bàn khỏi UI sau animation
        setTimeout(() => {
            tableCard.remove();
        }, 500);
    }

    // Hiển thị thông báo
    const message = update.message || `🔒 Bàn ${update.tableId} đã được đánh dấu retired và sẽ không hiển thị nữa`;
    showToast(message, 'warning');
}
