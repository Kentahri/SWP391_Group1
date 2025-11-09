'use strict';

/**
 * Kitchen WebSocket Manager
 * Quản lý kết nối WebSocket cho bếp để nhận thông tin order real-time
 */

// ==================== MODULE STATE ====================
let stompClient = null;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;
const BASE_URL = window.APP_CTX || '/pizzario';

// ==================== INITIALIZATION ====================
window.addEventListener('DOMContentLoaded', function () {
    connectWebSocket();
    // Không cần loadInitialOrders() vì đã có Thymeleaf render
});

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
    console.log('Kitchen WebSocket connected:', frame);
    reconnectAttempts = 0;
    subscribeToTopics();
    showConnectionStatus('connected');
}

/**
 * Callback khi có lỗi kết nối
 */
function onError(error) {
    console.error('Kitchen WebSocket error:', error);

    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
        const delay = Math.pow(2, reconnectAttempts) * 1000;
        console.log(`Reconnecting in ${delay}ms... (Attempt ${reconnectAttempts + 1}/${MAX_RECONNECT_ATTEMPTS})`);

        showConnectionStatus('reconnecting');
        showNotification(`Mất kết nối. Đang thử kết nối lại... (${reconnectAttempts + 1}/${MAX_RECONNECT_ATTEMPTS})`, 'warning');

        setTimeout(function () {
            reconnectAttempts++;
            connectWebSocket();
        }, delay);
    } else {
        showConnectionStatus('disconnected');
        showNotification('Mất kết nối. Vui lòng tải lại trang.', 'error');
    }
}

// ==================== SUBSCRIPTIONS ====================
/**
 * Đăng ký các topic/queue cần thiết cho kitchen
 */
function subscribeToTopics() {
    // KÊNH 1: Nhận thông tin order mới từ guest
    stompClient.subscribe('/topic/kitchen-orders', handleNewOrder);

    // KÊNH 2: Nhận thông báo cá nhân cho kitchen
    stompClient.subscribe('/queue/kitchen-notifications', handleKitchenNotification);

    // KÊNH 3: Nhận cập nhật trạng thái active của sản phẩm từ Manager hoặc Kitchen khác
    stompClient.subscribe('/topic/products-status', handleProductStatusUpdate);

    console.log('Kitchen subscribed to WebSocket topics');
}

/**
 * Xử lý order mới từ guest
 */
