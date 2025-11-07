'use strict';

let stompClient = null;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;

// Load cấu trúc cây DOM của html
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

    // Subscribe to payment pending notifications
    stompClient.subscribe('/topic/payment-pending', function (message) {
        const paymentData = JSON.parse(message.body);
        console.log('Payment pending received:', paymentData);
        handlePaymentPending(paymentData);
    });
}

/**
 *  Hiện thông báo khi đã thành công kết nối
 */
function onConnected(frame) {
    console.log('WebSocket connected:', frame);
    reconnectAttempts = 0;
    subscribeToTopics();
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

/**
 * Handle payment pending notification from guest
 */
function handlePaymentPending(paymentData) {
    console.log('📨 Nhận payment message:', paymentData);

    if (paymentData.type !== 'PAYMENT_PENDING') {
        console.log('⏭️ Bỏ qua vì type không phải PAYMENT_PENDING:', paymentData.type);
        return;
    }

    console.log('✅ Type = PAYMENT_PENDING, bắt đầu xử lý...');
    hideEditOrderButton();
    openConfirmPaymentButton(paymentData.paymentMethod);

    // Update payment notification badge
    updatePaymentNotificationBadge();

    // Toast thông báo bàn đang chờ thanh toán
    try {
        var tableLabel = paymentData.tableName || (paymentData.tableNumber ? ('Bàn ' + paymentData.tableNumber) : 'Bàn');
        var metadata = {};
        if (paymentData.tableNumber) metadata.tableId = paymentData.tableNumber;
        if (paymentData.orderId) metadata.orderId = paymentData.orderId;
        // showToast('💰 ' + tableLabel + ' đang chờ thanh toán', 'info', metadata);
    } catch (e) { /* ignore */
    }

    // Show payment confirmation modal
    showPaymentConfirmationModal(paymentData);
}

function openConfirmPaymentButton(paymentMethod) {
    console.log('🔍 openConfirmPaymentButton() được gọi với paymentMethod:', paymentMethod);

    // Tìm container chứa các nút thanh toán
    const paymentButtonsContainer = document.getElementById('payment-buttons-container');

    if (!paymentButtonsContainer) {
        console.warn('⚠️ Không tìm thấy #payment-buttons-container');
        return;
    }

    // Tìm 2 nút
    const cashButton = paymentButtonsContainer.querySelector('.btn-confirm-cash');
    const qrButton = paymentButtonsContainer.querySelector('.btn-confirm-qr');

    if (cashButton) cashButton.style.display = 'none';
    if (qrButton) qrButton.style.display = 'none';

    if (paymentMethod === 'CASH' && cashButton) {
        cashButton.style.display = 'inline-block';
    } else if (paymentMethod === 'QR_BANKING' && qrButton) {
        qrButton.style.display = 'inline-block';
    } else {
        if (cashButton) cashButton.style.display = 'inline-block';
        if (qrButton) qrButton.style.display = 'inline-block';
    }

    paymentButtonsContainer.style.display = 'block';
}

/**
 * Ẩn nút "Thêm món" khi đã thanh toán
 */
function hideEditOrderButton() {
    const editButton = document.querySelector('.btn-edit-order');

    if (editButton) {
        editButton.style.setProperty('display', 'none', 'important');
        console.log('✅ Đã ẩn nút "Thêm món" với !important');
    } else {
        console.warn('⚠️ Không tìm thấy nút .btn-edit-order');
    }
}

/**
 * Update payment notification badge count
 */
function updatePaymentNotificationBadge() {
    const badge = document.getElementById('payment-notification-badge');
    if (badge) {
        const currentCount = parseInt(badge.textContent) || 0;
        badge.textContent = currentCount + 1;
        badge.style.display = 'inline-block';
    }
}

/**
 * Show payment confirmation modal with payment details
 */
function showPaymentConfirmationModal(paymentData) {
    const modal = document.getElementById('paymentConfirmationModal');
    if (!modal) {
        console.error('Payment confirmation modal not found');
        return;
    }

    // Populate modal with payment data
    document.getElementById('payment-table-number').textContent = paymentData.tableNumber || 'N/A';
    document.getElementById('payment-customer-name').textContent = paymentData.customerName || 'Khách vãng lai';
    document.getElementById('payment-total-amount').textContent =
        new Intl.NumberFormat('vi-VN', {style: 'currency', currency: 'VND'}).format(paymentData.totalAmount || 0);
    document.getElementById('payment-method').textContent =
        paymentData.paymentMethod === 'QR_BANKING' ? 'QR Banking' : 'Tiền mặt';
    document.getElementById('payment-session-id').value = paymentData.sessionId;

    // Show modal
    modal.style.display = 'block';
}



