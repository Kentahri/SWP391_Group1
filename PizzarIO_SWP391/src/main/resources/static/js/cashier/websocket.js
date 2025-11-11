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

    // Subscribe to order updates from guest
    stompClient.subscribe('/topic/order-updates-cashier', function (message) {
        const orderData = JSON.parse(message.body);
        console.log('Order update from guest received:', orderData);
        handleOrderUpdateFromGuest(orderData);
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

/**
 * Handle order update from guest
 * Xử lý khi nhận được cập nhật order từ guest
 */
function handleOrderUpdateFromGuest(orderData) {
    console.log('📦 Nhận order update từ guest:', orderData);

    try {
        // Hiển thị thông báo cho cashier
        let message = 'Order đã được cập nhật bởi guest';
        if (orderData.message) {
            message = orderData.message;
        }

        // Hiển thị toast notification tổng quát
        if (typeof showToast === 'function') {
            // Chỉ hiển thị toast cho các loại update quan trọng
            if (orderData.type === 'NEW_ORDER') {
                const tableName = orderData.tableName || 'N/A';
                showToast(`📦 ${tableName}: Có order mới`, 'info');
            } else if (orderData.type === 'ORDER_COMPLETED') {
                const tableName = orderData.tableName || 'N/A';
                showToast(`✅ ${tableName}: Order đã hoàn thành`, 'success');
            }
            // Bỏ toast cho ORDER_UPDATED và ORDER_ITEM_CANCELLED để tránh spam
        } else {
            console.log('Order update notification:', message);
        }

        // Từ tableName (ví dụ: "Bàn 5") trích xuất tableId
        let tableId = null;
        if (orderData.tableName) {
            const match = orderData.tableName.match(/Bàn\s+(\d+)/i);
            if (match && match[1]) {
                tableId = parseInt(match[1]);
            }
        }

        // Cập nhật trạng thái bàn nếu có tableId
        if (tableId) {
            updateTableStatusForOrder(tableId, orderData.type);
        }

        // Tất cả các loại order update đều cần reload nếu đang xem order detail
        // Bao gồm: NEW_ORDER, ORDER_UPDATED, ORDER_ITEM_CANCELLED, ORDER_COMPLETED
        const currentUrl = window.location.href;
        if (currentUrl.includes('/cashier/tables/') && currentUrl.includes('/order')) {
            // Kiểm tra xem URL có chứa tableId không
            const urlMatch = currentUrl.match(/\/tables\/(\d+)\/order/);
            if (urlMatch && urlMatch[1]) {
                const currentTableId = parseInt(urlMatch[1]);
                
                // Nếu đang xem đúng bàn này, reload sau 1 giây để cập nhật order
                // Áp dụng cho tất cả các loại update: NEW_ORDER, ORDER_UPDATED, ORDER_ITEM_CANCELLED
                if (tableId && currentTableId === tableId) {
                    console.log('Reloading order detail page for table:', tableId, 'update type:', orderData.type);
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                }
            } else if (orderData.orderId) {
                // Nếu không tìm thấy tableId trong URL, thử dùng orderId
                const urlParams = new URLSearchParams(window.location.search);
                const orderIdParam = urlParams.get('orderId');
                
                if (orderIdParam && orderIdParam == orderData.orderId) {
                    console.log('Reloading order detail page for orderId:', orderData.orderId, 'update type:', orderData.type);
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                }
            }
        }
    } catch (error) {
        console.error('Error handling order update from guest:', error);
    }
}

/**
 * Cập nhật trạng thái bàn khi có order mới hoặc cập nhật
 */
function updateTableStatusForOrder(tableId, orderType) {
    if (!tableId) return;

    const tableCard = document.getElementById('table-' + tableId);
    if (!tableCard) {
        console.log('Table card not found for table:', tableId);
        return;
    }

    // Nếu là order mới (NEW_ORDER) và bàn đang AVAILABLE, chuyển sang OCCUPIED
    if (orderType === 'NEW_ORDER') {
        const currentStatus = tableCard.dataset.status;
        if (currentStatus === 'AVAILABLE') {
            // Cập nhật trạng thái bàn
            const isSelected = tableCard.classList.contains('selected');
            tableCard.className = 'table-card occupied';
            if (isSelected) {
                tableCard.classList.add('selected');
            }
            tableCard.dataset.status = 'OCCUPIED';

            // Cập nhật icon
            const tableIcon = tableCard.querySelector('.table-icon');
            if (tableIcon) {
                tableIcon.innerHTML = '<span>👥</span>';
            }

            // Cập nhật status text
            const statusElement = tableCard.querySelector('.table-status-text');
            if (statusElement) {
                statusElement.textContent = 'Có khách';
            }

            // Animation
            tableCard.style.animation = 'none';
            setTimeout(() => {
                tableCard.style.animation = 'slideIn 0.3s ease';
            }, 10);
        }
    } else if (orderType === 'ORDER_UPDATED' || orderType === 'ORDER_ITEM_CANCELLED') {
        // Nếu order được cập nhật hoặc có món bị hủy, chỉ cần animation để báo hiệu
        tableCard.style.animation = 'none';
        setTimeout(() => {
            tableCard.style.animation = 'pulse 0.5s ease';
        }, 10);
    }
}



