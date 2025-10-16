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

  if (openBtn) {
    openBtn.addEventListener("click", function () {
      modal.style.display = "flex";
    });
  }

  if (cancelBtn) {
    cancelBtn.addEventListener("click", function () {
      modal.style.display = "none";
    });
  }

  modal &&
    modal.addEventListener("click", function (e) {
      if (e.target === modal) {
        modal.style.display = "none";
      }
    });

  // Update Table Modal
  const updateModal = document.getElementById("updateTableModal");
  const cancelUpdateBtn = document.getElementById("cancelUpdateTable");

  if (cancelUpdateBtn) {
    cancelUpdateBtn.addEventListener("click", function () {
      updateModal.style.display = "none";
    });
  }

  updateModal &&
    updateModal.addEventListener("click", function (e) {
      if (e.target === updateModal) {
        updateModal.style.display = "none";
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
      tiles.forEach((t) => t.classList.remove("selected"));
      this.classList.add("selected");

      const ds = this.dataset;

      // Update detail panel
      document.getElementById("tableName").textContent = "Bàn " + (ds.id || "");
      document.getElementById("detailStatus").textContent = ds.status || "—";
      document.getElementById("detailCapacity").textContent =
        ds.capacity || "0";
      document.getElementById("detailCondition").textContent =
        ds.condition || "—";
      document.getElementById("detailCreatedAt").textContent =
        ds.createdAt || "—";
      document.getElementById("detailUpdatedAt").textContent =
        ds.updatedAt || "—";

      console.log("Selected table:", ds.id);
      console.log("Update URL:", ds.update_url);

      currentTableId = ds.id;
      updateForm.action = ds.update_url;

      document.getElementById("updateCapacity").value = ds.capacity || "1";
      document.getElementById("updateTableCondition").value =
        ds.condition || "";

      tableActions.style.display = "block";
    });
  });

  // Hide actions initially if no table selected
  if (tableActions) {
    tableActions.style.display = "none";
  }

  // Update button handler
  const updateBtn = document.getElementById("updateTableBtn");
  if (updateBtn) {
    updateBtn.addEventListener("click", function (e) {
      e.preventDefault();
      if (currentTableId) {
        document.getElementById("updateTableModal").style.display = "flex";
      }
    });
  }
});
