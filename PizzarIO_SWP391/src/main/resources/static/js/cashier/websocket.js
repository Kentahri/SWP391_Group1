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
 * Kiểm tra kết nối khi user bấm nút
 */
window.checkServerConnection = function() {
    const statusElement = document.getElementById('server-status');
    
    if (stompClient && stompClient.connected) {
        updateServerStatus('connected');
        setTimeout(() => {
            statusElement.innerHTML = '<button class="btn" onclick="checkServerConnection()">Check connect to server</button>';
        }, 3000);
    } else {
        updateServerStatus('disconnected');
        setTimeout(() => {
            statusElement.innerHTML = '<button class="btn" onclick="checkServerConnection()">Check connect to server</button>';
        }, 3000);
    }
};

