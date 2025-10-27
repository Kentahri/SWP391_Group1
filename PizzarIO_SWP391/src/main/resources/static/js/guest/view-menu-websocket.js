"use strict";

/**
 * WebSocket Manager for the Guest Menu Page
 * Handles connection and payment requests.
 */
let stompClient = null;
let guestSessionId = null;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;
const BASE_URL = window.APP_CTX || "/pizzario";

window.addEventListener("DOMContentLoaded", function () {
  initializeGuestSession();
  connectWebSocket();
});

function initializeGuestSession() {
  guestSessionId =
    "guest_" + Math.random().toString(36).slice(2) + "_" + Date.now();
  console.log("Guest session initialized:", guestSessionId);
}

function connectWebSocket() {
  const socketUrl = BASE_URL.replace(/\/$/, "") + "/ws";
  const socket = new SockJS(socketUrl);
  stompClient = Stomp.over(socket);
  if (window.location.hostname !== "localhost") stompClient.debug = null;
  stompClient.connect({}, onConnected, onError);
}

function onConnected(frame) {
  showToast("Đã kết nối WebSocket thành công.", "success");
  reconnectAttempts = 0;
  stompClient.subscribe(
    "/queue/guest-" + guestSessionId,
    handlePersonalMessage
  );
  showToast("Đã đăng ký kênh cá nhân cho phiên: " + guestSessionId, "success");
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
    showToast("Nhận phản hồi từ máy chủ.", "success");
    if (data.type === "SUCCESS") {
      handleTableReleaseSuccess(data);
    } else if (data.type === "ERROR") {
      showToast(data.message || "Có lỗi xảy ra khi gửi yêu cầu.", "error");
    }
  } catch (error) {
    showToast("Lỗi khi xử lý phản hồi từ máy chủ.", "error");
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
