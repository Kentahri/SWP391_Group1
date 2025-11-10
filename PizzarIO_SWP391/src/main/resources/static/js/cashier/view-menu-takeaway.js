async function showProductDetail(productId) {
    const modal = document.getElementById("productModal");
    const content = document.getElementById("productModalContent");
    content.innerHTML = "<p>Đang tải...</p>";
    modal.style.display = "flex";

    try {
        const res = await fetch(
            `/pizzario/cashier/product/detail/${productId}`
        );
        const html = await res.text();
        content.innerHTML = html;
    } catch (e) {
        content.innerHTML = "<p style='color:red;'>Lỗi khi tải sản phẩm!</p>";
    }
}


window.onclick = function (e) {
    const modal = document.getElementById("productModal");
    if (e.target === modal) modal.style.display = "none";
};

document.addEventListener("DOMContentLoaded", () => {
    const productGrid = document.getElementById("productGrid");
    const categoryBar = document.getElementById("categoryBar");
    const searchBox = document.getElementById("searchBox");

    function filterProducts() {
        const keyword = searchBox.value.toLowerCase().trim();
        const activeCategory = categoryBar.querySelector(".cat-chip.active");
        const activeCategoryId = activeCategory ? activeCategory.getAttribute("data-category-id") : "0";

        const productCards = productGrid.querySelectorAll(".product-card");

        productCards.forEach(card => {
            const cardCategoryId = card.getAttribute("data-category-id");
            const nameElement = card.querySelector(".name");
            const cardName = nameElement ? nameElement.textContent.toLowerCase() : "";

            // So khớp category (nếu =0 thì là All)
            const categoryMatch = activeCategoryId === "0" || cardCategoryId === activeCategoryId;

            // So khớp keyword (nếu không nhập gì thì true)
            const keywordMatch = keyword === "" || cardName.includes(keyword);

            // Hiển thị nếu thỏa cả 2 điều kiện
            card.style.display = categoryMatch && keywordMatch ? "flex" : "none";
        });
    }

    // Khi click chọn category
    categoryBar.addEventListener("click", (e) => {
        if (e.target.classList.contains("cat-chip")) {
            const active = categoryBar.querySelector(".cat-chip.active");
            if (active) active.classList.remove("active");
            e.target.classList.add("active");
            filterProducts();
        }
    });

    // Khi nhập ô tìm kiếm
    searchBox.addEventListener("input", filterProducts);

    // --- AJAX Form Submission Logic ---
    // This makes the cart interactions smooth without page reloads.
    document.body.addEventListener("submit", async (e) => {
        // We only handle forms with the 'ajax-form' class
        if (!e.target.classList.contains("ajax-form")) {
            return;
        }

        e.preventDefault();
        const form = e.target;
        const url = form.action;


        const formData = new FormData(form);


        try {
            const response = await fetch(url, {
                method: "POST",
                body: formData,
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            // The server redirects, and fetch follows, returning the final HTML.
            // We parse this new HTML to update only the sidebar.
            const html = await response.text();
            const parser = new DOMParser();
            const newDoc = parser.parseFromString(html, "text/html");

            const newSidebar = newDoc.getElementById("sidebar");
            const currentSidebar = document.getElementById("sidebar");

            if (newSidebar && currentSidebar) {
                // Replace the old sidebar with the updated one from the server response
                currentSidebar.innerHTML = newSidebar.innerHTML;
            } else {
                // Fallback to full reload if something goes wrong
                window.location.reload();
            }
        } catch (error) {
            console.error("Form submission failed:", error);
            // Optionally show an error message to the user
            alert("An error occurred. Please try again.");
        }
    });

    // Initial filter call
    filterProducts();
});

