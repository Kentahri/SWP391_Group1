"use strict";

/**
 * WebSocket Manager for the Guest Menu Page
 * Handles connection, payment requests, and real-time order updates.
 */
let stompClient = null;
let guestSessionId = null;
let guestOrderId = null; // Lưu orderId của guest để filter messages
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;
const BASE_URL = window.APP_CTX || "/pizzario";

window.addEventListener("DOMContentLoaded", function () {
  initializeGuestSession();
  initializeOrderId();
  connectWebSocket();
  
  // Lắng nghe sự kiện khi order được place thành công
  setupOrderPlaceListener();
});

/**
 * Lắng nghe sự kiện khi order được place
 */
function setupOrderPlaceListener() {
  // Lắng nghe khi form place order được submit
  document.body.addEventListener("submit", function(e) {
    const form = e.target;
    if (form.action && form.action.includes("/guest/order/place")) {
      // Sau khi submit thành công, cập nhật orderId
      setTimeout(() => {
        updateOrderIdFromServer();
      }, 1000); // Đợi server xử lý xong
    }
  });
  
  // Kiểm tra xem có ordered items không khi page load
  setTimeout(() => {
    checkAndUpdateOrderId();
  }, 500);
}

/**
 * Kiểm tra và cập nhật orderId nếu có ordered items
 */
function checkAndUpdateOrderId() {
  const orderedItemsList = document.getElementById("orderedItemsList");
  if (orderedItemsList && orderedItemsList.children.length > 0 && !guestOrderId) {
    // Nếu có ordered items nhưng chưa có orderId, thử lấy từ server
    updateOrderIdFromServer();
  }
}

function initializeGuestSession() {
  // Lấy sessionId từ URL params
  const urlParams = new URLSearchParams(window.location.search);
  const urlSessionId = urlParams.get("sessionId");
  
  if (urlSessionId) {
    guestSessionId = urlSessionId;
  } else {
    guestSessionId =
      "guest_" + Math.random().toString(36).slice(2) + "_" + Date.now();
  }
  console.log("[Guest] Session initialized:", guestSessionId);
}

/**
 * Lấy orderId từ server dựa trên sessionId
 */
function initializeOrderId() {
  updateOrderIdFromServer();
}

/**
 * Cập nhật orderId từ server (có thể gọi lại khi cần)
 */
function updateOrderIdFromServer() {
  const urlParams = new URLSearchParams(window.location.search);
  const sessionId = urlParams.get("sessionId");
  
  if (sessionId) {
    // Gọi API để lấy orderId (nếu có order)
    // Cho phép cập nhật cả khi đã có orderId (để đảm bảo đồng bộ)
    fetch(`${BASE_URL}/guest/order/current-order-id?sessionId=${sessionId}`)
      .then(response => {
        if (response.ok) {
          return response.json();
        }
        return null;
      })
      .then(data => {
        if (data && data.orderId) {
          const oldOrderId = guestOrderId;
          guestOrderId = data.orderId;
          if (oldOrderId !== guestOrderId) {
            console.log("[Guest] Order ID updated from server:", guestOrderId);
          }
        } else {
          // Nếu server trả về không có order, reset orderId
          if (guestOrderId) {
            console.log("[Guest] Order ID cleared - no order found");
            guestOrderId = null;
          }
        }
      })
      .catch(error => {
        console.log("[Guest] Error getting order ID:", error);
      });
  }
}

function connectWebSocket() {
  const socketUrl = BASE_URL.replace(/\/$/, "") + "/ws";
  const socket = new SockJS(socketUrl);
  stompClient = Stomp.over(socket);
  if (window.location.hostname !== "localhost") stompClient.debug = null;
  stompClient.connect({}, onConnected, onError);
}

