// Payment Page JavaScript
document.addEventListener("DOMContentLoaded", function () {
  // Wait for window.paymentData to be initialized
  if (!window.paymentData) {
    console.error("window.paymentData is not initialized! Waiting...");
    // Wait a bit and try again
    setTimeout(function () {
      if (!window.paymentData) {
        console.error("window.paymentData still not initialized after wait!");
        // Initialize with defaults
        window.paymentData = {
          sessionId: null,
          orderTotal: 0,
          originalTotal: 0,
          discountAmount: 0,
          finalTotal: 0,
          membershipPoints: 0,
          appliedVoucherId: null,
          availableVouchers: [],
          paymentConfirmUrl: null,
        };
      }
      initializePayment();
    }, 100);
    return;
  }

  initializePayment();
});

function initializePayment() {
  const sessionId = window.paymentData?.sessionId;
  const orderTotal = window.paymentData?.orderTotal || 0;
  const originalTotal = window.paymentData?.originalTotal || 0;
  const discountAmount = window.paymentData?.discountAmount || 0;
  const finalTotal = window.paymentData?.finalTotal || 0;
  const membershipPoints = window.paymentData?.membershipPoints || 0;
  const appliedVoucherId = window.paymentData?.appliedVoucherId;
  const availableVouchers = window.paymentData?.availableVouchers || [];

  // DOM Elements
  const subtotalElement = document.getElementById("subtotalTop");
  const taxAmountElement = document.getElementById("taxAmountTop");
  const totalAmountElement = document.getElementById("totalAmountTop");
  const discountAmountElement = document.getElementById("discountAmountTop");
  const confirmPaymentBtn = document.getElementById("confirmPaymentBtn");

  // Check if confirmPaymentBtn exists
  if (!confirmPaymentBtn) {
    console.error("confirmPaymentBtn element not found!");
  }

  const loadingModalElement = document.getElementById("loadingModal");
  const loadingModal = loadingModalElement
    ? new bootstrap.Modal(loadingModalElement)
    : null;

  // State
  let currentTotal = finalTotal;
  let appliedVoucher = null;
  let selectedPaymentMethod = null;

  // Initialize page
  init();

  function init() {
    // Debug: Check sessionId
    console.log("Session ID from window.paymentData:", sessionId);
    console.log("Session ID type:", typeof sessionId);
    console.log("Session ID is null:", sessionId === null);
    console.log("Session ID is undefined:", sessionId === undefined);
    // Không gọi calculateTotals() ngay lập tức để tránh ghi đè giá trị từ backend
    // Các giá trị từ backend sẽ được giữ nguyên
    setupEventListeners();

    // Check for error or success message from server
    const urlParams = new URLSearchParams(window.location.search);
    const errorMessage = urlParams.get("error");
    const successMessage = urlParams.get("success");

    if (errorMessage) {
      showAlert(decodeURIComponent(errorMessage), "danger");
    }

    if (successMessage) {
      showAlert(decodeURIComponent(successMessage), "success");
    }

    // Remove parameters from URL after showing messages
    if (errorMessage || successMessage) {
      const newUrl = window.location.pathname;
      window.history.replaceState({}, document.title, newUrl);
    }

    // Set default payment method if none selected
    // First check if any radio is already checked
    const checkedRadio = document.querySelector(
      'input[name="paymentMethod"]:checked'
    );
    if (checkedRadio) {
      selectedPaymentMethod = checkedRadio.value;
      console.log("Found checked payment method:", selectedPaymentMethod);
      updatePaymentButton();
    } else {
      // If no radio is checked, set default to CASH
      const defaultRadio = document.querySelector(
        'input[name="paymentMethod"][value="CASH"]'
      );
      if (defaultRadio) {
        defaultRadio.checked = true;
        selectedPaymentMethod = "CASH";
        console.log("Set default payment method to CASH");
        updatePaymentButton();
      } else {
        // If CASH not found, select the first available payment method
        const firstRadio = document.querySelector(
          'input[name="paymentMethod"]'
        );
        if (firstRadio) {
          firstRadio.checked = true;
          selectedPaymentMethod = firstRadio.value;
          console.log("Force selected payment method:", selectedPaymentMethod);
          updatePaymentButton();
        } else {
          console.error("No payment method radios found!");
        }
      }
    }
  }

  function setupEventListeners() {
    // Payment method selection
    const paymentMethodRadios = document.querySelectorAll(
      'input[name="paymentMethod"]'
    );
    console.log("Found payment method radios:", paymentMethodRadios.length);

    if (paymentMethodRadios.length === 0) {
      console.warn("No payment method radios found!");
    }

    paymentMethodRadios.forEach((radio, index) => {
      console.log(`Radio ${index}:`, radio.value, "checked:", radio.checked);
      if (radio.checked) {
        selectedPaymentMethod = radio.value;
        console.log("Pre-selected payment method:", selectedPaymentMethod);
        // Update button immediately if a radio is already checked
        updatePaymentButton();
      }
      radio.addEventListener("change", function () {
        selectedPaymentMethod = this.value;
        console.log("Payment method selected:", selectedPaymentMethod);
        updatePaymentButton();
      });
    });

    // Confirm payment button
    if (confirmPaymentBtn) {
      confirmPaymentBtn.addEventListener("click", function () {
        confirmPayment();
      });
    } else {
      console.error("confirmPaymentBtn not found, cannot attach click handler");
    }
  }

  // Voucher handling is now done via form submission and page reload
  // No need for AJAX functions anymore

  function calculateTotals() {
    // Sử dụng dữ liệu từ window.paymentData để đảm bảo tính nhất quán
    const subtotal = window.paymentData.originalTotal;
    const discountAmount = window.paymentData.discountAmount;
    const finalTotal = window.paymentData.finalTotal;
    const taxRate = 0.1; // 10%
    const tax = finalTotal * taxRate;
    const total = finalTotal + tax;

    // Cập nhật giá trị
    if (subtotalElement) subtotalElement.textContent = formatCurrency(subtotal);
    if (taxAmountElement) taxAmountElement.textContent = formatCurrency(tax);
    if (totalAmountElement)
      totalAmountElement.textContent = formatCurrency(total);

    // Show/hide voucher discount
    if (discountAmount > 0) {
      const discountRow = document.querySelector(
        ".summary-row:has(#discountAmountTop)"
      );
      if (discountRow) {
        discountRow.style.display = "flex";
      }
      if (discountAmountElement) {
        discountAmountElement.textContent =
          "-" + formatCurrency(discountAmount);
      }
    } else {
      const discountRow = document.querySelector(
        ".summary-row:has(#discountAmountTop)"
      );
      if (discountRow) {
        discountRow.style.display = "none";
      }
    }
  }

  // Voucher display and button management is now handled by server-side rendering
  // No need for client-side functions

  function updatePaymentButton() {
    if (!confirmPaymentBtn) {
      console.error("confirmPaymentBtn is null, cannot update button");
      return;
    }

    if (selectedPaymentMethod) {
      confirmPaymentBtn.disabled = false;
      confirmPaymentBtn.innerHTML = `
                <i class="fas fa-check-circle"></i>
                Xác nhận thanh toán (${getPaymentMethodText(
                  selectedPaymentMethod
                )})
            `;
      console.log("Payment button enabled with method:", selectedPaymentMethod);
    } else {
      confirmPaymentBtn.disabled = true;
      confirmPaymentBtn.innerHTML = `
                <i class="fas fa-check-circle"></i>
                Xác nhận thanh toán
            `;
      console.log("Payment button disabled - no method selected");
    }
  }

  function getPaymentMethodText(method) {
    switch (method) {
      case "CASH":
        return "Tiền mặt";
      case "QR_BANKING":
        return "QR Banking";
      case "CREDIT_CARD":
        return "Thẻ tín dụng";
      default:
        return method;
    }
  }

  function confirmPayment() {
    // Always use window.paymentData as the source of truth (it may have been updated)
    if (!window.paymentData) {
      console.error("window.paymentData is not available!");
      showAlert(
        "Lỗi: Dữ liệu thanh toán không sẵn sàng. Vui lòng tải lại trang.",
        "danger"
      );
      return;
    }

    // Always use sessionId from window.paymentData (most up-to-date)
    let actualSessionId = window.paymentData.sessionId;

    // Try to get sessionId from multiple sources
    if (!actualSessionId) {
      // Try to get from URL parameters
      const urlParams = new URLSearchParams(window.location.search);
      actualSessionId = urlParams.get("sessionId");
    }

    if (!actualSessionId) {
      // Try to get from URL path
      const pathMatch = window.location.pathname.match(/\/session\/(\d+)/);
      if (pathMatch) {
        actualSessionId = pathMatch[1];
      }
    }

    // Convert to number if it's a string
    if (actualSessionId && typeof actualSessionId === "string") {
      actualSessionId = parseInt(actualSessionId);
    }

    console.log(
      "Using sessionId:",
      actualSessionId,
      "Type:",
      typeof actualSessionId
    );

    if (!selectedPaymentMethod) {
      console.error("No payment method selected!");
      showAlert("Vui lòng chọn phương thức thanh toán", "warning");
      return;
    }

    // Double check by looking at checked radio button
    const checkedRadio = document.querySelector(
      'input[name="paymentMethod"]:checked'
    );
    if (!checkedRadio) {
      console.error("No radio button is checked!");
      showAlert("Vui lòng chọn phương thức thanh toán", "warning");
      return;
    }

    console.log("Checked radio value:", checkedRadio.value);
    selectedPaymentMethod = checkedRadio.value;

    // Kiểm tra paymentConfirmUrl hoặc sessionId
    let confirmUrl = window.paymentData?.paymentConfirmUrl;

    // Debug: Log paymentConfirmUrl value
    console.log("=== CONFIRM URL CHECK ===");
    console.log("Full window.paymentData:", window.paymentData);
    console.log("paymentConfirmUrl from window.paymentData:", confirmUrl);
    console.log("paymentConfirmUrl type:", typeof confirmUrl);
    console.log("paymentConfirmUrl is null:", confirmUrl === null);
    console.log("paymentConfirmUrl is undefined:", confirmUrl === undefined);
    console.log("paymentConfirmUrl is empty:", confirmUrl === "");
    console.log("paymentConfirmUrl length:", confirmUrl?.length);

    // Nếu không có paymentConfirmUrl (null, undefined, hoặc empty string), tạo từ sessionId (guest flow)
    if (
      !confirmUrl ||
      confirmUrl === null ||
      confirmUrl === undefined ||
      confirmUrl === "" ||
      confirmUrl === "null"
    ) {
      // Kiểm tra sessionId hợp lệ
      if (
        !actualSessionId ||
        actualSessionId === null ||
        actualSessionId === "null" ||
        actualSessionId === "" ||
        isNaN(actualSessionId)
      ) {
        console.error(
          "Invalid sessionId:",
          actualSessionId,
          "Type:",
          typeof actualSessionId
        );
        console.error(
          "paymentConfirmUrl:",
          window.paymentData?.paymentConfirmUrl
        );
        showAlert(
          "Lỗi: Không tìm thấy thông tin session hoặc URL thanh toán. Vui lòng thử lại.",
          "danger"
        );
        return;
      }
      // Tạo URL từ sessionId cho guest flow
      const BASE_URL = window.APP_CTX || "/pizzario";
      // Đảm bảo BASE_URL có dấu / ở cuối nếu cần
      const baseUrlNormalized = BASE_URL.endsWith("/")
        ? BASE_URL.slice(0, -1)
        : BASE_URL;
      confirmUrl = `${baseUrlNormalized}/guest/payment/session/${actualSessionId}/confirm`;
      console.log("Created confirm URL for guest flow:", confirmUrl);
    } else {
      // Nếu có paymentConfirmUrl (cashier flow), không cần sessionId
      // URL đã có context path từ controller rồi, không cần xử lý thêm
      console.log("Using paymentConfirmUrl from server:", confirmUrl);
    }

    if (!confirm("Bạn có chắc chắn muốn thanh toán?")) {
      return;
    }

    console.log("Confirming payment with method:", selectedPaymentMethod);
    console.log("Session ID:", actualSessionId);
    console.log("Confirm URL:", confirmUrl);

    // Show loading modal if available
    if (loadingModal) {
      loadingModal.show();
    }

    // Sử dụng form submission thay vì AJAX để đảm bảo redirect hoạt động đúng
    const form = document.createElement("form");
    form.method = "POST";
    form.action = confirmUrl;

    const paymentMethodInput = document.createElement("input");
    paymentMethodInput.type = "hidden";
    paymentMethodInput.name = "paymentMethod";
    paymentMethodInput.value = selectedPaymentMethod;

    // Thêm CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    if (csrfToken) {
      const csrfInput = document.createElement("input");
      csrfInput.type = "hidden";
      csrfInput.name = "_csrf";
      csrfInput.value = csrfToken.getAttribute("content");
      form.appendChild(csrfInput);
    }

    console.log("Payment method input value:", paymentMethodInput.value);
    console.log("Payment method input name:", paymentMethodInput.name);

    form.appendChild(paymentMethodInput);
    document.body.appendChild(form);

    console.log("Form created with action:", form.action);
    console.log("Form method:", form.method);
    console.log("Payment method input:", paymentMethodInput.value);
    console.log("Form HTML:", form.outerHTML);
    console.log("Submitting form...");

    // Add a small delay to see the form in DOM
    setTimeout(() => {
      console.log(
        "About to submit form with paymentMethod:",
        paymentMethodInput.value
      );
      form.submit();
    }, 100);
  }

  function showAlert(message, type) {
    // Remove existing alerts
    const existingAlerts = document.querySelectorAll(".alert");
    existingAlerts.forEach((alert) => alert.remove());

    // Create new alert
    const alertDiv = document.createElement("div");
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

    // Insert at the top of main content
    const mainContent = document.querySelector(".main-payment-content");
    if (mainContent) {
      mainContent.insertBefore(alertDiv, mainContent.firstChild);
    }

    // Auto dismiss after 5 seconds
    setTimeout(() => {
      if (alertDiv.parentNode) {
        alertDiv.remove();
      }
    }, 5000);
  }

  function formatCurrency(amount) {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount);
  }

  // Page data is now managed by server-side rendering after page reload
  // No need for client-side data updates

  // Add some visual feedback for interactions
  document.querySelectorAll(".voucher-card").forEach((card) => {
    card.addEventListener("mouseenter", function () {
      this.style.transform = "translateY(-2px)";
    });

    card.addEventListener("mouseleave", function () {
      this.style.transform = "translateY(0)";
    });
  });

  document.querySelectorAll(".payment-method-item").forEach((item) => {
    item.addEventListener("mouseenter", function () {
      if (!this.querySelector("input").checked) {
        this.style.borderColor = "#007bff";
        this.style.transform = "translateY(-2px)";
      }
    });

    item.addEventListener("mouseleave", function () {
      if (!this.querySelector("input").checked) {
        this.style.borderColor = "#e9ecef";
        this.style.transform = "translateY(0)";
      }
    });
  });
} // End of initializePayment function
