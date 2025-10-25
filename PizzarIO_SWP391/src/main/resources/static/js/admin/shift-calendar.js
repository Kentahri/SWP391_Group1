(function () {
  "use strict";

  // =========================
  // CSRF helpers
  // =========================
  function readCsrfMeta() {
    const tokenEl  = document.querySelector('meta[name="_csrf"]');
    const paramEl  = document.querySelector('meta[name="_csrf_parameter"]');
    const headerEl = document.querySelector('meta[name="_csrf_header"]');
    return {
      token:   tokenEl?.getAttribute("content") ?? null,
      parameter: paramEl?.getAttribute("content") ?? null,
      header:  headerEl?.getAttribute("content") ?? null,
    };
  }
  function ensureFormHasCsrf(form, csrf) {
    if (!csrf.token || !csrf.parameter) return;
    const has = form.querySelector(`input[name="${csrf.parameter}"]`);
    if (!has) {
      const hidden = document.createElement("input");
      hidden.type = "hidden";
      hidden.name = csrf.parameter;
      hidden.value = csrf.token;
      form.appendChild(hidden);
    }
  }
  function injectCsrfIntoAllForms() {
    const csrf = readCsrfMeta();
    if (!csrf.token || !csrf.parameter) return;
    document.querySelectorAll("form").forEach((f) => ensureFormHasCsrf(f, csrf));
  }

  // =========================
  // DOM helpers
  // =========================
  function setModalTitle(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text;
  }
  function show(el) { el?.classList.add("show"); }
  function hide(el) { el?.classList.remove("show"); }

  // =========================
  // Form state
  // =========================
  let formMode = "create"; // "create" | "edit"

  function resetStaffShiftForm(form) {
    form.querySelector('input[name="id"]').value = "";
    const workDateInput = form.querySelector("#workDate");
    const shiftSelect   = form.querySelector("#ssShiftId");
    const staffSelect   = form.querySelector("#ssStaffId");
    const wageInput     = form.querySelector("#hourlyWage");
    const noteInput     = form.querySelector("#ssNote");
    const statusSelect  = form.querySelector("#ssStatus");
    const statusHidden  = form.querySelector('input[name="status"][type="hidden"]');

    if (workDateInput) workDateInput.value = "";
    if (shiftSelect)   shiftSelect.selectedIndex = 0;
    if (staffSelect)   staffSelect.selectedIndex = 0;
    if (wageInput)     wageInput.value = "";
    if (noteInput)     noteInput.value = "";
    if (statusSelect)  statusSelect.value = "SCHEDULED";
    if (statusHidden)  statusHidden.value = "SCHEDULED";

    document.querySelectorAll(".error-message").forEach(n => n.textContent = "");
  }

  // =========================
  // Salary Auto-update
  // =========================
  window.updateSalaryFromShift = function () {
    const shiftSelect = document.getElementById("ssShiftId");
    const salaryInput = document.getElementById("hourlyWage");
    if (!shiftSelect || !salaryInput) return;
    const opt = shiftSelect.options[shiftSelect.selectedIndex];
    const salary = opt?.getAttribute("data-salary");
    salaryInput.value = salary ? salary : "";
  };

  // =========================
  // Modal open/close
  // =========================
  window.openStaffShiftModal = function () {
    const modal = document.getElementById("staffShiftModal");
    const form  = document.getElementById("staffShiftForm");
    if (!modal || !form) return;

    formMode = "create";
    resetStaffShiftForm(form);
    form.querySelector("#workDate")?.removeAttribute("readonly");

    setModalTitle("staffShiftModalTitle", "Thêm ca làm việc");
    injectCsrfIntoAllForms();
    show(modal);
  };

  window.closeStaffShiftModal = function () {
    const modal = document.getElementById("staffShiftModal");
    const form  = document.getElementById("staffShiftForm");
    if (!modal || !form) return;

    hide(modal);
    resetStaffShiftForm(form);
    formMode = "create";
  };

  // =========================
  // EDIT: fetch DTO & fill form
  // =========================
  window.editShift = function (shiftAssignId) {
    const modal = document.getElementById("staffShiftModal");
    const form  = document.getElementById("staffShiftForm");
    if (!modal || !form) return;

    fetch(`/pizzario/manager/staff_shifts/${shiftAssignId}`, {
      headers: { "Accept": "application/json" },
      credentials: "same-origin",
    })
        .then(r => {
          if (!r.ok) throw new Error("Không tải được dữ liệu ca làm.");
          return r.json();
        })
        .then(dto => {
          formMode = "edit";
          resetStaffShiftForm(form);

          form.querySelector('input[name="id"]').value = dto.id ?? "";

          const workDateInput = form.querySelector("#workDate");
          if (workDateInput) {
            workDateInput.value = dto.workDate ?? "";
            workDateInput.setAttribute("readonly", "readonly");
          }

          const shiftSelect = form.querySelector("#ssShiftId");
          if (shiftSelect && dto.shiftId != null) {
            shiftSelect.value = String(dto.shiftId);
          }

          const staffSelect = form.querySelector("#ssStaffId");
          if (staffSelect && dto.staffId != null) {
            staffSelect.value = String(dto.staffId);
          }

          const wageInput = form.querySelector("#hourlyWage");
          if (wageInput) {
            if (dto.hourlyWage != null) wageInput.value = dto.hourlyWage;
            else window.updateSalaryFromShift();
          }

          const noteInput = form.querySelector("#ssNote");
          if (noteInput) noteInput.value = dto.note ?? "";

          const statusSelect = form.querySelector("#ssStatus");
          const statusHidden = form.querySelector('input[name="status"][type="hidden"]');
          const st = dto.status ?? "SCHEDULED";
          if (statusSelect) statusSelect.value = st;
          if (statusHidden) statusHidden.value = st;

          setModalTitle("staffShiftModalTitle", "Sửa ca làm việc");
          injectCsrfIntoAllForms();
          show(modal);
        })
        .catch(err => {
          alert(err.message || "Lỗi khi tải dữ liệu ca làm.");
        });
  };

  // =========================
  // Filters & Navigation
  // =========================
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

  document.addEventListener("DOMContentLoaded", function () {
    injectCsrfIntoAllForms();
  });
})();
