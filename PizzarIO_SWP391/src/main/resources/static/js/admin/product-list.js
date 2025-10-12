// Tự động mở modal nếu có flag từ controller
window.addEventListener("DOMContentLoaded", function () {
  // Lấy giá trị openModal từ biến global (được set từ HTML)
  const openModal = window.openModalFlag;

  console.log("openModalFlag:", openModal); // Debug

  if (openModal) {
    const modal = document.getElementById("productModal");
    console.log("Modal element:", modal); // Debug

    if (modal) {
      modal.classList.add("show");
      console.log("Modal opened with mode:", openModal); // Debug

      const modalTitle = document.getElementById("modalTitle");
      if (openModal === "create") {
        modalTitle.textContent = "Thêm món mới";
      } else if (openModal === "edit") {
        modalTitle.textContent = "Sửa món ăn";
      }
    }
  }
});

// Đổi action form dựa vào có id hay không
document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("productForm");

  if (form) {
    form.addEventListener("submit", function (e) {
      // Set action URL cho form
      this.action = "/pizzario/manager/products/save";
    });
  }
});

// Đóng modal
function closeModal() {
  const modal = document.getElementById("productModal");
  modal.classList.remove("show");

  // Redirect về trang products để reset state
  window.location.href = "/pizzario/manager/products";
}

// Click ngoài modal để đóng
document.addEventListener("DOMContentLoaded", function () {
  const modal = document.getElementById("productModal");

  if (modal) {
    modal.addEventListener("click", function (e) {
      if (e.target === this) {
        closeModal();
      }
    });
  }
});

// Đóng modal khi nhấn ESC
document.addEventListener("keydown", function (e) {
  if (e.key === "Escape") {
    const modal = document.getElementById("productModal");
    if (modal && modal.classList.contains("show")) {
      closeModal();
    }
  }
});
