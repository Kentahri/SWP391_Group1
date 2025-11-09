'use strict';

/**
 * Guest WebSocket Manager
 * Quản lý kết nối WebSocket cho khách hàng chọn bàn
 */

// ==================== MODULE STATE ====================
let stompClient = null;
let guestSessionId = null;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;
const BASE_URL = window.APP_CTX || '/pizzario';

// ==================== INITIALIZATION ====================
window.addEventListener('DOMContentLoaded', function () {
    initializeGuestSession();
    connectWebSocket();
});

/**
 * Khởi tạo session cho guest
 */
function initializeGuestSession() {
    guestSessionId = window.GUEST_SESSION || generateSessionId();
    console.log('Guest session initialized:', guestSessionId);
}

/**
 * Tạo session ID ngẫu nhiên
 */
function generateSessionId() {
    return 'guest_' + Math.random().toString(36).slice(2) + '_' + Date.now();
}

// ==================== WEBSOCKET CONNECTION ====================
/**
 * Kết nối WebSocket và thiết lập STOMP client
 */
function connectWebSocket() {
    const socketUrl = BASE_URL.replace(/\/$/, '') + '/ws';
    const socket = new SockJS(socketUrl);
    stompClient = Stomp.over(socket);

    // Tắt debug log nếu không phải localhost
    if (window.location.hostname !== 'localhost') {
        stompClient.debug = null;
    }

    stompClient.connect({}, onConnected, onError);
}

/**
 * Callback khi kết nối thành công
 */
function onConnected(frame) {
    console.log('WebSocket connected:', frame);
    reconnectAttempts = 0;
    subscribeToTopics();
    // Không hiển thị toast kết nối thành công
}

/**
 * Callback khi có lỗi kết nối
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
        console.error('Max reconnection attempts reached. Please refresh the page.');
    }
}

// ==================== SUBSCRIPTIONS ====================
/**
 * Đăng ký các topic/queue cần thiết
 */
function subscribeToTopics() {
    // KÊNH 1: Queue cá nhân - Nhận kết quả chọn bàn
    stompClient.subscribe('/queue/guest-' + guestSessionId, handlePersonalMessage);

    // KÊNH 2: Topic broadcast - Nhận cập nhật trạng thái tất cả bàn
    stompClient.subscribe('/topic/tables-guest', handleTableBroadcast);

    console.log('Subscribed to topics for session:', guestSessionId);
}

/**
 * Xử lý tin nhắn cá nhân từ server
 */
function handlePersonalMessage(message) {
    try {
        const data = JSON.parse(message.body);
        console.log('[Guest] Personal message received:', data);

        switch (data.type) {
            case 'SUCCESS':
                handleTableSelectSuccess(data);
                break;
            case 'CONFLICT':
                handleTableSelectConflict(data);
                break;
            case 'TABLE_RETIRED':
                handleGuestTableRetired(data);
            case 'ERROR':
            default:
                handleTableSelectError(data);
                break;
        }
    } catch (error) {
        console.error('Error parsing personal message:', error);
        // Không hiển thị toast cho lỗi parse, chỉ log
    }
}

/**
 * Xử lý broadcast cập nhật trạng thái bàn
 */
function handleTableBroadcast(message) {
    try {
        const data = JSON.parse(message.body);
        console.log('[Guest] Table broadcast received:', data);
        updateTableUI(data);
    } catch (error) {
        console.error('Error parsing table broadcast:', error);
    }
}

// ==================== MESSAGE HANDLERS ====================
/**
 * Xử lý khi chọn bàn thành công
 */
function handleTableSelectSuccess(data) {
    showToast(data.message || 'Chọn bàn thành công!', 'success');

    // Redirect đến trang menu
    const params = new URLSearchParams();
    if (data.sessionId) params.set('sessionId', data.sessionId);
    if (data.tableId) params.set('tableId', data.tableId);

    const menuUrl = BASE_URL.replace(/\/$/, '') + '/guest/menu' +
        (params.toString() ? '?' + params.toString() : '');

    setTimeout(() => {
        window.location.href = menuUrl;
    }, 500);
}