function handleNewOrder(message) {
    try {
        console.log('[Kitchen] Raw message received:', message);
        console.log('[Kitchen] Message body:', message.body);
        
        const orderData = JSON.parse(message.body);
        console.log('[Kitchen] Parsed order data:', orderData);

        // Kiểm tra nếu đang ở trang order detail
        const isOrderDetailPage = window.location.pathname.includes('/kitchen/order/');
        
        // Nếu đang ở trang order detail - reload để hiển thị trạng thái mới nhất
        if (isOrderDetailPage) {
            const currentOrderId = window.location.pathname.split('/kitchen/order/')[1];
            
            // Kiểm tra xem update này có liên quan đến order hiện tại không
            if (orderData.orderId == currentOrderId || orderData.id == currentOrderId) {
                console.log('[Kitchen] Order update received on detail page - reloading to refresh status');
                
                // Reset tracking để tránh reload khi user tự thay đổi (quá 2 giây)
                const isRecentUpdate = window.lastUpdateByThisUser && 
                                       Date.now() - window.lastUpdateByThisUser.timestamp < 2000;
                
                if (!isRecentUpdate) {
                    // Reload sau delay nhỏ để tránh reload liên tục
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                } else {
                    console.log('[Kitchen] Recent update by this user - skipping reload');
                    window.lastUpdateByThisUser = null;
                }
            }
            return; // Không xử lý thêm
        }

        // Xử lý theo loại message
        if (orderData.type === 'ORDER_UPDATED') {
            console.log('[Kitchen] Order updated:', orderData);
            
            // Chỉ hiển thị thông báo nếu không ở trang order detail
            if (!isOrderDetailPage) {
                upsertOrderCard(orderData);
                // Hiển thị thông báo chi tiết hơn nếu có message
                const notificationMsg = orderData.message || `Order ${orderData.code} đã được cập nhật`;
                showNotification(notificationMsg, 'info');
                // Nếu đang ở trang kitchen dashboard thì reload để cập nhật danh sách
                if (isKitchenDashboardPage()) {
                    scheduleDashboardReload();
                }
            }
        } else if (orderData.type === 'ORDER_ITEM_CANCELLED') {
            console.log('[Kitchen] Order item cancelled:', orderData);
            
            // Hiển thị thông báo khi có món bị hủy
            if (!isOrderDetailPage) {
                upsertOrderCard(orderData);
                // Hiển thị thông báo về món bị hủy
                const cancelledItemInfo = orderData.items && orderData.items.length > 0 
                    ? orderData.items[0] 
                    : null;
                const itemName = cancelledItemInfo ? cancelledItemInfo.productName : 'Món';
                const quantity = cancelledItemInfo ? cancelledItemInfo.quantity : '';
                const notificationMsg = orderData.message || 
                    `Order ${orderData.code}: ${itemName}${quantity ? ' (x' + quantity + ')' : ''} đã bị hủy`;
                showNotification(notificationMsg, 'warning');
                // Nếu đang ở trang kitchen dashboard thì reload để cập nhật danh sách
                if (isKitchenDashboardPage()) {
                    scheduleDashboardReload();
                }
            } else {
                // Nếu đang ở trang order detail, reload để hiển thị trạng thái mới
                const currentOrderId = window.location.pathname.split('/kitchen/order/')[1];
                if (orderData.orderId == currentOrderId || orderData.id == currentOrderId) {
                    console.log('[Kitchen] Order item cancelled on detail page - reloading to refresh status');
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                }
            }
        } else if (orderData.type === 'ORDER_COMPLETED') {
            console.log('[Kitchen] Order completed:', orderData);
            
            // Order đã hoàn thành - dashboard sẽ tự động refresh để order này biến mất
            // (vì dashboard chỉ hiển thị order chưa hoàn thành)
            const notificationMsg = orderData.message || `Order ${orderData.code} đã hoàn thành`;
            showNotification(notificationMsg, 'success');
            
            // Reload dashboard để order này biến mất khỏi danh sách
            if (isKitchenDashboardPage()) {
                scheduleDashboardReload();
            }
            
            // Nếu đang ở trang order detail của order này, reload
            if (isOrderDetailPage) {
                const currentOrderId = window.location.pathname.split('/kitchen/order/')[1];
                if (orderData.orderId == currentOrderId || orderData.id == currentOrderId) {
                    console.log('[Kitchen] Order completed on detail page - reloading');
                    setTimeout(() => {
                        window.location.href = '/pizzario/kitchen'; // Redirect về dashboard
                    }, 1500);
                }
            }
        } else if (orderData.type === 'NEW_ORDER') {
            console.log('[Kitchen] New order:', orderData);
            upsertOrderCard(orderData);
            showNotification(`Có order mới: ${orderData.code} - ${orderData.tableName || 'Take away'}`, 'info');
            playNotificationSound();
            if (isKitchenDashboardPage()) {
                scheduleDashboardReload();
            }
        } else {
            // Fallback cho message không có type
            upsertOrderCard(orderData);
            showNotification(`Có order mới: ${orderData.code} - ${orderData.tableName || 'Take away'}`, 'info');
            if (isKitchenDashboardPage()) {
                scheduleDashboardReload();
            }
        }

    } catch (error) {
        console.error('Error parsing new order message:', error);
        console.error('Message body:', message.body);
    }
}

// Xóa bỏ handleOrderUpdate vì kitchen không cập nhật order status

