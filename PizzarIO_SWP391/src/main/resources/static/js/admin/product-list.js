// ===== BIẾN TOÀN CỤC =====
let selectedProducts = [];

// ===== KHỞI TẠO =====
document.addEventListener("DOMContentLoaded", function () {
  const modal = document.getElementById("productModal");
  const form = document.getElementById("productForm");
  const searchInput = document.getElementById("productSearch");

  setupImageTimeouts();
  setupModal(modal);
  setupComboFeature(form);
  setupSearch(searchInput);
});

// ===== XỬ LÝ ẢNH =====
function setupImageTimeouts() {
  document.querySelectorAll(".product-image img").forEach((img) => {
    const timeoutId = setTimeout(() => {
      if (!img.complete || img.naturalHeight === 0) {
        img.src = "";
        img.style.display = "none";
      }
    }, 5000);

    img.addEventListener("load", () => clearTimeout(timeoutId));
    img.addEventListener("error", () => clearTimeout(timeoutId));
  });
}

// ===== XỬ LÝ MODAL =====
function setupModal(modal) {
  // Hiển thị modal nếu cần (bao gồm cả khi có validation errors)
  if (window.openModalFlag && modal) {
    modal.classList.add("show");
    const title = document.getElementById("modalTitle");
    if (title) {
      title.textContent =
        window.openModalFlag === "create" ? "Thêm món mới" : "Sửa món ăn";
    }

    // Scroll to first error nếu có
    setTimeout(() => {
      const firstError = document.querySelector(".error-field");
      if (firstError) {
        firstError.scrollIntoView({ behavior: "smooth", block: "center" });
        firstError.focus();
      }
    }, 100);
  }

  // Đóng modal khi click bên ngoài
  modal?.addEventListener("click", (e) => {
    if (e.target === modal) closeModal();
  });

  // Đóng modal khi nhấn ESC
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && modal?.classList.contains("show")) {
      closeModal();
    }
  });
}

function closeModal() {
  document.getElementById("productModal")?.classList.remove("show");
  window.location.href = "/pizzario/manager/products";
}

// ===== XỬ LÝ COMBO =====
function setupComboFeature(form) {
  if (!form) return;

  const categorySelect = form.querySelector('select[name="categoryId"]');
  const comboSection = document.getElementById("comboProductsSection");

  if (!categorySelect || !comboSection) return;

  // Kiểm tra category khi load và khi thay đổi
  const checkCategory = () => {
    const categoryName =
      categorySelect.options[
        categorySelect.selectedIndex
      ]?.text.toLowerCase() || "";

    if (categoryName.includes("combo")) {
      comboSection.style.display = "block";
      loadProducts();
    } else {
      comboSection.style.display = "none";
      selectedProducts = [];
      updateSelectedList();
    }
  };

  checkCategory();
  categorySelect.addEventListener("change", checkCategory);

  // Xử lý submit form
  form.addEventListener("submit", function (e) {
    const categoryName =
      categorySelect.options[
        categorySelect.selectedIndex
      ]?.text.toLowerCase() || "";

    if (categoryName.includes("combo") && selectedProducts.length === 0) {
      e.preventDefault();
      alert("Vui lòng chọn ít nhất một món ăn cho combo");
      return;
    }

    this.action = "/pizzario/manager/products/save";

    // Thêm dữ liệu combo vào form
    if (selectedProducts.length > 0) {
      let input = document.getElementById("selectedProductsJson");
      if (!input) {
        input = document.createElement("input");
        input.type = "hidden";
        input.id = "selectedProductsJson";
        input.name = "selectedProductsJson";
        this.appendChild(input);
      }
      input.value = JSON.stringify(selectedProducts);
    }
  });
}

