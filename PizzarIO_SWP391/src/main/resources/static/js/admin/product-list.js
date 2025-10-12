// Tất cả logic trong 1 DOMContentLoaded để tránh conflict
document.addEventListener("DOMContentLoaded", function () {
  console.log("Page loaded, checking openModalFlag...");

  const openModal = window.openModalFlag;
  const modal = document.getElementById("productModal");
  const form = document.getElementById("productForm");
  const modalTitle = document.getElementById("modalTitle");

  // Xử lý timeout cho tất cả ảnh product
  const productImages = document.querySelectorAll(".product-image img");
  productImages.forEach(function (img) {
    // Set timeout 5 giây cho mỗi ảnh
    const timeoutId = setTimeout(function () {
      if (!img.complete || img.naturalHeight === 0) {
        console.warn("Image timeout:", img.src);
        img.src = ""; // Clear src để stop loading
        img.style.display = "none";
      }
    }, 5000);

    // Clear timeout nếu ảnh load thành công
    img.addEventListener("load", function () {
      clearTimeout(timeoutId);
    });

    // Clear timeout nếu ảnh lỗi
    img.addEventListener("error", function () {
      clearTimeout(timeoutId);
    });
  });

  // Tự động mở modal nếu có flag từ controller
  if (openModal && modal) {
    console.log("Opening modal with mode:", openModal);
    modal.classList.add("show");

    if (modalTitle) {
      if (openModal === "create") {
        modalTitle.textContent = "Thêm món mới";
      } else if (openModal === "edit") {
        modalTitle.textContent = "Sửa món ăn";
      }
    }
  }

  // Set form action khi submit
  if (form) {
    form.addEventListener("submit", function (e) {
      this.action = "/pizzario/manager/products/save";
      console.log("Form submitting to:", this.action);
    });
  }

  // Click ngoài modal để đóng
  if (modal) {
    modal.addEventListener("click", function (e) {
      if (e.target === this) {
        closeModal();
      }
    });
  }

  // Đóng modal khi nhấn ESC
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape" && modal && modal.classList.contains("show")) {
      closeModal();
    }
  });
});

// Đóng modal function
function closeModal() {
  const modal = document.getElementById("productModal");
  if (modal) {
    modal.classList.remove("show");
  }
  // Redirect về trang products để reset state
  window.location.href = "/pizzario/manager/products";
}