/**
 * Xử lý thông báo cá nhân cho kitchen
 */
function handleKitchenNotification(message) {
    try {
        const notification = JSON.parse(message.body);
        console.log('[Kitchen] Personal notification:', notification);

        showNotification(notification.message || notification, 'info');

        // Nếu đang ở trang kitchen dashboard, làm mới để đồng bộ giao diện
        if (isKitchenDashboardPage()) {
            scheduleDashboardReload();
        }

    } catch (error) {
        console.error('Error parsing kitchen notification:', error);
    }
}

/**
 * Xử lý cập nhật trạng thái active của sản phẩm từ Manager hoặc Kitchen khác
 * Để đồng bộ UI real-time khi có người khác toggle product active
 */
function handleProductStatusUpdate(message) {
    try {
        const data = JSON.parse(message.body);
        console.log('[Kitchen] Product status update received:', data);

        // Xử lý các loại message: PRODUCT_TOGGLED, PRODUCT_UPDATED, PRODUCT_CREATED
        if (data.type === 'PRODUCT_TOGGLED' || 
            data.type === 'PRODUCT_UPDATED' || 
            data.type === 'PRODUCT_CREATED') {
            
            if (data.product && data.product.id) {
                const productId = data.product.id;
                const isActive = data.product.active === true;
                const updatedBy = data.updatedBy || 'Unknown';
                
                console.log(`[Kitchen] Product ${productId} active status updated to ${isActive} by ${updatedBy}`);
                
                // Cập nhật UI trên trang outstock management nếu đang ở đó
                updateProductStatusInUI(productId, isActive);
                
                // Hiển thị thông báo nếu không phải do chính kitchen này update
                // (tránh duplicate notification khi kitchen tự toggle)
                const isOnOutstockPage = window.location.pathname.includes('/kitchen/outstock');
                if (isOnOutstockPage && updatedBy !== 'Kitchen') {
                    const productName = data.product.name || 'Món ăn';
                    showNotification(
                        `${productName} đã được ${isActive ? 'bật' : 'tắt'} bởi ${updatedBy}`,
                        'info'
                    );
                }
            }
        }
    } catch (error) {
        console.error('[Kitchen] Error handling product status update:', error);
    }
}

/**
 * Cập nhật UI của product card trong trang outstock management
 * @param {number} productId - ID của sản phẩm
 * @param {boolean} isActive - Trạng thái active mới
 */
function updateProductStatusInUI(productId, isActive) {
    // Chỉ cập nhật nếu đang ở trang outstock management
    if (!window.location.pathname.includes('/kitchen/outstock')) {
        return;
    }

    // Tìm product card trong DOM
    const productCards = document.querySelectorAll('.product-card');
    let productCard = null;

    for (const card of productCards) {
        // Tìm product card có chứa product ID trong text hoặc attribute
        const productIdElement = card.querySelector('.attr-value');
        if (productIdElement && productIdElement.textContent.trim() == productId) {
            productCard = card;
            break;
        }
    }

    if (productCard) {
        // Cập nhật badge status
        const activeBadge = productCard.querySelector('.badge.active');
        const inactiveBadge = productCard.querySelector('.badge.inactive');
        
        if (isActive) {
            if (inactiveBadge) inactiveBadge.style.display = 'none';
            if (activeBadge) {
                activeBadge.style.display = 'inline-block';
            } else {
                // Tạo badge mới nếu chưa có
                const badge = document.createElement('span');
                badge.className = 'badge status-badge active';
                badge.textContent = 'ACTIVE';
                const header = productCard.querySelector('.card-header');
                if (header) {
                    header.appendChild(badge);
                }
            }
        } else {
            if (activeBadge) activeBadge.style.display = 'none';
            if (inactiveBadge) {
                inactiveBadge.style.display = 'inline-block';
            } else {
                // Tạo badge mới nếu chưa có
                const badge = document.createElement('span');
                badge.className = 'badge status-badge inactive';
                badge.textContent = 'INACTIVE';
                const header = productCard.querySelector('.card-header');
                if (header) {
                    header.appendChild(badge);
                }
            }
        }

        // Cập nhật checkbox
        const checkbox = productCard.querySelector('input[type="checkbox"][name="active"]');
        if (checkbox) {
            checkbox.checked = isActive;
        }

        // Animation để người dùng thấy thay đổi
        productCard.style.animation = 'none';
        setTimeout(() => {
            productCard.style.animation = 'pulse 0.5s ease';
        }, 10);

        console.log(`[Kitchen] Product ${productId} UI updated: ${isActive ? 'ACTIVE' : 'INACTIVE'}`);
    }
}

