(function () {
  "use strict";

  // =========================
  // CSRF helpers
  // =========================
  function readCsrfMeta() {
    const tokenEl = document.querySelector('meta[name="_csrf"]');
    const paramEl = document.querySelector('meta[name="_csrf_parameter"]');
    const headerEl = document.querySelector('meta[name="_csrf_header"]');
    return {
      token: tokenEl?.getAttribute("content") ?? null,
      parameter: paramEl?.getAttribute("content") ?? null,
      header: headerEl?.getAttribute("content") ?? null,
    };
  }

  /** Thêm input _csrf vào 1 form nếu chưa có */
  function ensureFormHasCsrf(form, csrf) {
    if (!csrf.token || !csrf.parameter) return;
    const hasAlready = form.querySelector(`input[name="${csrf.parameter}"]`);
    if (!hasAlready) {
      const hidden = document.createElement("input");
      hidden.type = "hidden";
      hidden.name = csrf.parameter;
      hidden.value = csrf.token;
      form.appendChild(hidden);
    }
  }

  /** Tiêm CSRF cho tất cả form (1 lần duy nhất) */
  function injectCsrfIntoAllForms() {
    const csrf = readCsrfMeta();
    if (!csrf.token || !csrf.parameter) return;
    document.querySelectorAll("form").forEach((f) => ensureFormHasCsrf(f, csrf));
  }

  // =========================
  // Common DOM utils
  // =========================
  function onEscClose(modalEl, closeFn) {
    document.addEventListener("keydown", (e) => {
      if (e.key === "Escape" && modalEl.classList.contains("show")) closeFn();
    });
  }

  function enableBackdropClose(modalEl, closeFn) {
    modalEl.addEventListener("click", (e) => {
      if (e.target === modalEl) closeFn();
    });
  }

  function setModalTitle(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text;
  }

  // =========================
  // STAFF SHIFT FORM helpers
  // =========================
  /** Chỉ reset field dữ liệu business; KHÔNG xoá _csrf */
  function resetStaffShiftForm(form, { clearId = true } = {}) {
    // Reset các input user-facing
    const workDateInput = form.querySelector("#workDate");
    const shiftSelect = form.querySelector("#ssShiftId");
    const staffSelect = form.querySelector("#ssStaffId");
    const wageInput = form.querySelector("#hourlyWage");
    const statusSelect = form.querySelector("#ssStatus");

    if (workDateInput) workDateInput.value = "";
    if (shiftSelect) shiftSelect.selectedIndex = 0;
    if (staffSelect) staffSelect.selectedIndex = 0;
    if (wageInput) wageInput.value = "";
    if (statusSelect) statusSelect.selectedIndex = 0;

    // Dọn lỗi validation cũ (nếu bạn render lỗi client)
    form
        .querySelectorAll(".field-error, .error, [data-error]")
        .forEach((n) => (n.textContent = ""));

    // Chỉ làm sạch id; KHÔNG đụng tới _csrf
    if (clearId) {
      const idInput = form.querySelector('input[name="id"]');
      if (idInput) idInput.value = "";
    }
  }

  // =========================
  // Shift Type Modal
  // =========================
  function setupShiftTypeModal() {
    const modal = document.getElementById("shiftTypeModal");
    const form = document.getElementById("shiftTypeForm");
    if (!modal || !form) return;

    // CSRF cho form này
    injectCsrfIntoAllForms();

    if (window.openShiftModalFlag) {
      modal.classList.add("show");
      setModalTitle(
          "shiftModalTitle",
          window.openShiftModalFlag === "create" ? "Thêm loại ca mới" : "Sửa loại ca"
      );
      switchTab("shifts");
    }

    enableBackdropClose(modal, closeShiftModal);
    onEscClose(modal, closeShiftModal);
  }

  window.openShiftModal = function () {
    window.location.href =
        "/pizzario/manager/shift/create?modal=true&returnPage=staff_shifts";
  };

  window.closeShiftModal = function () {
    const modal = document.getElementById("shiftTypeModal");
    if (modal) {
      modal.classList.remove("show");
      window.location.href = "/pizzario/manager/staff_shifts";
    }
  };

  window.editShiftType = function (shiftId) {
    window.location.href = "/pizzario/manager/shift/edit/" + shiftId;
  };

  // =========================
  // Staff Shift Modal
  // =========================
  function setupStaffShiftModal() {
    const modal = document.getElementById("staffShiftModal");
    const form = document.getElementById("staffShiftForm");
    if (!modal || !form) return;

    injectCsrfIntoAllForms();

    if (window.openStaffShiftModalFlag) {
      modal.classList.add("show");
      setModalTitle(
          "staffShiftModalTitle",
          window.openStaffShiftModalFlag === "create" ? "Thêm ca làm việc" : "Sửa ca làm việc"
      );
    }

    enableBackdropClose(modal, closeStaffShiftModal);
    onEscClose(modal, closeStaffShiftModal);
  }

  /** Mở modal ở chế độ CREATE */
  window.openStaffShiftModal = function () {
    const modal = document.getElementById("staffShiftModal");
    const form = document.getElementById("staffShiftForm");
    if (!modal || !form) return;

    // Giữ _csrf, chỉ reset dữ liệu business + id
    resetStaffShiftForm(form, { clearId: true });
    setModalTitle("staffShiftModalTitle", "Thêm ca làm việc");
    injectCsrfIntoAllForms(); // đảm bảo vẫn còn _csrf nếu form vừa được render lại
    modal.classList.add("show");
  };

  /** Đóng modal; không phá _csrf */
  window.closeStaffShiftModal = function () {
    const modal = document.getElementById("staffShiftModal");
    const form = document.getElementById("staffShiftForm");
    if (!modal) return;

    modal.classList.remove("show");
    if (form) {
      resetStaffShiftForm(form, { clearId: true });
    }
  };

  // =========================
  // Tabs & Filters & Navigation
  // =========================
  window.switchTab = function (tabName) {
    document.querySelectorAll(".tab-btn").forEach((btn) => btn.classList.remove("active"));
    document.querySelectorAll(".tab-content").forEach((c) => c.classList.remove("active"));

    const activeBtn = document.querySelector(`.tab-btn[onclick*="${tabName}"]`);
    const activeContent = document.getElementById(`tab-${tabName}`);
    activeBtn?.classList.add("active");
    activeContent?.classList.add("active");
  };

  window.navigateWeek = function (offset) {
    const url = new URL(window.location);
    const currentOffset = parseInt(url.searchParams.get("weekOffset")) || 0;
    url.searchParams.set("weekOffset", currentOffset + offset);
    window.location.href = url.toString();
  };

  window.filterByStaff = function () {
    const val = document.getElementById("staffFilter")?.value;
    const url = new URL(window.location);
    if (val) url.searchParams.set("staffId", val);
    else url.searchParams.delete("staffId");
    window.location.href = url.toString();
  };

  window.filterByShift = function () {
    const val = document.getElementById("shiftFilter")?.value;
    const url = new URL(window.location);
    if (val) url.searchParams.set("shiftId", val);
    else url.searchParams.delete("shiftId");
    window.location.href = url.toString();
  };

  window.editShift = function (shiftId) {
    window.location.href = "/pizzario/manager/staff_shifts/edit/" + shiftId + "?modal=true";
  };

  // =========================
  // Boot
  // =========================
  document.addEventListener("DOMContentLoaded", function () {
    injectCsrfIntoAllForms();
    setupShiftTypeModal();
    setupStaffShiftModal();
  });
})();
