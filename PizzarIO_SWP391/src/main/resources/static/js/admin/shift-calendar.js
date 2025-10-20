/**
 * Shift Calendar JavaScript
 * Handles week navigation, filters, shift editing, and tab switching
 */

(function () {
  "use strict";

  // Initialize on DOM ready
  document.addEventListener("DOMContentLoaded", function () {
    setupShiftTypeModal();
    setupStaffShiftModal();
  });

  /**
   * Setup Shift Type Modal
   */
  function setupShiftTypeModal() {
    const modal = document.getElementById("shiftTypeModal");
    const form = document.getElementById("shiftTypeForm");

    if (!modal || !form) return;

    // Show modal if needed (for edit mode)
    if (window.openShiftModalFlag) {
      modal.classList.add("show");
      const title = document.getElementById("shiftModalTitle");
      if (title) {
        title.textContent =
          window.openShiftModalFlag === "create"
            ? "Thêm loại ca mới"
            : "Sửa loại ca";
      }
      // Switch to shifts tab when modal opens
      switchTab("shifts");
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

  /**
   * Open Shift Type Modal (for create)
   */
  window.openShiftModal = function () {
    // Redirect to create endpoint to properly initialize form
    window.location.href =
      "/pizzario/manager/shift/create?modal=true&returnPage=staff_shifts";
  };

  /**
   * Close Shift Type Modal
   */
  window.closeShiftModal = function () {
    const modal = document.getElementById("shiftTypeModal");
    if (modal) {
      modal.classList.remove("show");
      // Redirect to clear any edit parameters
      window.location.href = "/pizzario/manager/staff_shifts";
    }
  };

  /**
   * Edit Shift Type
   * @param {number} shiftId - Shift ID to edit
   */
  window.editShiftType = function (shiftId) {
    window.location.href = "/pizzario/manager/shift/edit/" + shiftId;
  };

  /**
   * Switch between tabs
   * @param {string} tabName - 'calendar' or 'shifts'
   */
  window.switchTab = function (tabName) {
    // Remove active class from all tabs and buttons
    document.querySelectorAll(".tab-btn").forEach((btn) => {
      btn.classList.remove("active");
    });
    document.querySelectorAll(".tab-content").forEach((content) => {
      content.classList.remove("active");
    });

    // Add active class to selected tab
    const activeBtn = document.querySelector(`.tab-btn[onclick*="${tabName}"]`);
    const activeContent = document.getElementById(`tab-${tabName}`);

    if (activeBtn) activeBtn.classList.add("active");
    if (activeContent) activeContent.classList.add("active");
  };

  /**
   * Navigate to different week
   * @param {number} offset - Week offset (-1 for previous, +1 for next)
   */
  window.navigateWeek = function (offset) {
    const url = new URL(window.location);
    const currentOffset = parseInt(url.searchParams.get("weekOffset")) || 0;
    url.searchParams.set("weekOffset", currentOffset + offset);
    window.location.href = url.toString();
  };

  /**
   * Filter by staff
   */
  window.filterByStaff = function () {
    const staffId = document.getElementById("staffFilter").value;
    const url = new URL(window.location);
    if (staffId) {
      url.searchParams.set("staffId", staffId);
    } else {
      url.searchParams.delete("staffId");
    }
    window.location.href = url.toString();
  };

  /**
   * Filter by shift type
   */
  window.filterByShift = function () {
    const shiftId = document.getElementById("shiftFilter").value;
    const url = new URL(window.location);
    if (shiftId) {
      url.searchParams.set("shiftId", shiftId);
    } else {
      url.searchParams.delete("shiftId");
    }
    window.location.href = url.toString();
  };

  /**
   * Edit shift (Staff Shift assignment)
   * @param {number} shiftId - Staff Shift ID to edit
   */
  window.editShift = function (shiftId) {
    window.location.href =
      "/pizzario/manager/staff_shifts/edit/" + shiftId + "?modal=true";
  };

  /**
   * Setup Staff Shift Modal
   */
  function setupStaffShiftModal() {
    const modal = document.getElementById("staffShiftModal");
    const form = document.getElementById("staffShiftForm");

    if (!modal || !form) return;

    // Show modal if needed (for edit mode)
    if (window.openStaffShiftModalFlag) {
      modal.classList.add("show");
      const title = document.getElementById("staffShiftModalTitle");
      if (title) {
        title.textContent =
          window.openStaffShiftModalFlag === "create"
            ? "Thêm ca làm việc"
            : "Sửa ca làm việc";
      }
    }

    // Close modal when clicking outside
    modal.addEventListener("click", (e) => {
      if (e.target === modal) closeStaffShiftModal();
    });

    // Close modal on ESC key
    document.addEventListener("keydown", (e) => {
      if (e.key === "Escape" && modal.classList.contains("show")) {
        closeStaffShiftModal();
      }
    });
  }

  /**
   * Open Staff Shift Modal (for create)
   */
  window.openStaffShiftModal = function () {
    const modal = document.getElementById("staffShiftModal");
    const form = document.getElementById("staffShiftForm");
    const title = document.getElementById("staffShiftModalTitle");

    if (modal && form && title) {
      // Reset form for create mode
      form.reset();
      const hiddenId = form.querySelector('input[type="hidden"]');
      if (hiddenId) hiddenId.value = "";
      title.textContent = "Thêm ca làm việc";
      modal.classList.add("show");
    }
  };

  /**
   * Close Staff Shift Modal
   */
  window.closeStaffShiftModal = function () {
    const modal = document.getElementById("staffShiftModal");
    if (modal) {
      modal.classList.remove("show");
    }
  };
})();