// ==================== UI UPDATES ====================
// Không cần loadInitialOrders() và displayOrders() vì sử dụng Thymeleaf

/**
 * Thêm order mới vào UI (chỉ cho WebSocket real-time)
 */
function addNewOrderToUI(orderData) {
    console.log('Adding new order to UI:', orderData);
    
    const orderList = document.getElementById('order-list');
    if (!orderList) {
        console.error('Order list element not found');
        return;
    }

    // Xóa "Không có đơn nào" nếu có
    const noOrders = orderList.querySelector('.no-orders');
    if (noOrders) {
        noOrders.remove();
    }

    try {
        const orderCard = createOrderCardElement(orderData);
        console.log('[debug] Created order card:', orderCard, (orderCard ? orderCard.outerHTML : orderCard));
        
        // Check safety before style access
        if (!orderCard || !orderCard.style) {
            console.error('[debug] orderCard is not a valid DOM node', orderCard);
            return;
        }
        
        // Thêm animation
        orderCard.style.opacity = '0';
        orderCard.style.transform = 'translateY(-20px)';
        
        // Insert vào đầu danh sách
        orderList.insertBefore(orderCard, orderList.firstChild);
        
        // Animation fade in
        setTimeout(() => {
            if (orderCard && orderCard.style) {
                orderCard.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                orderCard.style.opacity = '1';
                orderCard.style.transform = 'translateY(0)';
            }
        }, 100);

        // Highlight order mới
        setTimeout(() => {
            if (orderCard) {
                orderCard.classList.add('new-order');
                setTimeout(() => {
                    if (orderCard) {
                        orderCard.classList.remove('new-order');
                    }
                }, 3000);
            }
        }, 500);
        
        console.log('Successfully added new order to UI');
    } catch (error) {
        console.error('Error adding new order to UI:', error, orderData);
    }
}

/**
 * Cập nhật order trong UI
 */
function updateOrderInUI(updateData) {
    const orderElement = document.querySelector(`[data-order-id="${updateData.orderId}"]`);
    if (!orderElement) return;

    // Cập nhật trạng thái
    if (updateData.status) {
        updateOrderStatus(orderElement, updateData.status);
    }

    // Cập nhật progress
    if (updateData.progress !== undefined) {
        updateOrderProgress(orderElement, updateData.progress);
    }

    // Cập nhật completed items
    if (updateData.completedItems !== undefined) {
        updateCompletedItems(orderElement, updateData.completedItems, updateData.totalItems);
    }

    // Animation update
    orderElement.style.animation = 'none';
    setTimeout(() => {
        orderElement.style.animation = 'pulse 0.5s ease';
    }, 10);
}

/**
 * Tạo DOM element cho order card
 */
