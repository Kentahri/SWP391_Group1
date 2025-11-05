// ===== BIẾN TOÀN CỤC =====
let selectedProductSizes = [];

// ===== KHỞI TẠO =====
document.addEventListener("DOMContentLoaded", function () {
    const modal = document.getElementById("psModal");
    const form = document.getElementById("psForm");
    const searchInput = document.getElementById("psSearch");

    setupModal(modal);
    setupSearch(searchInput);
});

// ===== XỬ LÝ MODAL =====
function setupModal(modal) {
    // Hiển thị modal nếu cần (bao gồm cả khi có validation errors)
    if (window.psOpenModal && modal) {
        modal.classList.add("show");
        const title = document.getElementById("psModalTitle");
        if (title) {
            title.textContent =
                window.psOpenModal === "create" ? "Thêm giá theo size" : "Sửa giá theo size";
        }

        // Scroll to first error nếu có
        setTimeout(() => {
            const firstError = document.querySelector(".ps-error-field");
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
    document.getElementById("psModal")?.classList.remove("show");
    window.location.href = "/pizzario/manager/product-sizes";
}

// ===== TÌM KIẾM GIÁ THEO SIZE =====
function setupSearch(searchInput) {
    if (!searchInput) return;

    searchInput.addEventListener("input", function () {
        const searchTerm = this.value.toLowerCase().trim();
        filterProductSizes(searchTerm);
    });
}

function filterProductSizes(searchTerm) {
    const psItems = document.querySelectorAll(".ps-item");
    let visibleCount = 0;

    psItems.forEach((item) => {
        const name =
            item.querySelector(".ps-name")?.textContent.toLowerCase() || "";
        const size =
            item.querySelector(".ps-badge-category")?.textContent.toLowerCase() || "";
        const price =
            item.querySelector(".ps-price")?.textContent.toLowerCase() || "";

        const matches =
            name.includes(searchTerm) ||
            size.includes(searchTerm) ||
            price.includes(searchTerm);

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
            noResultsDiv.className = "ps-no-results";
            noResultsDiv.innerHTML = `
        <div class="no-results-icon">Search</div>
        <p>Không tìm thấy giá nào với từ khóa "<strong>${escapeHtml(
                searchTerm
            )}</strong>"</p>
      `;
            document.querySelector(".ps-list").appendChild(noResultsDiv);
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