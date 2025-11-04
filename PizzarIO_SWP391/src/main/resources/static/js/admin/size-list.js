// size-list.js - ĐỘC LẬP, giống product-list.js
const SizeList = (function () {
    const modal = document.getElementById('sizeModal');
    const searchInput = document.getElementById('sizeSearch');

    // Tìm kiếm realtime
    const initSearch = () => {
        if (!searchInput) return;
        searchInput.addEventListener('input', () => {
            const query = searchInput.value.toLowerCase().trim();
            document.querySelectorAll('.size-item').forEach(item => {
                const text = item.textContent.toLowerCase();
                item.style.display = text.includes(query) ? '' : 'none';
            });
        });
    };

    // Mở modal
    const openModal = () => {
        if (modal) {
            modal.style.display = 'flex';
            const firstInput = modal.querySelector('input[type="text"]');
            if (firstInput) firstInput.focus();
        }
    };

    // Đóng modal
    const closeModal = () => {
        if (modal) modal.style.display = 'none';
        document.querySelectorAll('.size-error-field').forEach(el => {
            el.classList.remove('size-error-field');
        });
    };

    // Khởi động
    const init = () => {
        initSearch();

        // Tự động mở modal
        if (window.sizeOpenModal) {
            openModal();
        }

        // ESC để đóng
        document.addEventListener('keydown', e => {
            if (e.key === 'Escape') closeModal();
        });
    };

    // Public API
    return { init, closeModal, openModal };
})();

// Khởi động khi DOM ready
document.addEventListener('DOMContentLoaded', () => {
    SizeList.init();
});