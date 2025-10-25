// Cashier Dashboard JavaScript
document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
});

function initializeDashboard() {
    setupPaymentMethods();
    setupTipButtons();
    setupActionButtons();
    updateClock();
}

// Payment Methods
function setupPaymentMethods() {
    const paymentBtns = document.querySelectorAll('.payment-btn');
    
    paymentBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            // Remove selected class from all payment buttons
            paymentBtns.forEach(b => b.classList.remove('selected'));
            
            // Add selected class to clicked button
            this.classList.add('selected');
            
            // Update payment buttons at bottom
            updatePaymentButtons(this.classList.contains('cash'));
        });
    });
}

function updatePaymentButtons(isCash) {
    const payCashBtn = document.querySelector('.payment-buttons .btn-success');
    const payCardBtn = document.querySelector('.payment-buttons .btn-primary');
    
    if (isCash) {
        if (payCashBtn) payCashBtn.style.display = 'flex';
        if (payCardBtn) payCardBtn.style.display = 'none';
    } else {
        if (payCashBtn) payCashBtn.style.display = 'none';
        if (payCardBtn) payCardBtn.style.display = 'flex';
    }
}

// Tip Buttons
function setupTipButtons() {
    const tipBtns = document.querySelectorAll('.tip-btn');
    
    tipBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            // Remove selected class from all tip buttons
            tipBtns.forEach(b => b.classList.remove('selected'));
            
            // Add selected class to clicked button
            this.classList.add('selected');
            
            // Calculate tip amount
            calculateTip(this.textContent);
        });
    });
}

function calculateTip(tipPercentage) {
    const subtotal = 470000; // This would come from actual order data
    let tipAmount = 0;
    
    if (tipPercentage === '5%') {
        tipAmount = subtotal * 0.05;
    } else if (tipPercentage === '10%') {
        tipAmount = subtotal * 0.10;
    } else if (tipPercentage === '15%') {
        tipAmount = subtotal * 0.15;
    } else if (tipPercentage === 'Other') {
        // Show input for custom tip
        const customTip = prompt('Enter custom tip amount:');
        if (customTip && !isNaN(customTip)) {
            tipAmount = parseFloat(customTip);
        }
    }
    
    // Update total with tip
    updateTotalWithTip(tipAmount);
}

function updateTotalWithTip(tipAmount) {
    const subtotal = 470000;
    const tax = 47000;
    const discount = 50000;
    const total = subtotal + tax - discount + tipAmount;
    
    // Update total display
    const totalElement = document.querySelector('.summary-row.total span:last-child');
    if (totalElement) {
        totalElement.textContent = total.toLocaleString('vi-VN');
    }
}

// Action Buttons
function setupActionButtons() {
    // New Order button
    const newOrderBtn = document.querySelector('.action-buttons .btn-primary');
    if (newOrderBtn) {
        newOrderBtn.addEventListener('click', function() {
            if (confirm('Tạo đơn hàng mới cho bàn này?')) {
                // This would typically open a new order modal or redirect
                alert('Tính năng tạo đơn hàng mới sẽ được triển khai');
            }
        });
    }
    
    // View History button
    const viewHistoryBtn = document.querySelector('.action-buttons .btn-secondary');
    if (viewHistoryBtn) {
        viewHistoryBtn.addEventListener('click', function() {
            // This would typically open a history modal or redirect
            alert('Tính năng xem lịch sử sẽ được triển khai');
        });
    }
    
    // Pay buttons
    const payCashBtn = document.querySelector('.payment-buttons .btn-success');
    const payCardBtn = document.querySelector('.payment-buttons .btn-primary');
    
    if (payCashBtn) {
        payCashBtn.addEventListener('click', function() {
            processPayment('cash');
        });
    }
    
    if (payCardBtn) {
        payCardBtn.addEventListener('click', function() {
            processPayment('card');
        });
    }
    
    // Bottom action buttons
    const splitBillBtn = document.querySelector('.action-buttons-bottom .btn:nth-child(1)');
    const printBtn = document.querySelector('.action-buttons-bottom .btn:nth-child(2)');
    const editBtn = document.querySelector('.action-buttons-bottom .btn:nth-child(3)');
    
    if (splitBillBtn) {
        splitBillBtn.addEventListener('click', function() {
            alert('Tính năng chia hóa đơn sẽ được triển khai');
        });
    }
    
    if (printBtn) {
        printBtn.addEventListener('click', function() {
            window.print();
        });
    }
    
    if (editBtn) {
        editBtn.addEventListener('click', function() {
            alert('Tính năng chỉnh sửa đơn hàng sẽ được triển khai');
        });
    }
}

function processPayment(method) {
    const methodText = method === 'cash' ? 'tiền mặt' : 'thẻ';
    
    if (confirm(`Xác nhận thanh toán bằng ${methodText}?`)) {
        // Show loading state
        const payBtn = method === 'cash' ? 
            document.querySelector('.payment-buttons .btn-success') :
            document.querySelector('.payment-buttons .btn-primary');
            
        if (payBtn) {
            const originalText = payBtn.textContent;
            payBtn.textContent = 'Đang xử lý...';
            payBtn.disabled = true;
            
            // Simulate payment processing
            setTimeout(() => {
                alert(`Thanh toán bằng ${methodText} thành công!`);
                payBtn.textContent = originalText;
                payBtn.disabled = false;
                
                // Reset table status
                resetTableAfterPayment();
            }, 2000);
        }
    }
}

function resetTableAfterPayment() {
    // Remove selection from current table
    document.querySelectorAll('.table-card').forEach(card => {
        card.classList.remove('selected');
    });
    
    // Hide order details
    const orderInfoCard = document.querySelector('.order-info-card');
    const orderItemsSection = document.querySelector('.order-items-section');
    const specialNotes = document.querySelector('.special-notes');
    const orderSummary = document.querySelector('.order-summary-section');
    
    if (orderInfoCard) orderInfoCard.style.display = 'none';
    if (orderItemsSection) orderItemsSection.style.display = 'none';
    if (specialNotes) specialNotes.style.display = 'none';
    if (orderSummary) orderSummary.style.display = 'none';
    
    // Show empty state
    const emptyState = document.querySelector('.empty-state');
    if (emptyState) {
        emptyState.style.display = 'flex';
    }
}

// Clock Update
function updateClock() {
    const clockElement = document.getElementById('clock');
    const dateElement = document.getElementById('date');
    
    if (clockElement && dateElement) {
        const now = new Date();
        
        // Update time
        const timeString = now.toLocaleTimeString('vi-VN', {
            hour12: false,
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
        clockElement.textContent = timeString;
        
        // Update date
        const dateString = now.toLocaleDateString('vi-VN', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
        dateElement.textContent = dateString;
    }
    
    // Update every second
    setTimeout(updateClock, 1000);
}



// Keyboard shortcuts
document.addEventListener('keydown', function(event) {
    // F1 - New Order
    if (event.key === 'F1') {
        event.preventDefault();
        const newOrderBtn = document.querySelector('.action-buttons .btn-primary');
        if (newOrderBtn) newOrderBtn.click();
    }
    
    // F2 - View History
    if (event.key === 'F2') {
        event.preventDefault();
        const viewHistoryBtn = document.querySelector('.action-buttons .btn-secondary');
        if (viewHistoryBtn) viewHistoryBtn.click();
    }
    
    // Ctrl+P - Print
    if (event.ctrlKey && event.key === 'p') {
        event.preventDefault();
        const printBtn = document.querySelector('.action-buttons-bottom .btn:nth-child(2)');
        if (printBtn) printBtn.click();
    }
});