function onConnected(frame) {
  console.log("[Guest] WebSocket connected:", frame);
  reconnectAttempts = 0;
  
  // Subscribe vào queue cá nhân
  stompClient.subscribe(
    "/queue/guest-" + guestSessionId,
    handlePersonalMessage
  );
  
  // Subscribe vào topic kitchen-orders để nhận order updates real-time
  stompClient.subscribe(
    "/topic/kitchen-orders",
    handleOrderUpdate
  );
  
  console.log("[Guest] Subscribed to WebSocket topics");
}

function onError(error) {
  showToast("Lỗi kết nối WebSocket.", "error");
  if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
    const delay = Math.pow(2, reconnectAttempts) * 1000;
    showToast(`Mất kết nối, sẽ thử lại sau ${delay / 1000} giây...`, "warning");
    setTimeout(() => {
      reconnectAttempts++;
      connectWebSocket();
    }, delay);
  } else {
    alert("Mất kết nối tới máy chủ. Vui lòng tải lại trang.");
  }
}

function handlePersonalMessage(message) {
  try {
    const data = JSON.parse(message.body);
    if (data.type === "SUCCESS") {
      handleTableReleaseSuccess(data);
    } else if (data.type === "ERROR") {
      showToast(data.message || "Có lỗi xảy ra khi gửi yêu cầu.", "error");
    }
  } catch (error) {
    console.error("[Guest] Error parsing personal message:", error);
  }
}

/**
 * Xử lý order updates từ kitchen (real-time)
 * Tương tự như kitchen websocket handler
 */
function handleOrderUpdate(message) {
  try {
    const orderData = JSON.parse(message.body);
    console.log("[Guest] Order update received:", orderData);
    
    // Nếu chưa có orderId, thử lấy từ server hoặc từ message
    if (!guestOrderId) {
      // Thử lấy orderId từ server trước
      updateOrderIdFromServer();
      
      // Nếu message có orderId và có thể là order của guest này (dựa vào sessionId)
      // Thì tạm thời xử lý, sau đó sẽ filter lại
      if (orderData.orderId) {
        // Kiểm tra xem có phải order của guest này không bằng cách so sánh sessionId
        const urlParams = new URLSearchParams(window.location.search);
        const sessionId = urlParams.get("sessionId");
        
        // Nếu có sessionId và message type là NEW_ORDER, có thể là order mới của guest
        if (sessionId && orderData.type === "NEW_ORDER") {
          guestOrderId = orderData.orderId;
          console.log("[Guest] Order ID set from NEW_ORDER message:", guestOrderId);
          reloadOrderedItems();
          return;
        }
      }
      
      // Nếu vẫn chưa có orderId, bỏ qua message
      console.log("[Guest] Ignoring order update - no orderId yet");
      return;
    }
    
    // Chỉ xử lý nếu là order của guest này
    if (orderData.orderId != guestOrderId) {
      console.log("[Guest] Ignoring order update - not for this guest (orderId: " + orderData.orderId + " vs " + guestOrderId + ")");
      return;
    }
    
    // Xử lý theo loại message
    switch (orderData.type) {
      case "ORDER_UPDATED":
        handleOrderUpdated(orderData);
        break;
      case "ORDER_ITEM_CANCELLED":
        handleOrderItemCancelled(orderData);
        break;
      case "ORDER_COMPLETED":
        handleOrderCompleted(orderData);
        break;
      case "NEW_ORDER":
        // Nếu là order mới và orderId khớp, reload
        reloadOrderedItems();
        break;
      default:
        console.log("[Guest] Unknown order update type:", orderData.type);
        reloadOrderedItems();
    }
  } catch (error) {
    console.error("[Guest] Error parsing order update:", error);
  }
}

/**
 * Xử lý khi order được cập nhật (kitchen thay đổi status items)
 */
function handleOrderUpdated(orderData) {
  console.log("[Guest] Order updated:", orderData);
  
  // Nếu có items trong message, cập nhật UI trực tiếp
  if (orderData.items && orderData.items.length > 0) {
    updateOrderItemsUI(orderData.items);
    showToast("Đơn hàng của bạn đã được cập nhật", "info");
  } else {
    // Nếu không có items, reload từ server
    reloadOrderedItems();
    showToast("Đơn hàng của bạn đã được cập nhật", "info");
  }
}

