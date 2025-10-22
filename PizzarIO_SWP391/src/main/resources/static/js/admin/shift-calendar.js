(function () {
  "use strict";

  document.addEventListener("DOMContentLoaded", function () {
    setupShiftTypeModal();
    setupStaffShiftModal();
  });

  function setupShiftTypeModal() {
    const modal = document.getElementById("shiftTypeModal");
    const form = document.getElementById("shiftTypeForm");

    if (!modal || !form) return;

    if (window.openShiftModalFlag) {
      modal.classList.add("show");
      const title = document.getElementById("shiftModalTitle");
      if (title) {
        title.textContent =
          window.openShiftModalFlag === "create"
            ? "Thêm loại ca mới"
            : "Sửa loại ca";
      }
      switchTab("shifts");
    }

    modal.addEventListener("click", (e) => {
      if (e.target === modal) closeShiftModal();
    });

    document.addEventListener("keydown", (e) => {
      if (e.key === "Escape" && modal.classList.contains("show")) {
        closeShiftModal();
      }
    });
  }

  window.openShiftModal = function () {
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

  window.editShiftType = function (shiftId) {
    window.location.href = "/pizzario/manager/shift/edit/" + shiftId;
  };

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

  window.navigateWeek = function (offset) {
    const url = new URL(window.location);
    const currentOffset = parseInt(url.searchParams.get("weekOffset")) || 0;
    url.searchParams.set("weekOffset", currentOffset + offset);
    window.location.href = url.toString();
  };

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

  window.editShift = function (shiftId) {
    // Preserve current URL parameters when editing
    const currentUrl = new URL(window.location);
    const editUrl = new URL(
      "/pizzario/manager/staff_shifts/edit/" + shiftId,
      window.location.origin
    );

    // Copy all query parameters from current URL to edit URL
    currentUrl.searchParams.forEach((value, key) => {
      editUrl.searchParams.set(key, value);
    });

    window.location.href = editUrl.toString();
  };

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

    modal.addEventListener("click", (e) => {
      if (e.target === modal) closeStaffShiftModal();
    });

    document.addEventListener("keydown", (e) => {
      if (e.key === "Escape" && modal.classList.contains("show")) {
        closeStaffShiftModal();
      }
    });
  }

  window.openStaffShiftModal = function () {
    // Redirect to server to get a fresh empty form, preserve current URL parameters
    const currentUrl = new URL(window.location);
    const createUrl = new URL(
      "/pizzario/manager/staff_shifts/create",
      window.location.origin
    );

    // Copy all query parameters from current URL to create URL
    currentUrl.searchParams.forEach((value, key) => {
      createUrl.searchParams.set(key, value);
    });

    window.location.href = createUrl.toString();
  };

  window.closeStaffShiftModal = function () {
    const modal = document.getElementById("staffShiftModal");
    if (modal) {
      modal.classList.remove("show");
    }
  };
})();
