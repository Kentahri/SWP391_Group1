// Shift List JavaScript
document.addEventListener("DOMContentLoaded", function () {
  const modal = document.getElementById("shiftModal");
  const form = document.getElementById("shiftForm");

  // Setup modal
  setupModal(modal, form);
});

// Modal functions
function setupModal(modal, form) {
  if (!modal || !form) return;

  // Show modal if needed
  if (window.openShiftModalFlag && modal) {
    modal.classList.add("show");
    const title = document.getElementById("modalTitle");
    if (title) {
      title.textContent =
        window.openShiftModalFlag === "create"
          ? "Thêm ca làm việc mới"
          : "Sửa ca làm việc";
    }
  }

  // Close modal when clicking outside
  modal.addEventListener("click", (e) => {
    if (e.target === modal) closeShiftModal();
  });

  // Close modal on ESC key
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && modal.classList.contains("show")) {
      closeShiftModal();
    }
  });
}

function openShiftModal() {
  // Redirect to create endpoint to properly initialize form
  window.location.href =
    "/pizzario/manager/shift/create?modal=true&returnPage=shifts";
}

function closeShiftModal() {
  const modal = document.getElementById("shiftModal");
  if (modal) {
    modal.classList.remove("show");
    window.location.href = "/pizzario/manager/shifts";
  }
}

function editShiftType(shiftId) {
  window.location.href =
    "/pizzario/manager/shift/edit/" + shiftId + "?returnPage=shifts";
}