/**
 * Xử lý khi có món bị hủy
 */
function handleOrderItemCancelled(orderData) {
  console.log("[Guest] Order item cancelled:", orderData);
  
  // Reload ordered items để cập nhật UI
  reloadOrderedItems();
  
  if (orderData.items && orderData.items.length > 0) {
    const cancelledItem = orderData.items[0];
    showToast(
      `Món "${cancelledItem.productName}" đã bị hủy`,
      "warning"
    );
  } else {
    showToast("Một món trong đơn hàng đã bị hủy", "warning");
  }
}

/**
 * Xử lý khi order hoàn thành
 */
function handleOrderCompleted(orderData) {
  console.log("[Guest] Order completed:", orderData);
  
  reloadOrderedItems();
  showToast("Đơn hàng của bạn đã hoàn thành!", "success");
}

/**
 * Cập nhật UI của order items với dữ liệu mới
 */
function updateOrderItemsUI(items) {
  const orderedItemsList = document.getElementById("orderedItemsList");
  if (!orderedItemsList) {
    console.warn("[Guest] orderedItemsList not found");
    return;
  }
  
  // Tìm và cập nhật từng item
  items.forEach(item => {
    const itemElement = orderedItemsList.querySelector(
      `[data-order-item-id="${item.itemId}"]`
    );
    
    if (itemElement) {
      // Cập nhật status
      const statusElement = itemElement.querySelector(".status");
      if (statusElement) {
        const statusClass = getStatusClass(item.status);
        statusElement.textContent = getStatusText(item.status);
        statusElement.className = "status " + statusClass;
      }
      
      // Cập nhật class của card để đổi màu theo trạng thái
      const orderStatusClasses = [
        "order-status-pending",
        "order-status-preparing",
        "order-status-ready",
        "order-status-served",
        "order-status-cancelled",
        "order-status-unknown",
      ];
      orderStatusClasses.forEach(cls => itemElement.classList.remove(cls));
      itemElement.classList.add(getOrderItemStatusClass(item.status));

      // Toggle nút Cancel theo trạng thái; nếu thiếu form mà status là PENDING thì reload fragment
      const cancelForm = itemElement.querySelector("form.ajax-form");
      const isPending = (item.status || "").toUpperCase() === "PENDING";
      if (cancelForm) {
        cancelForm.style.display = isPending ? "inline-block" : "none";
      } else if (isPending) {
        // Trường hợp ban đầu không render form (do trước đó không phải PENDING)
        // => reload phần ordered items để lấy đúng fragment có nút Cancel
        reloadOrderedItems();
      }
      
      // Animation để người dùng thấy thay đổi
      itemElement.style.animation = "none";
      setTimeout(() => {
        itemElement.style.animation = "pulse 0.5s ease";
      }, 10);
    }
  });
}

/**
 * Reload ordered items từ server
 */