// Load danh sách sản phẩm cho combo
function loadProducts() {
  const container = document.getElementById("productSelectionList");
  if (!container) return;

  container.innerHTML = '<div class="loading">Đang tải...</div>';

  fetch("/pizzario/manager/products/search?query=")
    .then((res) => res.json())
    .then((products) => {
      const activeProducts = products.filter(
        (p) => p.active && !p.categoryName?.toLowerCase().includes("combo")
      );

      if (activeProducts.length === 0) {
        container.innerHTML =
          '<div class="no-products">Không có sản phẩm</div>';
        return;
      }

      container.innerHTML = activeProducts
        .map(
          (p) => `
        <div class="product-selection-item" data-id="${p.id}" data-price="${p.basePrice}" data-name="${p.name}">
          <label>
            <input type="checkbox" class="product-checkbox" data-id="${p.id}">
            <span>${p.name}</span>
          </label>
          <span class="product-price">${p.basePriceFormatted}</span>
          <div class="quantity-control" style="display: none;">
            <input type="number" min="1" value="1" class="quantity-input" data-id="${p.id}">
          </div>
        </div>
      `
        )
        .join("");

      // Gắn sự kiện cho checkbox
      container.querySelectorAll(".product-checkbox").forEach((cb) => {
        cb.addEventListener("change", function () {
          const item = this.closest(".product-selection-item");
          const quantityControl = item.querySelector(".quantity-control");

          const id = this.dataset.id;
          const name = item.dataset.name;
          const price = parseFloat(item.dataset.price);

          if (this.checked) {
            quantityControl.style.display = "flex";
            selectedProducts.push({
              id: id,
              name: name,
              price: price,
              quantity: 1,
            });
          } else {
            quantityControl.style.display = "none";
            selectedProducts = selectedProducts.filter((p) => p.id !== id);
          }

          updateSelectedList();
          updateDescription();
        });
      });

      // Gắn sự kiện cho input số lượng
      container.querySelectorAll(".quantity-input").forEach((input) => {
        input.addEventListener("change", function () {
          const product = selectedProducts.find(
            (p) => p.id === this.dataset.id
          );
          if (product) {
            product.quantity = parseInt(this.value) || 1;
            updateSelectedList();
            updateDescription();
          }
        });
      });
    })
    .catch((err) => {
      console.error(err);
      container.innerHTML = '<div class="error">Lỗi tải sản phẩm</div>';
    });
}

// Cập nhật danh sách món đã chọn
function updateSelectedList() {
  const list = document.getElementById("selectedProductsList");
  if (!list) return;

  if (selectedProducts.length === 0) {
    list.innerHTML =
      '<div class="no-selected-products">Chưa chọn món nào</div>';
    return;
  }

  list.innerHTML = selectedProducts
    .map(
      (p) => `
    <div class="selected-product-item">
      <span class="selected-product-name">${p.name}</span>
      <span class="selected-product-quantity">x${p.quantity}</span>
    </div>
  `
    )
    .join("");
}

// Cập nhật mô tả và giá
function updateDescription() {
  const form = document.getElementById("productForm");
  if (!form) return;

  const descField = form.querySelector('textarea[name="description"]');
  const priceField = form.querySelector('input[name="basePrice"]');

  if (selectedProducts.length === 0) {
    if (descField) descField.value = "";
    return;
  }

  // Cập nhật mô tả
  if (descField) {
    descField.value = selectedProducts
      .map((p) => `${p.name} x${p.quantity}`)
      .join(", ");
  }

  // Tính và cập nhật giá (giảm 10%)
  if (priceField) {
    const total = selectedProducts.reduce(
      (sum, p) => sum + p.price * p.quantity,
      0
    );
    priceField.value = Math.round(total * 0.9);
  }
}

// ===== TÌM KIẾM SẢN PHẨM =====
function setupSearch(searchInput) {
  if (!searchInput) return;

  searchInput.addEventListener("input", function () {
    const searchTerm = this.value.toLowerCase().trim();
    filterProducts(searchTerm);
  });
}

function filterProducts(searchTerm) {
  const productItems = document.querySelectorAll(".product-item");
  let visibleCount = 0;

  productItems.forEach((item) => {
    const name =
      item.querySelector(".product-name")?.textContent.toLowerCase() || "";
    const description =
      item.querySelector(".product-description")?.textContent.toLowerCase() ||
      "";
    const price =
      item.querySelector(".product-price")?.textContent.toLowerCase() || "";
    const category =
      item.querySelector(".badge-category")?.textContent.toLowerCase() || "";
    const status =
      item.querySelector(".status-badge")?.textContent.toLowerCase() || "";

    const matches =
      name.includes(searchTerm) ||
      description.includes(searchTerm) ||
      price.includes(searchTerm) ||
      category.includes(searchTerm) ||
      status.includes(searchTerm);

    if (matches) {
      item.style.display = "";
      visibleCount++;
    } else {
      item.style.display = "none";
    }
  });

  // Show/hide no results message
  showNoResults(visibleCount === 0, searchTerm);
}

function showNoResults(show, searchTerm) {
  let noResultsDiv = document.getElementById("noResults");

  if (show) {
    if (!noResultsDiv) {
      noResultsDiv = document.createElement("div");
      noResultsDiv.id = "noResults";
      noResultsDiv.className = "no-results";
      noResultsDiv.innerHTML = `
        <div class="no-results-icon">🔍</div>
        <p>Không tìm thấy món ăn nào với từ khóa "<strong>${escapeHtml(
          searchTerm
        )}</strong>"</p>
      `;
      document.querySelector(".product-list").appendChild(noResultsDiv);
    }
  } else {
    if (noResultsDiv) {
      noResultsDiv.remove();
    }
  }
}

function escapeHtml(text) {
  const map = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#039;",
  };
  return text.replace(/[&<>"']/g, (m) => map[m]);
}