function createOrderCardElement(orderData) {
    let orderCard;
    try {
        orderCard = document.createElement('div');
        orderCard.className = 'order-card';
        orderCard.setAttribute('data-order-id', orderData.orderId || orderData.id || 'unknown');
        const code = orderData.code || 'N/A';
        const tableName = orderData.tableName || orderData.type || 'N/A';
        const priorityBadge = getPriorityBadgeHTML(orderData.priority);
        const statusBadge = getStatusBadgeHTML(orderData.status);
        const totalPrice = (typeof orderData.totalPrice === 'number' && !isNaN(orderData.totalPrice)) ? orderData.totalPrice : 0;
        const orderNote = (typeof orderData.note === 'string' && orderData.note.trim()) ? orderData.note : null;
        
        orderCard.innerHTML = `
            <div class="order-header">
                <span class="order-id">${code}</span>
                ${priorityBadge}
                ${statusBadge}
            </div>
            <div class="order-meta">
                <span class="order-table"><i class="fa fa-map-marker-alt"></i> ${tableName}</span>
                <span class="order-time"><i class="fa fa-clock"></i> <span>Vừa xong</span></span>
            </div>
            ${orderNote ? `<div class="order-info"><span class="order-note"><i class="fa fa-sticky-note"></i> <span>${orderNote}</span></span></div>` : ''}
            <div class="order-total"><span class="total-price">Tổng: ${formatPrice(totalPrice)} VNĐ</span></div>
            <a href="/pizzario/kitchen/order/${orderData.orderId || orderData.id || 'unknown'}" class="order-detail-btn">Xem chi tiết</a>
        `;
    } catch (error) {
        console.error('Error creating order card element:', error, orderData);
        orderCard = null;
    }
    if (!orderCard || typeof orderCard !== 'object' || !orderCard.nodeType) {
        const fallbackCard = document.createElement('div');
        fallbackCard.className = 'order-card error';
        fallbackCard.setAttribute('data-order-id', 'error');
        fallbackCard.innerHTML = `<div class="order-header"><span class="order-id">Error loading order</span></div><div class="order-meta"><span>Lỗi khi tải thông tin order</span></div>`;
        return fallbackCard;
    }
    return orderCard;
}

/**
 * Lấy HTML cho priority badge
 */
function getPriorityBadgeHTML(priority) {
    try {
        switch (priority) {
            case 'PRIORITY':
                return '<span class="badge orange">Ưu tiên</span>';
            case 'NORMAL':
            default:
                return '<span class="badge blue">Bình thường</span>';
        }
    } catch (error) {
        console.error('Error creating priority badge:', error);
        return '<span class="badge blue">Bình thường</span>';
    }
}

/**
 * Lấy HTML cho status badge
 */
function getStatusBadgeHTML(status) {
    try {
        switch (status) {
            case 'PREPARING':
                return '<span class="badge yellow">Đang chuẩn bị</span>';
            case 'SERVED':
                return '<span class="badge green">Đã phục vụ</span>';
            case 'COMPLETED':
                return '<span class="badge gray">Hoàn thành</span>';
            case 'CANCELLED':
                return '<span class="badge red">Đã hủy</span>';
            default:
                return '<span class="badge yellow">Đang chuẩn bị</span>';
        }
    } catch (error) {
        console.error('Error creating status badge:', error);
        return '<span class="badge yellow">Đang chuẩn bị</span>';
    }
}

/**
 * Cập nhật trạng thái order
 */
function updateOrderStatus(orderElement, newStatus) {
    const statusBadge = orderElement.querySelector('.badge');
    if (statusBadge) {
        statusBadge.className = 'badge ' + getStatusClass(newStatus);
        statusBadge.textContent = getStatusText(newStatus);
    }
}

/**
 * Cập nhật progress bar
 */
function updateOrderProgress(orderElement, progress) {
    const progressBar = orderElement.querySelector('.progress');
    if (progressBar) {
        progressBar.style.width = progress + '%';
    }
}

/**
 * Cập nhật số món đã hoàn thành
 */
function updateCompletedItems(orderElement, completed, total) {
    const completedSpan = orderElement.querySelector('.order-progress span:last-child');
    if (completedSpan) {
        completedSpan.textContent = `${completed}/${total}`;
    }
}

/**
 * Upsert order card in UI
 */
