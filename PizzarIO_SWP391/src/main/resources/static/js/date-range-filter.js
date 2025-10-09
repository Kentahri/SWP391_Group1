// Xử lý dropdown chọn thời gian
document.addEventListener("DOMContentLoaded", function () {
  const timeRangeSelect = document.getElementById("timeRangeSelect");
  const customDateRange = document.getElementById("customDateRange");
  const startDate = document.getElementById("startDate");
  const endDate = document.getElementById("endDate");
  const applyBtn = document.getElementById("applyCustomDate");

  // Set ngày mặc định cho date pickers
  const today = new Date();
  endDate.value = today.toISOString().split("T")[0];
  const defaultStart = new Date(today);
  defaultStart.setDate(today.getDate() - 28);
  startDate.value = defaultStart.toISOString().split("T")[0];

  // Xử lý khi chọn dropdown
  timeRangeSelect.addEventListener("change", function () {
    console.log("Dropdown changed to:", this.value);
    if (this.value === "custom") {
      customDateRange.style.display = "block";
    } else {
      customDateRange.style.display = "none";

      // Tải lại dữ liệu theo khoảng thời gian đã chọn
      const days = parseInt(this.value);
      console.log("Loading data for days:", days);
      loadDataForRange(days);
    }
  });

  // Xử lý khi nhấn nút "Áp dụng" cho tùy chỉnh
  applyBtn.addEventListener("click", function () {
    const start = startDate.value;
    const end = endDate.value;

    if (!start || !end) {
      alert("Vui lòng chọn cả ngày bắt đầu và ngày kết thúc");
      return;
    }

    if (new Date(start) > new Date(end)) {
      alert("Ngày bắt đầu phải nhỏ hơn ngày kết thúc");
      return;
    }

    customDateRange.style.display = "none";

    // Tải lại dữ liệu với khoảng tùy chỉnh
    loadDataForCustomRange(start, end);
  });

  // Đóng date picker khi click bên ngoài
  document.addEventListener("click", function (event) {
    if (
      !event.target.closest("#timeRangeSelect") &&
      !event.target.closest("#customDateRange")
    ) {
      customDateRange.style.display = "none";
    }
  });
});

// Hàm tải dữ liệu cho khoảng thời gian cố định (7 hoặc 28 ngày)
function loadDataForRange(days) {
  console.log(`Đang tải dữ liệu cho ${days} ngày qua...`);
  submitForm({ days: days });
}

// Hàm tải dữ liệu cho khoảng thời gian tùy chỉnh
function loadDataForCustomRange(startDate, endDate) {
  console.log(`Đang tải dữ liệu từ ${startDate} đến ${endDate}...`);
  submitForm({ startDate: startDate, endDate: endDate });
}

// Hàm helper để submit form với POST
function submitForm(params) {
  const form = document.createElement("form");
  form.method = "POST";
  form.action = "/pizzario/manager/analytics";

  for (const [key, value] of Object.entries(params)) {
    const input = document.createElement("input");
    input.type = "hidden";
    input.name = key;
    input.value = value;
    form.appendChild(input);
  }

  document.body.appendChild(form);
  form.submit();
}
