/**
 * Shift Calendar JavaScript
 * Handles week navigation, filters, shift editing, and tab switching
 */

(function () {
  "use strict";

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
   * Edit shift
   * @param {number} shiftId - Shift ID to edit
   */
  window.editShift = function (shiftId) {
    window.location.href = "/manager/staff_shifts/create?editId=" + shiftId;
  };
})();