function upsertOrderCard(orderData) {
  const orderId = orderData.orderId || orderData.id;
  let orderCard = document.querySelector(`.order-card[data-order-id="${orderId}"]`);
  if (orderCard) {
    // Update trường text content
    const codeEl = orderCard.querySelector('.order-id');
    if (codeEl) codeEl.textContent = orderData.code || 'N/A';
    const tableEl = orderCard.querySelector('.order-table');
    if (tableEl) tableEl.innerHTML = `<i class="fa fa-map-marker-alt"></i> ${orderData.tableName || orderData.type || 'N/A'}`;
    // Update badge (trạng thái)
    const oldBadge = orderCard.querySelector('.badge');
    if (oldBadge) oldBadge.outerHTML = getStatusBadgeHTML(orderData.status);
    const totalPrice = (typeof orderData.totalPrice === 'number' && !isNaN(orderData.totalPrice)) ? orderData.totalPrice : 0;
    const totalPriceEl = orderCard.querySelector('.total-price');
    if (totalPriceEl) totalPriceEl.textContent = `Tổng: ${formatPrice(totalPrice)} VNĐ`;
    const orderNote = (typeof orderData.note === 'string' && orderData.note.trim()) ? orderData.note : null;
    const noteWrapper = orderCard.querySelector('.order-info .order-note span:last-child');
    if (noteWrapper) noteWrapper.textContent = orderNote || '';
    orderCard.classList.add('updated-order');
    setTimeout(() => orderCard.classList.remove('updated-order'), 1200);
    return;
  }
  addNewOrderToUI(orderData);
}

// ==================== UTILITY FUNCTIONS ====================
/**
 * Validate DOM element
 */
function isValidElement(element) {
    try {
        if (!element) {
            console.log('Element is null or undefined');
            return false;
        }
        
        if (typeof element !== 'object') {
            console.log('Element is not an object:', typeof element);
            return false;
        }
        
        if (element.nodeType !== Node.ELEMENT_NODE) {
            console.log('Element is not a DOM element, nodeType:', element.nodeType);
            return false;
        }
        
        if (!element.style) {
            console.log('Element has no style property');
            return false;
        }
        
        console.log('Element is valid:', element);
        return true;
    } catch (error) {
        console.error('Error validating element:', error);
        return false;
    }
}

/**
 * Hiển thị trạng thái kết nối
 */
function showConnectionStatus(status) {
    const statusElement = document.getElementById('connection-status');
    if (!statusElement) return;

    switch (status) {
        case 'connected':
            statusElement.className = 'connection-status connected';
            statusElement.textContent = 'Đã kết nối';
            break;
        case 'reconnecting':
            statusElement.className = 'connection-status reconnecting';
            statusElement.textContent = 'Đang kết nối lại...';
            break;
        case 'disconnected':
            statusElement.className = 'connection-status disconnected';
            statusElement.textContent = 'Mất kết nối';
            break;
    }
}

/**
 * Hiển thị thông báo
 */
function showNotification(message, type = 'info') {
    // Tạo notification element
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;

    // Thêm vào body
    document.body.appendChild(notification);

    // Animation
    setTimeout(() => {
        notification.classList.add('show');
    }, 100);

    // Auto remove
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 3000);
}

/**
 * Kiểm tra có đang ở trang Kitchen Dashboard không
 */
function isKitchenDashboardPage() {
    try {
        const path = window.location.pathname || '';
        // Trang dashboard không phải order-list, outstock hay order detail
        const isKitchenArea = path.includes('/kitchen');
        const isOrderList = path.includes('/kitchen/order-list');
        const isOutstock = path.includes('/kitchen/outstock');
        const isOrderDetail = path.includes('/kitchen/order/');
        if (!isKitchenArea || isOrderList || isOutstock || isOrderDetail) return false;
        // Dựa trên cấu trúc DOM đặc trưng của dashboard
        const hasDashboardTitle = document.querySelector('.dashboard-title');
        const hasOrderItems = document.getElementById('orderItemsContainer');
        const hasCategoryFilter = document.getElementById('categoryFilter');
        return Boolean(hasDashboardTitle && hasOrderItems && hasCategoryFilter);
    } catch (e) {
        return false;
    }
}

