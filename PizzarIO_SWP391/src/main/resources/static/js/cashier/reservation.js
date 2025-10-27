/**
 * Mở modal đặt bàn
 */
window.openReservationModal = function(tableId) {
    const modal = document.getElementById('reservation-modal');
    const form = document.getElementById('reservation-form');

    // Reset lại form để xóa các giá trị đã có sẵn
    form.reset();

    // Gán table id cho bàn (hidden input và display input)
    document.getElementById('table-id-hidden').value = tableId;
    document.getElementById('table-id-display').value = 'Bàn ' + tableId;

    // Set các giá trị mặc định
    const now = new Date();
    const startTime = new Date(now.getTime());
    document.getElementById('start-time').value = formatDateTimeLocal(startTime);

    // Show modal
    modal.classList.add('show');
    
    // Prevent body scroll when modal is open
    document.body.style.overflow = 'hidden';
};

/**
 * Đóng modal đặt bàn
 */
window.closeReservationModal = function() {
    const modal = document.getElementById('reservation-modal');
    modal.classList.remove('show');
    
    // Restore body scroll when modal is closed
    document.body.style.overflow = '';
    
    // Xóa tất cả toast trong modal khi đóng
    const modalToastContainer = document.getElementById('modal-toast-container');
    modalToastContainer.innerHTML = '';
};

/**
 * Đóng modal khi click ra bên ngoài
 */
window.addEventListener('click', function(event) {
    const modal = document.getElementById('reservation-modal');
    if (event.target === modal) {
        closeReservationModal();
    }
});