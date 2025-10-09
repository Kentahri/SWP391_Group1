const labels =
  window.CHART_DATA && Array.isArray(window.CHART_DATA.labels_bar)
    ? window.CHART_DATA.labels_bar
    : [];
const values =
  window.CHART_DATA && Array.isArray(window.CHART_DATA.data_bar)
    ? window.CHART_DATA.data_bar
    : [];

const data = {
  labels: labels,
  datasets: [
    {
      label: "Weekly Sales",
      data: values,
      borderWidth: 1,
    },
  ],
};

// config
const bar = {
  type: "bar",
  data,
  options: {
    scales: {
      y: {
        beginAtZero: true,
      },
    },
  },
};
const myChartBar = new Chart(document.getElementById("myChartBar"), bar);

const labelsLine =
  window.CHART_DATA && Array.isArray(window.CHART_DATA.labels_Line)
    ? window.CHART_DATA.labels_Line
    : [];
const valuesLine =
  window.CHART_DATA && Array.isArray(window.CHART_DATA.data_Line)
    ? window.CHART_DATA.data_Line
    : [];

// Tạo formatter cho số (tiền tệ hoặc số thông thường)
const fmt = new Intl.NumberFormat("vi-VN", {
  style: "decimal",
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
});

// Tạo gradient cho line chart
const ctx = document.getElementById("myChartLine").getContext("2d");
const gradient = ctx.createLinearGradient(0, 0, 0, 400);
gradient.addColorStop(0, "rgba(14, 165, 233, 0.4)"); // Xanh đậm ở trên
gradient.addColorStop(0.5, "rgba(14, 165, 233, 0.2)"); // Xanh nhạt ở giữa
gradient.addColorStop(1, "rgba(14, 165, 233, 0)"); // Trong suốt ở dưới

// Tạo label với ngày đầu và ngày cuối
const startDate = labelsLine.length > 0 ? labelsLine[0] : "";
const endDate = labelsLine.length > 0 ? labelsLine[labelsLine.length - 2] : "";
const revenueLabel =
 `Doanh thu (${startDate} - ${endDate})`;

const dataLine = {
  labels: labelsLine,
  datasets: [
    {
      label: revenueLabel,
      data: valuesLine,
      tension: 0.35, // đường mượt
      borderWidth: 2,
      borderColor: "#0ea5e9",
      backgroundColor: gradient,
      fill: true, // tô nền nhẹ với gradient
      pointRadius: 2,
      pointHoverRadius: 5,
      pointHitRadius: 10,
      pointBackgroundColor: "#0ea5e9",
      pointBorderWidth: 0,
    },
  ],
};

// cấu hình cho nền trắng, tooltip đẹp, lưới mảnh
const line = {
  type: "line",
  data: dataLine,
  options: {
    responsive: true,
    maintainAspectRatio: false,
    layout: { padding: { top: 8, right: 12, bottom: 8, left: 6 } },
    interaction: { mode: "index", intersect: false },
    animation: { duration: 600, easing: "easeOutQuart" },
    plugins: {
      legend: {
        display: true,
        labels: {
          color: "#0f172a", // chữ đậm trên nền trắng
          boxWidth: 10,
          boxHeight: 10,
          usePointStyle: true,
          pointStyle: "line",
        },
      },
      tooltip: {
        backgroundColor: "rgba(15,23,42,0.92)",
        titleColor: "#fff",
        bodyColor: "#e2e8f0",
        borderColor: "rgba(255,255,255,0.08)",
        borderWidth: 1,
        padding: 10,
        displayColors: false,
        callbacks: {
          title: (items) => items[0].label,
          label: (ctx) => `${fmt.format(ctx.parsed.y ?? 0)}`, // format số
        },
      },
    },
    scales: {
      x: {
        grid: { display: false },
        ticks: { color: "#475569", maxRotation: 0, autoSkip: true },
      },
      y: {
        beginAtZero: true,
        grid: {
          color: "rgba(2,6,23,0.06)", // lưới rất nhạt
          drawBorder: false,
          borderDash: [4, 4],
        },
        ticks: {
          color: "#475569",
          callback: (v) => fmt.format(v),
        },
      },
    },
  },
};

// Tạo chart
const myChartLine = new Chart(document.getElementById("myChartLine"), line);
