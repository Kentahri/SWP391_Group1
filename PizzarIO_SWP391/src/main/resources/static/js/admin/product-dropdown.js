document.addEventListener('DOMContentLoaded', function () {
    const dropdown = document.querySelector('.nav-dropdown');
    if (!dropdown) return;

    const toggle = dropdown.querySelector('.dropdown-toggle');

    // Click để toggle
    toggle.addEventListener('click', function (e) {
        e.preventDefault();
        dropdown.classList.toggle('active');
    });

    // Click ngoài → đóng
    document.addEventListener('click', function (e) {
        if (!dropdown.contains(e.target)) {
            dropdown.classList.remove('active');
        }
    });

    // Tự động mở nếu đang ở trang con
    const isActive = /*[[${activePage == 'products' || activePage == 'sizes' || activePage == 'product-sizes'}]]*/ false;
    if (isActive) {
        dropdown.classList.add('active');
    }
});
