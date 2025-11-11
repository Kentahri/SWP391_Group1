"use strict";

/**
 * WebSocket Manager for Cashier Takeaway Page
 * Handles real-time product status updates from manager
 */
let stompClient = null;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;
const BASE_URL = window.APP_CTX || "/pizzario";

// ✅ THÊM: Khởi tạo khi page load
window.addEventListener("DOMContentLoaded", function () {
    console.log("[Cashier Takeaway] Initializing WebSocket...");
    connectWebSocket();
});

// ✅ THÊM: Hàm connect WebSocket
function connectWebSocket() {
    const socketUrl = BASE_URL.replace(/\/$/, "") + "/ws";
    const socket = new SockJS(socketUrl);
    stompClient = Stomp.over(socket);
    
    // Tắt debug log nếu không phải localhost
    if (window.location.hostname !== "localhost") {
      stompClient.debug = null;
    }
    
    stompClient.connect({}, onConnected, onError);
    console.log("[Cashier Takeaway] Connecting to WebSocket:", socketUrl);
}

// ✅ SỬA LẠI: Hàm onConnected (file hiện tại bị thiếu frame parameter)
function onConnected(frame) {
  console.log("[Cashier Takeaway] WebSocket connected:", frame);
  reconnectAttempts = 0;

  // Subscribe vào topic products-status
  stompClient.subscribe(
    "/topic/products-status",
    handleProductStatusUpdate
  );

  console.log("[Cashier Takeaway] Subscribed to WebSocket topics");
}

function onError(error) {
  console.error("[Cashier Takeaway] WebSocket error:", error);
  
  if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
    const delay = Math.pow(2, reconnectAttempts) * 1000; // Exponential backoff
    console.log(`[Cashier Takeaway] Reconnecting in ${delay / 1000} seconds...`);
    
    setTimeout(() => {
      reconnectAttempts++;
      connectWebSocket();
    }, delay);
  } else {
    console.error("[Cashier Takeaway] Max reconnection attempts reached. Please refresh the page.");
    showToast("Mất kết nối WebSocket. Vui lòng tải lại trang.", "error");
  }
}

// ✅ THÊM: Disconnect khi đóng page
window.addEventListener("beforeunload", function () {
  if (stompClient && stompClient.connected) {
    stompClient.disconnect();
    console.log("[Cashier Takeaway] WebSocket disconnected");
  }
});

/**
 * Xử lý cập nhật trạng thái active của sản phẩm từ manager
 * Manager có thể bật/tắt món ăn, và cashier sẽ nhận được thông báo real-time
 */
function handleProductStatusUpdate(message) {
    try {
      const data = JSON.parse(message.body);
      console.log("[Cashier Takeaway] Product status update received:", data);
  
      // Xử lý các loại message: PRODUCT_TOGGLED, PRODUCT_UPDATED, PRODUCT_CREATED
      if (data.type === "PRODUCT_TOGGLED" || 
          data.type === "PRODUCT_UPDATED" || 
          data.type === "PRODUCT_CREATED") {
        
        if (data.product && data.product.id) {
          const productId = data.product.id;
          const isActive = data.product.active === true;
          
          console.log(`[Cashier Takeaway] Product ${productId} active status: ${isActive}`);
          
          // Cập nhật UI: Ẩn/hiện món ăn trong menu
          updateProductVisibility(productId, isActive);
          
          // Hiển thị thông báo cho cashier
          const productName = data.product.name || "Món ăn";
          if (isActive) {
            showToast(`${productName} đã được thêm vào thực đơn`, "success");
          } else {
            showToast(`${productName} đã được ẩn khỏi thực đơn`, "warning");
          }
        }
      }
    } catch (error) {
      console.error("[Cashier Takeaway] Error handling product status update:", error);
    }
  }

  /**
 * Cập nhật hiển thị của product card trong menu dựa trên trạng thái active
 * @param {number} productId - ID của sản phẩm
 * @param {boolean} isActive - Trạng thái active (true = hiển thị, false = ẩn)
 */