function reloadOrderedItems() {
  const urlParams = new URLSearchParams(window.location.search);
  const sessionId = urlParams.get("sessionId");
  
  if (!sessionId) {
    console.warn("[Guest] No sessionId to reload ordered items");
    return;
  }
  
  // Cập nhật orderId trước khi reload
  updateOrderIdFromServer();
  
  // Gọi API để lấy ordered items mới
  fetch(`${BASE_URL}/guest/order/ordered-items?sessionId=${sessionId}`)
    .then(response => {
      if (response.ok) {
        return response.text(); // HTML fragment
      }
      throw new Error("Failed to reload ordered items");
    })
    .then(html => {
      // Cập nhật phần ordered items trong sidebar
      const sidebar = document.getElementById("sidebar");
      if (sidebar) {
        // Parse HTML fragment
        const tempDiv = document.createElement("div");
        tempDiv.innerHTML = html;
        
        // Tìm phần order-view trong fragment
        const orderViewFragment = tempDiv.querySelector("#order-view");
        if (orderViewFragment) {
          // Tìm order-view hiện tại trong sidebar
          const currentOrderView = sidebar.querySelector("#order-view");
          if (currentOrderView) {
            currentOrderView.replaceWith(orderViewFragment);
          } else {
            // Nếu không tìm thấy, thêm vào sidebar
            const orderSection = sidebar.querySelector(".order-section");
            if (orderSection) {
              orderSection.appendChild(orderViewFragment);
            }
          }
        } else {
          // Fallback: tìm phần order-section
          const newOrderSection = tempDiv.querySelector(".order-section");
          if (newOrderSection) {
            const orderSection = sidebar.querySelector(".order-section");
            if (orderSection) {
              orderSection.replaceWith(newOrderSection);
            }
          }
        }
        
        // Cập nhật lại orderId sau khi reload
        updateOrderIdFromServer();
      }
    })
    .catch(error => {
      console.error("[Guest] Error reloading ordered items:", error);
    });
}

/**
 * Lấy class CSS cho status
 */
function getStatusClass(status) {
  switch (status) {
    case "PENDING":
      return "status-pending";
    case "PREPARING":
      return "status-preparing";
    case "READY":
      return "status-ready";
    case "SERVED":
      return "status-served";
    case "CANCELLED":
      return "status-cancelled";
    default:
      return "status-pending";
  }
}

function getOrderItemStatusClass(status) {
  switch ((status || "").toUpperCase()) {
    case "PENDING":
      return "order-status-pending";
    case "PREPARING":
      return "order-status-preparing";
    case "READY":
      return "order-status-ready";
    case "SERVED":
      return "order-status-served";
    case "CANCELLED":
      return "order-status-cancelled";
    default:
      return "order-status-unknown";
  }
}

/**
 * Lấy text hiển thị cho status
 */
function getStatusText(status) {
  switch (status) {
    case "PENDING":
      return "Đang chờ";
    case "PREPARING":
      return "Đang chuẩn bị";
    case "READY":
      return "Sẵn sàng";
    case "SERVED":
      return "Đã phục vụ";
    case "CANCELLED":
      return "Đã hủy";
    default:
      return status;
  }
}

function handleTableReleaseSuccess(data) {
  // alert(data.message || 'Yêu cầu thanh toán đã được gửi! Sẽ quay lại trang chọn bàn sau 3 giây.');

  const releaseButton = document.querySelector(`[onclick^="releaseTable"]`);
  if (releaseButton) {
    releaseButton.disabled = true;
    releaseButton.textContent = "Đã yêu cầu thanh toán";
    releaseButton.style.cursor = "not-allowed";
    releaseButton.style.backgroundColor = "#ccc";
  }

  setTimeout(() => {
    const guestUrl = BASE_URL.replace(/\/$/, "") + "/guest";
    window.location.href = guestUrl;
  }, 1500);
}

window.releaseTable = function (tableId) {
  if (!stompClient || !stompClient.connected) {
    showToast("Chưa kết nối tới máy chủ. Vui lòng đợi...", "warning");
    return;
  }

  const request = {
    tableId: tableId,
    sessionId: guestSessionId,
  };

  showToast("Đang gửi yêu cầu thanh toán...", "success");
  try {
    stompClient.send("/app/guest/table/release", {}, JSON.stringify(request));
  } catch (error) {
    showToast("Không thể gửi yêu cầu thanh toán.", "error");
    alert("Không thể gửi yêu cầu. Vui lòng thử lại.");
  }
};

window.addEventListener("beforeunload", function () {
  if (stompClient && stompClient.connected) {
    stompClient.disconnect(() =>
      showToast("Đã ngắt kết nối WebSocket.", "warning")
    );
  }
});