/**
 * Xử lý khi bàn bị conflict (đã có người chọn)
 */
function handleTableSelectConflict(data) {
    const message = data.message || 'Bàn đã được chọn.';
    const availableTables = data.availableTables || [];

    let fullMessage = message;
    if (availableTables.length > 0) {
        fullMessage += ' Gợi ý bàn trống: ' + availableTables.join(', ');
    }

    showToast(fullMessage, 'error');
    highlightSuggestedTables(availableTables);
}

/**
 * Xử lý khi có lỗi
 */
function handleTableSelectError(data) {
    showToast(data.message || 'Có lỗi xảy ra', 'error');
}

// ==================== UI UPDATES ====================
/**
 * Cập nhật UI của bàn khi có thay đổi
 */
function updateTableUI(data) {
    const tableElement = document.getElementById('table-' + data.tableId);
    if (!tableElement) return;

    // Cập nhật text trạng thái
    const statusElement = tableElement.querySelector('.status');
    if (statusElement) {
        statusElement.textContent = data.newStatus;
    }

    // Cập nhật class để disable/enable
    if (data.newStatus === 'AVAILABLE') {
        tableElement.classList.remove('disabled');
        tableElement.classList.add('available');
    } else {
        tableElement.classList.add('disabled');
        tableElement.classList.remove('available');
    }

    // Animation effect
    tableElement.style.animation = 'none';
    setTimeout(() => {
        tableElement.style.animation = 'pulse 0.5s ease';
    }, 10);
}

/**
 * Highlight các bàn được gợi ý
 */
function highlightSuggestedTables(tableIds) {
    if (!Array.isArray(tableIds) || tableIds.length === 0) return;

    // Xóa highlight cũ
    document.querySelectorAll('.table-card.suggested').forEach(el => {
        el.classList.remove('suggested');
    });

    // Thêm highlight mới
    tableIds.forEach(id => {
        const element = document.getElementById('table-' + id);
        if (element && !element.classList.contains('disabled')) {
            element.classList.add('suggested');
        }
    });
}

// ==================== PUBLIC API ====================
/**
 * Guest chọn bàn
 * @param {string|number} tableId - ID của bàn
 * @param {number} guestCount - Số lượng khách (mặc định 1)
 */
window.selectTable = function (tableId, guestCount = 1) {
    if (!stompClient || !stompClient.connected) {
        console.warn('WebSocket not connected. Retrying...');
        // Thử kết nối lại
        connectWebSocket();
        setTimeout(() => {
            if (stompClient && stompClient.connected) {
                const request = {
                    tableId: tableId,
                    sessionId: guestSessionId,
                    guestCount: guestCount
                };
                stompClient.send('/app/guest/table/select', {}, JSON.stringify(request));
            } else {
                showToast('Không thể kết nối tới máy chủ. Vui lòng thử lại.', 'error');
            }
        }, 1000);
        return;
    }

    if (!guestSessionId) {
        showToast('Lỗi: Không tìm thấy session ID', 'error');
        return;
    }

    const request = {
        tableId: tableId,
        sessionId: guestSessionId,
        guestCount: guestCount
    };

    console.log('Sending table selection request:', request);

    try {
        stompClient.send('/app/guest/table/select', {}, JSON.stringify(request));
        // Không hiển thị toast "Đang xử lý..."
    } catch (error) {
        console.error('Error sending table selection:', error);
        showToast('Không thể gửi yêu cầu. Vui lòng thử lại.', 'error');
    }
};
/**
 * Lấy session ID hiện tại
 */
window.getGuestSessionId = function () {
    return guestSessionId;
};

/**
 * Kiểm tra trạng thái kết nối
 */
window.isConnected = function () {
    return stompClient && stompClient.connected;
};

// ==================== CLEANUP ====================
/**
 * Cleanup khi tắt trang
 */
window.addEventListener('beforeunload', function () {
    if (stompClient && stompClient.connected) {
        stompClient.disconnect(function () {
            console.log('WebSocket disconnected on page unload');
        });
    }
});
