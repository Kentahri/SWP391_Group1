// Voucher List JavaScript
document.addEventListener("DOMContentLoaded", function () {
  const modal = document.getElementById("voucherModal");
  const form = document.getElementById("voucherForm");

  // Setup modal
  setupModal(modal, form);

  // Auto-hide error alerts after 5 seconds
  const errorAlerts = document.querySelectorAll(".error-alert");
  errorAlerts.forEach((alert) => {
    setTimeout(() => {
      alert.style.transition = "opacity 0.3s";
      alert.style.opacity = "0";
      setTimeout(() => alert.remove(), 300);
    }, 5000);
  });
});

// Modal functions
function setupModal(modal, form) {
  if (!modal || !form) return;

  // Hiển thị modal nếu cần
  if (window.openModalFlag && modal) {
    modal.classList.add("show");
    const title = document.getElementById("modalTitle");
    if (title) {
      title.textContent =
        window.openModalFlag === "create" ? "Tạo voucher mới" : "Sửa voucher";
    }

    // Scroll to top of modal to show errors
    const modalContent = modal.querySelector(".modal-content");
    if (modalContent) {
      modalContent.scrollTop = 0;
    }
  }

  // Đóng modal khi click bên ngoài
  modal.addEventListener("click", (e) => {
    if (e.target === modal) closeModal();
  });

  // Đóng modal khi nhấn ESC
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && modal.classList.contains("show")) {
      closeModal();
    }
  });

  // Xử lý submit form
  form.addEventListener("submit", function (e) {
    this.action = "/pizzario/manager/vouchers/save";
  });
}

function closeModal() {
  const modal = document.getElementById("voucherModal");
  if (modal) {
    modal.classList.remove("show");
  }
  // Redirect to clean URL without modal
  window.location.href = "/pizzario/manager/vouchers";
}

// Format currency
function formatCurrency(value) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
}

// Format date
function formatDate(dateString) {
  const date = new Date(dateString);
  return date.toLocaleDateString("vi-VN");
}

// Calculate progress percentage
function calculateProgress(used, max) {
  if (max === 0) return 0;
  return Math.min((used / max) * 100, 100);
}