/**
 * Lên lịch reload trang (debounce) để tránh reload liên tục khi có nhiều message
 */
let dashboardReloadTimer = null;
function scheduleDashboardReload(delayMs = 800) {
    try {
        if (dashboardReloadTimer) {
            clearTimeout(dashboardReloadTimer);
        }
        dashboardReloadTimer = setTimeout(() => {
            // Tránh reload nếu người dùng đang thao tác submit ngay trước đó
            const isRecentUpdate = window.lastUpdateByThisUser &&
                Date.now() - window.lastUpdateByThisUser.timestamp < 1000;
            if (!isRecentUpdate) {
                window.location.reload();
            }
            dashboardReloadTimer = null;
        }, delayMs);
    } catch (e) {
        // fallback nếu có lỗi
        window.location.reload();
    }
}

/**
 * Phát âm thanh thông báo
 */
function playNotificationSound() {
    // Tạo audio context để phát âm thanh
    try {
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();

        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);

        oscillator.frequency.setValueAtTime(800, audioContext.currentTime);
        oscillator.frequency.setValueAtTime(600, audioContext.currentTime + 0.1);

        gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.3);

        oscillator.start(audioContext.currentTime);
        oscillator.stop(audioContext.currentTime + 0.3);
    } catch (error) {
        console.log('Could not play notification sound:', error);
    }
}

/**
 * Lấy class CSS cho status
 */
function getStatusClass(status) {
    switch (status) {
        case 'PREPARING':
            return 'yellow';
        case 'SERVED':
            return 'green';
        case 'COMPLETED':
            return 'gray';
        case 'CANCELLED':
            return 'red';
        default:
            return 'yellow';
    }
}

/**
 * Lấy text cho status
 */
function getStatusText(status) {
    switch (status) {
        case 'PREPARING':
            return 'Đang chuẩn bị';
        case 'SERVED':
            return 'Đã phục vụ';
        case 'COMPLETED':
            return 'Hoàn thành';
        case 'CANCELLED':
            return 'Đã hủy';
        default:
            return 'Đang chuẩn bị';
    }
}

/**
 * Format giá tiền
 */
function formatPrice(price) {
    try {
        if (price === null || price === undefined || isNaN(price)) {
            return '0';
        }
        return new Intl.NumberFormat('vi-VN').format(price);
    } catch (error) {
        console.error('Error formatting price:', error, price);
        return '0';
    }
}

// ==================== PUBLIC API ====================
/**
 * Kiểm tra trạng thái kết nối
 */
window.isKitchenConnected = function () {
    return stompClient && stompClient.connected;
};

/**
 * Gửi cập nhật trạng thái item từ kitchen
 * Kitchen chỉ cập nhật status của từng món, không cập nhật order status
 */
window.updateItemStatus = function (itemId, status, note = '') {
    if (!stompClient || !stompClient.connected) {
        showNotification('Chưa kết nối đến server', 'error');
        return;
    }

    const update = {
        items: [{
            itemId: itemId,
            status: status,
            note: note
        }],
        timestamp: new Date().toISOString()
    };

    stompClient.send('/app/kitchen/update-item', {}, JSON.stringify(update));
    showNotification(`Đã cập nhật món ${itemId} thành ${status}`, 'success');
};

// ==================== CLEANUP ====================
/**
 * Cleanup khi tắt trang
 */
window.addEventListener('beforeunload', function () {
    if (stompClient && stompClient.connected) {
        stompClient.disconnect(function () {
            console.log('Kitchen WebSocket disconnected on page unload');
        });
    }
});
