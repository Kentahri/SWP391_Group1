/**
 * ============================================
 * ANALYTICS REAL-TIME STATS AUTO-REFRESH
 * ============================================
 * Tự động cập nhật số liệu hôm nay mỗi 30 giây
 * Không reload trang, chỉ update số liệu
 */

(function() {
  'use strict';

  // ========== CONFIGURATION ==========
  const REFRESH_INTERVAL = 30000; // 30 seconds
  const API_ENDPOINT = window.location.pathname.replace(/\/analytics.*/, '') + '/analytics/api/today-stats';

  // ========== DOM ELEMENTS ==========
  const todayOrdersEl = document.getElementById('todayOrders');
  const todayRevenueEl = document.getElementById('todayRevenue');

  // ========== FORMATTER ==========
  const numberFormatter = new Intl.NumberFormat('vi-VN', {
    style: 'decimal',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  });

  /**
   * ========== FUNCTION 1: FETCH DATA ==========
   * Gọi API để lấy dữ liệu mới nhất
   *
   * Flow:
   * 1. Fetch API endpoint
   * 2. Parse JSON response
   * 3. Kiểm tra success
   * 4. Gọi updateUI() để cập nhật
   */
  async function fetchTodayStats() {
    try {
      console.log('[Analytics Real-time] Fetching today stats from:', API_ENDPOINT);

      // BƯỚC 1: Gọi API
      const response = await fetch(API_ENDPOINT, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      });

      // BƯỚC 2: Kiểm tra HTTP status
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      // BƯỚC 3: Parse JSON
      const result = await response.json();

      console.log('[Analytics Real-time] Received:', result);

      // BƯỚC 4: Kiểm tra success và update UI
      if (result.success && result.data) {
        updateUI(result.data);
      } else {
        console.error('[Analytics Real-time] API returned error:', result.message);
      }

    } catch (error) {
      console.error('[Analytics Real-time] Fetch error:', error);
      // Không hiển thị lỗi cho user, chỉ log để debug
    }
  }

  /**
   * ========== FUNCTION 2: UPDATE UI ==========
   * Cập nhật giao diện với dữ liệu mới
   * Có animation effect khi giá trị thay đổi
   *
   * @param {Object} data - {todayOrders, todayRevenue}
   */
  function updateUI(data) {
    // ===== CẬP NHẬT SỐ ĐƠN HÀNG =====
    if (todayOrdersEl && data.todayOrders !== undefined) {
      const oldValue = parseInt(todayOrdersEl.textContent) || 0;
      const newValue = data.todayOrders;

      // Cập nhật text
      todayOrdersEl.textContent = newValue;

      // Animation nếu giá trị tăng
      if (newValue > oldValue) {
        animateChange(todayOrdersEl);
        console.log(`[Analytics Real-time] Orders updated: ${oldValue} → ${newValue}`);
      }
    }

    // ===== CẬP NHẬT DOANH THU =====
    if (todayRevenueEl && data.todayRevenue !== undefined) {
      const oldValue = parseFloat(todayRevenueEl.dataset.value) || 0;
      const newValue = data.todayRevenue;

      // Cập nhật text (format số)
      todayRevenueEl.textContent = numberFormatter.format(newValue) + ' đ';

      // Cập nhật data-value để lần sau so sánh
      todayRevenueEl.dataset.value = newValue;

      // Animation nếu giá trị tăng
      if (newValue > oldValue) {
        animateChange(todayRevenueEl);
        console.log(`[Analytics Real-time] Revenue updated: ${numberFormatter.format(oldValue)} → ${numberFormatter.format(newValue)}`);
      }
    }
  }

  /**
   * ========== FUNCTION 3: ANIMATION ==========
   * Tạo hiệu ứng khi giá trị thay đổi
   * Scale up + đổi màu xanh, sau đó trở lại bình thường
   *
   * @param {HTMLElement} element - Element cần animate
   */
  function animateChange(element) {
    // BƯỚC 1: Scale up + màu xanh
    element.style.transition = 'all 0.3s ease';
    element.style.transform = 'scale(1.1)';
    element.style.color = '#10b981'; // Màu xanh lá

    // BƯỚC 2: Sau 300ms, trở lại bình thường
    setTimeout(() => {
      element.style.transform = 'scale(1)';
      element.style.color = ''; // Quay lại màu mặc định
    }, 300);
  }

  /**
   * ========== FUNCTION 4: INITIALIZE ==========
   * Khởi chạy khi page load
   *
   * Flow:
   * 1. Fetch ngay lập tức
   * 2. Set interval để fetch định kỳ
   * 3. Log thông tin
   */
  function init() {
    // Kiểm tra xem có elements không
    if (!todayOrdersEl || !todayRevenueEl) {
      console.warn('[Analytics Real-time] Required elements not found. Auto-refresh disabled.');
      return;
    }

    console.log('[Analytics Real-time] Initializing...');

    // BƯỚC 1: Fetch ngay khi load trang
    fetchTodayStats();

    // BƯỚC 2: Setup auto-refresh (mỗi 30 giây)
    setInterval(fetchTodayStats, REFRESH_INTERVAL);

    console.log(`[Analytics Real-time] Auto-refresh enabled (every ${REFRESH_INTERVAL/1000}s)`);
  }

  // ========== START ==========
  // Chờ DOM ready rồi mới chạy
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

})();
