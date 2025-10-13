// ===== BIẾN TOÀN CỤC =====
let selectedProducts = [];

// ===== KHỞI TẠO =====
document.addEventListener("DOMContentLoaded", function () {
  const modal = document.getElementById("productModal");
  const form = document.getElementById("productForm");

  setupImageTimeouts();
  setupModal(modal);
  setupComboFeature(form);
  setupSearch();
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
  // Hiển thị modal nếu cần
  if (window.openModalFlag && modal) {
    modal.classList.add("show");
    const title = document.getElementById("modalTitle");
    if (title) {
      title.textContent =
        window.openModalFlag === "create" ? "Thêm món mới" : "Sửa món ăn";
    }
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

          if (this.checked) {
            quantityControl.style.display = "flex";
            selectedProducts.push({
              id: id,
              name: item.dataset.name,
              price: parseFloat(item.dataset.price),
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
function setupSearch() {
  const searchInput = document.getElementById("productSearch");
  const productList = document.querySelector(".product-list");
  if (!searchInput || !productList) return;

  const originalHtml = productList.innerHTML;

  // Debounce function
  const debounce = (fn, delay) => {
    let timeout;
    return (...args) => {
      clearTimeout(timeout);
      timeout = setTimeout(() => fn(...args), delay);
    };
  };

  // Tìm kiếm
  const search = debounce(async (query) => {
    if (!query.trim()) {
      productList.innerHTML = originalHtml;
      return;
    }

    productList.innerHTML = '<div class="searching">Đang tìm kiếm...</div>';

    try {
      const res = await fetch(
        `/pizzario/manager/products/search?query=${encodeURIComponent(query)}`
      );
      const products = await res.json();

      if (products.length === 0) {
        productList.innerHTML = `<div class="no-results"><p>Không tìm thấy "${query}"</p></div>`;
        return;
      }

      const regex = new RegExp(
        `(${query.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")})`,
        "gi"
      );

      productList.innerHTML = products
        .map((p) => {
          const name = p.name.replace(
            regex,
            '<span class="highlight-match">$1</span>'
          );
          const desc =
            p.description?.replace(
              regex,
              '<span class="highlight-match">$1</span>'
            ) || "";

          return `
          <div class="product-item ${
            !p.active ? "inactive" : ""
          }" id="product-${p.id}">
            <div class="product-image">
              ${
                p.imageURL
                  ? `<img src="${p.imageURL}" alt="${p.name}" loading="lazy" onerror="this.onerror=null;this.src='';this.style.display='none';">`
                  : '<div class="no-image-placeholder">No Image</div>'
              }
            </div>
            <div class="product-info">
              <div class="product-name-line">
                <h3 class="product-name">${name}</h3>
                <div class="product-badges">
                  <span class="badge ${
                    p.active ? "badge-active" : "badge-inactive"
                  }">
                    ${p.active ? "Có sẵn" : "Đã ẩn"}
                  </span>
                  <span class="badge badge-category">${p.categoryName}</span>
                </div>
              </div>
              <p class="product-description">${desc}</p>
              <div class="product-price">${p.basePriceFormatted}</div>
            </div>
            <div class="product-actions">
              <a href="/pizzario/manager/products/edit/${
                p.id
              }" class="btn-icon btn-edit" title="Sửa">Sửa</a>
              <form action="/pizzario/manager/products/toggle/${
                p.id
              }" method="post" class="inline-form" onsubmit="return confirm('Bạn có chắc muốn thay đổi trạng thái?')">
                <button type="submit" class="btn-icon ${
                  p.active ? "btn-hide" : "btn-show"
                }" title="${p.active ? "Ẩn" : "Hiện"}">
                  ${p.active ? "Ẩn" : "Hiện"}
                </button>
              </form>
            </div>
          </div>
        `;
        })
        .join("");
    } catch (err) {
      console.error(err);
      productList.innerHTML =
        '<div class="no-results"><p>Lỗi tìm kiếm</p></div>';
    }
  }, 300);

  searchInput.addEventListener("input", (e) => search(e.target.value));
}
