  /**
 * Table Management JavaScript
 * Handles add/update table modals and table selection
 */

(function () {
  "use strict";

  // Add Table Modal
  const openBtn = document.getElementById("openAddTable");
  const modal = document.getElementById("addTableModal");
  const cancelBtn = document.getElementById("cancelAddTable");

  if (openBtn && modal) {
    openBtn.addEventListener("click", function () {
      modal.setAttribute("aria-hidden", "false");
    });
  }

  if (cancelBtn && modal) {
    cancelBtn.addEventListener("click", function () {
      modal.setAttribute("aria-hidden", "true");
    });
  }

  if (modal) {
    modal.addEventListener("click", function (e) {
      if (e.target === modal) {
        modal.setAttribute("aria-hidden", "true");
      }
    });
  }

  // Update Table Modal
  const updateModal = document.getElementById("updateTableModal");
  const cancelUpdateBtn = document.getElementById("cancelUpdateTable");

  if (cancelUpdateBtn && updateModal) {
    cancelUpdateBtn.addEventListener("click", function () {
      updateModal.setAttribute("aria-hidden", "true");
    });
  }

  if (updateModal) {
    updateModal.addEventListener("click", function (e) {
      if (e.target === updateModal) {
        updateModal.setAttribute("aria-hidden", "true");
      }
    });
  }

  // Close modal on ESC key
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape") {
      if (modal && modal.getAttribute("aria-hidden") === "false") {
        modal.setAttribute("aria-hidden", "true");
      }
      if (updateModal && updateModal.getAttribute("aria-hidden") === "false") {
        updateModal.setAttribute("aria-hidden", "true");
      }
    }
  });
})();

// Table Selection and Details
document.addEventListener("DOMContentLoaded", function () {
  const tiles = document.querySelectorAll(".tile");
  const updateForm = document.getElementById("updateTableForm");
  const tableActions = document.getElementById("tableActions");

  let currentTableId = null;

  tiles.forEach((tile) => {
    tile.addEventListener("click", function () {
      // Remove selected class from all tiles
      tiles.forEach((t) => t.classList.remove("selected"));

      // Add selected class to clicked tile
      this.classList.add("selected");

      const ds = this.dataset;

      // Update detail panel
      document.getElementById("tableName").textContent = "Bàn " + (ds.id || "");

      // Update status with badge styling
      const statusElement = document.getElementById("detailStatus");
      const status = ds.status || "—";
      statusElement.textContent = status;

      // Remove all status classes
      statusElement.classList.remove(
        "available",
        "occupied",
        "paying",
        "reserved"
      );

      // Add appropriate status class
      if (status !== "—") {
        statusElement.classList.add(status.toLowerCase());
      }

      document.getElementById("detailCapacity").textContent =
        ds.capacity || "0";
      document.getElementById("detailCondition").textContent =
        ds.condition || "—";
      document.getElementById("detailCreatedAt").textContent =
        ds.createdAt || "—";
      document.getElementById("detailUpdatedAt").textContent =
        ds.updatedAt || "—";

      // Store current table info
      currentTableId = ds.id;

      // Set update form action (data-update_url becomes updateUrl in dataset)
      if (updateForm && ds.updateUrl) {
        updateForm.action = ds.updateUrl;
        console.log("Update form action set to:", ds.updateUrl);
      }

      // Populate update form
      const capacityInput = document.getElementById("updateCapacity");
      const conditionSelect = document.getElementById("updateTableCondition");

      if (capacityInput) {
        capacityInput.value = ds.capacity || "1";
      }

      if (conditionSelect) {
        conditionSelect.value = ds.condition || "";
      }

      // Show action buttons
      if (tableActions) {
        tableActions.style.display = "block";
      }
    });
  });

  // Hide actions initially if no table selected
  if (tableActions) {
    tableActions.style.display = "none";
  }

  // Update button handler
  const updateBtn = document.getElementById("updateTableBtn");
  const updateModal = document.getElementById("updateTableModal");

  if (updateBtn && updateModal) {
    updateBtn.addEventListener("click", function (e) {
      e.preventDefault();
      if (currentTableId) {
        updateModal.setAttribute("aria-hidden", "false");
      }
    });
  }
});
