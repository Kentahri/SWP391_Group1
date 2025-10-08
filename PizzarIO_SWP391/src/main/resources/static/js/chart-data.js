const labels = (window.CHART_DATA && Array.isArray(window.CHART_DATA.labels)) ? window.CHART_DATA.labels : [];
const values = (window.CHART_DATA && Array.isArray(window.CHART_DATA.data)) ? window.CHART_DATA.data : [];


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
