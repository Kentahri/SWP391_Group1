// ===== BIẾN TOÀN CỤC =====
let selectedSizes = [];

// ===== KHỞI TẠO =====
document.addEventListener("DOMContentLoaded", function () {
    const modal = document.getElementById("sizeModal");
    const form = document.getElementById("sizeForm");
    const searchInput = document.getElementById("sizeSearch");

    setupModal(modal);
    setupSearch(searchInput);
});

// ===== XỬ LÝ MODAL =====
function setupModal(modal) {
    // Hiển thị modal nếu cần (bao gồm cả khi có validation errors)
    if (window.sizeOpenModal && modal) {
        modal.classList.add("show");
        const title = document.getElementById("sizeModalTitle");
        if (title) {
            title.textContent =
                window.sizeOpenModal === "create" ? "Thêm kích thước mới" : "Sửa kích thước";
        }

        // Scroll to first error nếu có
        setTimeout(() => {
            const firstError = document.querySelector(".size-error-field");
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
    document.getElementById("sizeModal")?.classList.remove("show");
    window.location.href = "/pizzario/manager/sizes";
}

// ===== TÌM KIẾM KÍCH THƯỚC =====
function setupSearch(searchInput) {
    if (!searchInput) return;

    searchInput.addEventListener("input", function () {
        const searchTerm = this.value.toLowerCase().trim();
        filterSizes(searchTerm);
    });
}

function filterSizes(searchTerm) {
    const sizeItems = document.querySelectorAll(".size-item");
    let visibleCount = 0;

    sizeItems.forEach((item) => {
        const name =
            item.querySelector(".size-name")?.textContent.toLowerCase() || "";
        const usage =
            item.querySelector(".size-muted")?.textContent.toLowerCase() || "";

        const matches =
            name.includes(searchTerm) ||
            usage.includes(searchTerm);

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
            noResultsDiv.className = "size-no-results";
            noResultsDiv.innerHTML = `
        <div class="no-results-icon">Search</div>
        <p>Không tìm thấy kích thước nào với từ khóa "<strong>${escapeHtml(
                searchTerm
            )}</strong>"</p>
      `;
            document.querySelector(".size-list").appendChild(noResultsDiv);
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