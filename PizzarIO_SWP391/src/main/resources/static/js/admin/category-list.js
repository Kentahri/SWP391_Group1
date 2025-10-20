// Category List JavaScript
document.addEventListener("DOMContentLoaded", function () {
  const modal = document.getElementById("categoryModal");
  const form = document.getElementById("categoryForm");
  const searchInput = document.getElementById("categorySearch");

  // Setup modal
  setupModal(modal, form);

  // Setup search
  setupSearch(searchInput);
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
        window.openModalFlag === "create"
          ? "Thêm danh mục mới"
          : "Sửa danh mục";
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
    this.action = "/pizzario/manager/categories/save";
  });
}

function closeModal() {
  document.getElementById("categoryModal")?.classList.remove("show");
  window.location.href = "/pizzario/manager/categories";
}

// Search functions
function setupSearch(searchInput) {
  if (!searchInput) return;

  searchInput.addEventListener("input", function () {
    const searchTerm = this.value.toLowerCase().trim();
    filterCategories(searchTerm);
  });
}

function filterCategories(searchTerm) {
  const tableRows = document.querySelectorAll(".table tbody tr");
  let visibleCount = 0;

  tableRows.forEach((row) => {
    const id =
      row.querySelector("td:nth-child(1)")?.textContent.toLowerCase() || "";
    const name =
      row.querySelector("td:nth-child(2)")?.textContent.toLowerCase() || "";
    const description =
      row.querySelector("td:nth-child(3)")?.textContent.toLowerCase() || "";
    const status =
      row.querySelector("td:nth-child(4)")?.textContent.toLowerCase() || "";

    const matches =
      id.includes(searchTerm) ||
      name.includes(searchTerm) ||
      description.includes(searchTerm) ||
      status.includes(searchTerm);

    if (matches) {
      row.style.display = "";
      visibleCount++;
    } else {
      row.style.display = "none";
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
        <p>Không tìm thấy danh mục nào với từ khóa "<strong>${escapeHtml(
          searchTerm
        )}</strong>"</p>
      `;
      document
        .querySelector(".category-table-container")
        .appendChild(noResultsDiv);
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
