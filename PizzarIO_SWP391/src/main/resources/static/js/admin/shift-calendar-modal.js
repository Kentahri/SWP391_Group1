function exportMonthlySalary() {
  document.getElementById("exportModal").style.display = "flex";
}

function closeExportModal() {
  document.getElementById("exportModal").style.display = "none";
}

function confirmExport() {
  const year = document.getElementById("exportYear").value;
  const month = document.getElementById("exportMonth").value;

  // Tạo form để submit
  const form = document.createElement("form");
  form.method = "POST";
  form.action = "/pizzario/manager/staff_shifts/export";

  const yearInput = document.createElement("input");
  yearInput.type = "hidden";
  yearInput.name = "year";
  yearInput.value = year;

  const monthInput = document.createElement("input");
  monthInput.type = "hidden";
  monthInput.name = "month";
  monthInput.value = month;

  form.appendChild(yearInput);
  form.appendChild(monthInput);

  // Thêm CSRF token
  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfInput = document.createElement("input");
  csrfInput.type = "hidden";
  csrfInput.name = document.querySelector(
    'meta[name="_csrf_parameter"]'
  ).content;
  csrfInput.value = csrfToken;
  form.appendChild(csrfInput);

  document.body.appendChild(form);
  form.submit();
  document.body.removeChild(form);

  closeExportModal();
}

// Complete Shift Modal Functions
let currentShiftId = null;
let currentShiftEndTime = null;

// Wrapper function to read data from button attributes
function openCompleteShiftModalFromButton(button) {
  const shiftId = button.getAttribute("data-shift-id");
  const staffName = button.getAttribute("data-staff-name");
  const shiftName = button.getAttribute("data-shift-name");
  const endTime = button.getAttribute("data-end-time");
  const workDate = button.getAttribute("data-work-date");

  openCompleteShiftModal(
    shiftId,
    staffName,
    shiftName,
    endTime,
    workDate
  );
}

function openCompleteShiftModal(
  shiftId,
  staffName,
  shiftName,
  endTime,
  workDate
) {
  currentShiftId = shiftId;
  currentShiftEndTime = endTime;

  // Set shift info
  document.getElementById("completeStaffName").textContent = staffName;
  document.getElementById("completeShiftName").textContent = shiftName;
  document.getElementById("completeWorkDate").textContent = workDate;

  // Set default checkout time to shift end time
  // Format: workDate + endTime -> datetime-local format (YYYY-MM-DDTHH:MM)
  const defaultCheckoutTime = workDate + "T" + endTime;
  document.getElementById("completeCheckoutTime").value =
    defaultCheckoutTime;

  // Reset form
  document.getElementById("completePenalty").value = 0;
  document.getElementById("completeNote").value = "";

  // Update status preview
  updateStatusPreview();

  // Add event listener for checkout time change
  document
    .getElementById("completeCheckoutTime")
    .addEventListener("input", updateStatusPreview);

  // Show modal
  document.getElementById("completeShiftModal").style.display = "flex";
}

function closeCompleteShiftModal() {
  document.getElementById("completeShiftModal").style.display = "none";
  currentShiftId = null;
  currentShiftEndTime = null;
}

function updateStatusPreview() {
  const checkoutTimeInput = document.getElementById(
    "completeCheckoutTime"
  ).value;
  const statusPreview = document.getElementById("statusPreview");

  if (!checkoutTimeInput || !currentShiftEndTime) {
    statusPreview.textContent = "COMPLETED";
    statusPreview.style.background = "#28a745";
    statusPreview.style.color = "white";
    return;
  }

  // Extract time from datetime-local input (format: YYYY-MM-DDTHH:MM)
  const checkoutTime = checkoutTimeInput.split("T")[1]; // Get HH:MM

  // Compare times
  if (checkoutTime < currentShiftEndTime) {
    statusPreview.textContent = "LEFT_EARLY (Về sớm)";
    statusPreview.style.background = "#ffc107";
    statusPreview.style.color = "#000";
  } else {
    statusPreview.textContent = "COMPLETED (Hoàn thành)";
    statusPreview.style.background = "#28a745";
    statusPreview.style.color = "white";
  }
}

function submitCompleteShift() {
  const form = document.getElementById("completeShiftForm");

  // Validate required fields
  const checkoutTime = document.getElementById(
    "completeCheckoutTime"
  ).value;
  const note = document.getElementById("completeNote").value;
  const penalty = document.getElementById("completePenalty").value;

  // Validation messages with better UX
  if (!checkoutTime) {
    showValidationError(
      "completeCheckoutTime",
      "Vui lòng chọn thời gian checkout"
    );
    return;
  }

  // Validate checkout time is not in the future
  const checkoutDateTime = new Date(checkoutTime);
  const now = new Date();
  if (checkoutDateTime > now) {
    showValidationError(
      "completeCheckoutTime",
      "Thời gian checkout không thể ở tương lai"
    );
    return;
  }

  if (!note.trim()) {
    showValidationError(
      "completeNote",
      "Vui lòng nhập lý do hoàn thành ca"
    );
    return;
  }

  if (note.trim().length < 10) {
    showValidationError(
      "completeNote",
      "Lý do phải có ít nhất 10 ký tự để đảm bảo rõ ràng"
    );
    return;
  }

  if (penalty === "" || penalty < 0 || penalty > 100) {
    showValidationError("completePenalty", "Mức phạt phải từ 0-100%");
    return;
  }

  // Show confirmation dialog with details
  const statusPreviewText =
    document.getElementById("statusPreview").textContent;
  const confirmMessage =
    `Bạn có chắc chắn muốn hoàn thành ca này?\n\n` +
    `Trạng thái: ${statusPreviewText}\n` +
    `Thời gian checkout: ${formatDateTime(checkoutTime)}\n` +
    `Mức phạt: ${penalty}%\n\n` +
    `Lưu ý: Thao tác này không thể hoàn tác!`;

  if (!confirm(confirmMessage)) {
    return;
  }

  // Update form action with current shift ID
  form.action =
    "/pizzario/manager/staff_shifts/manual-complete/" + currentShiftId;

  // Disable submit button to prevent double submission
  const submitBtn = event.target;
  submitBtn.disabled = true;
  submitBtn.textContent = "Đang xử lý...";
  submitBtn.style.opacity = "0.6";

  // Submit form
  form.submit();
}

// Helper function to show validation errors
function showValidationError(fieldId, message) {
  const field = document.getElementById(fieldId);

  // Highlight field with error
  field.style.borderColor = "#f44336";
  field.style.boxShadow = "0 0 0 3px rgba(244, 67, 54, 0.1)";

  // Show alert
  alert("❌ " + message);

  // Focus on field
  field.focus();

  // Reset border after 3 seconds
  setTimeout(() => {
    field.style.borderColor = "#ddd";
    field.style.boxShadow = "none";
  }, 3000);
}

// Helper function to format datetime for display
function formatDateTime(datetimeString) {
  const date = new Date(datetimeString);
  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const year = date.getFullYear();
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${day}/${month}/${year} ${hours}:${minutes}`;
}

// Close modal when clicking outside
window.onclick = function (event) {
  const completeModal = document.getElementById("completeShiftModal");
  const exportModal = document.getElementById("exportModal");

  if (event.target == completeModal) {
    closeCompleteShiftModal();
  }
  if (event.target == exportModal) {
    closeExportModal();
  }
};