function updateProductVisibility(productId, isActive) {
    // Tìm product grid (có thể là productGrid hoặc tên khác tùy HTML của bạn)
    const productGrid = document.getElementById("productGrid") || 
                        document.querySelector(".product-grid") ||
                        document.querySelector(".menu-products");
    
    if (!productGrid) {
      console.warn("[Cashier Takeaway] Product grid not found");
      return;
    }
  
    // Tìm product card bằng data-product-id attribute (ưu tiên)
    let productCard = productGrid.querySelector(`[data-product-id="${productId}"]`);
    
    // Fallback: nếu không tìm thấy bằng data-product-id, tìm bằng các cách khác
    if (!productCard) {
      // Cách 1: Tìm bằng form input với productId
      const productCards = productGrid.querySelectorAll(".product-card");
      for (const card of productCards) {
        const form = card.querySelector('form');
        if (form) {
          const productIdInput = form.querySelector('input[name="productId"]');
          if (productIdInput && productIdInput.value == productId) {
            productCard = card;
            break;
          }
        }
      }
    }
    
    // Fallback 2: Tìm bằng class hoặc id trực tiếp
    if (!productCard) {
      productCard = productGrid.querySelector(`#product-${productId}`) ||
                    productGrid.querySelector(`.product-${productId}`);
    }
  
    if (productCard) {
      if (isActive) {
        // ✅ HIỂN THỊ món ăn
        productCard.classList.remove("inactive", "product-inactive");
        productCard.style.display = "";
        productCard.style.opacity = "0";
        
        // Animation fade in
        setTimeout(() => {
          productCard.style.transition = "opacity 0.5s ease";
          productCard.style.opacity = "1";
        }, 10);
        
        // Bật lại các button/input trong card
        const buttons = productCard.querySelectorAll("button, input, select");
        buttons.forEach(btn => btn.disabled = false);
        
      } else {
        // ❌ ẨN món ăn
        productCard.classList.add("inactive", "product-inactive");
        productCard.style.opacity = "1";
        
        // Animation fade out
        setTimeout(() => {
          productCard.style.transition = "opacity 0.5s ease";
          productCard.style.opacity = "0";
          setTimeout(() => {
            productCard.style.display = "none";
          }, 500);
        }, 10);
        
        // Disable các button/input trong card
        const buttons = productCard.querySelectorAll("button, input, select");
        buttons.forEach(btn => btn.disabled = true);
      }
  
      // Thêm animation pulse để người dùng thấy thay đổi
      productCard.style.animation = "none";
      setTimeout(() => {
        productCard.style.animation = "pulse 0.5s ease";
      }, 10);
  
      console.log(`[Cashier Takeaway] Product ${productId} visibility updated: ${isActive ? "visible" : "hidden"}`);
    } else {
      // Nếu không tìm thấy product card (có thể là product mới được tạo)
      // Reload trang để hiển thị product mới
      if (isActive) {
        console.log(`[Cashier Takeaway] Product ${productId} not found in DOM, reloading page to show new product`);
        showToast("Có món ăn mới được thêm vào. Đang tải lại trang...", "info");
        setTimeout(() => {
          location.reload();
        }, 1000);
      }
    }
  }

  /**
 * Hiển thị toast notification
 * @param {string} message - Nội dung thông báo
 * @param {string} type - Loại: success, error, warning, info
 */
function showToast(message, type = "info") {
  // Nếu bạn đã có hệ thống toast, dùng nó
  // Nếu chưa, đây là implementation đơn giản:
  
  // Kiểm tra xem có thư viện toast nào không (như Toastify, SweetAlert, etc.)
  if (typeof Toastify !== "undefined") {
    Toastify({
      text: message,
      duration: 3000,
      gravity: "top",
      position: "right",
      backgroundColor: getToastColor(type),
    }).showToast();
    return;
  }
  
  // Fallback: console log hoặc alert
  console.log(`[Toast ${type.toUpperCase()}]: ${message}`);
  
  // Hoặc tạo toast đơn giản bằng DOM
  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  toast.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    padding: 15px 20px;
    background: ${getToastColor(type)};
    color: white;
    border-radius: 5px;
    box-shadow: 0 2px 5px rgba(0,0,0,0.2);
    z-index: 10000;
    animation: slideInRight 0.3s ease;
  `;
  
  document.body.appendChild(toast);
  
  setTimeout(() => {
    toast.style.animation = "slideOutRight 0.3s ease";
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

function getToastColor(type) {
  switch(type) {
    case "success": return "#28a745";
    case "error": return "#dc3545";
    case "warning": return "#ffc107";
    case "info": return "#17a2b8";
    default: return "#6c757d";
  }
}