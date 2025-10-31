// Kitchen Dashboard JavaScript

class KitchenDashboard {
    constructor() {
        this.currentCategory = 'all';
        this.orderItems = [];
        this.categories = [];
        this.stats = {
            total: 0,
            new: 0,
            preparing: 0,
            completed: 0
        };
        
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadDashboardData();
        this.updateLastUpdateTime();
        
        // Update time every second
        setInterval(() => {
            this.updateLastUpdateTime();
        }, 1000);
    }

    setupEventListeners() {
        // Category filter
        document.addEventListener('click', (e) => {
            if (e.target.closest('.category-item')) {
                const categoryItem = e.target.closest('.category-item');
                const category = categoryItem.dataset.category;
                this.filterByCategory(category);
            }
        });

        // Action buttons
        document.addEventListener('click', (e) => {
            if (e.target.closest('.action-btn')) {
                const btn = e.target.closest('.action-btn');
                const action = btn.dataset.action;
                const itemId = btn.dataset.itemId;
                this.handleItemAction(action, itemId);
            }
        });
    }

    async loadDashboardData() {
        try {
            // Load order items data
            const response = await fetch('/kitchen/api/dashboard-data');
            if (!response.ok) {
                throw new Error('Failed to load dashboard data');
            }
            
            const data = await response.json();
            this.orderItems = data.orderItems || [];
            this.categories = data.categories || [];
            
            this.updateStats();
            this.renderCategories();
            this.renderOrderItems();
            
        } catch (error) {
            console.error('Error loading dashboard data:', error);
            this.showError('Không thể tải dữ liệu dashboard');
        }
    }

    updateStats() {
        this.stats = {
            total: this.orderItems.length,
            new: this.orderItems.filter(item => item.status === 'PENDING').length,
            preparing: this.orderItems.filter(item => item.status === 'PREPARING').length,
            completed: this.orderItems.filter(item => item.status === 'SERVED').length
        };

        // Update stat cards
        document.getElementById('totalDishes').textContent = this.stats.total;
        document.getElementById('newDishes').textContent = this.stats.new;
        document.getElementById('preparingDishes').textContent = this.stats.preparing;
        document.getElementById('completedDishes').textContent = this.stats.completed;
    }

    renderCategories() {
        const categoryFilter = document.getElementById('categoryFilter');
        
        // Clear existing categories (except "Tất cả")
        const allCategory = categoryFilter.querySelector('[data-category="all"]');
        categoryFilter.innerHTML = '';
        categoryFilter.appendChild(allCategory);

        // Add category items
        this.categories.forEach(category => {
            const count = this.orderItems.filter(item => 
                item.categoryId === category.id
            ).length;

            const categoryItem = document.createElement('div');
            categoryItem.className = 'category-item';
            categoryItem.dataset.category = category.id;
            
            categoryItem.innerHTML = `
                <div class="category-icon">
                    <i class="${this.getCategoryIcon(category.name)}"></i>
                </div>
                <div class="category-name">${category.name}</div>
                <div class="category-count">${count}</div>
            `;
            
            categoryFilter.appendChild(categoryItem);
        });

        // Update "Tất cả" count
        document.getElementById('count-all').textContent = this.stats.total;
    }

    renderOrderItems() {
        const container = document.getElementById('orderItemsContainer');
        const emptyState = document.getElementById('emptyState');
        
        if (this.orderItems.length === 0) {
            container.style.display = 'none';
            emptyState.style.display = 'block';
            return;
        }

        container.style.display = 'block';
        emptyState.style.display = 'none';

        // Filter items by category
        let filteredItems = this.orderItems;
        if (this.currentCategory !== 'all') {
            filteredItems = this.orderItems.filter(item => 
                item.categoryId === parseInt(this.currentCategory)
            );
        }

        if (filteredItems.length === 0) {
            container.innerHTML = '<div class="empty-state"><div class="empty-icon"><i class="fas fa-utensils"></i></div><h3>Không có món ăn nào</h3><p>Không có món ăn nào trong danh mục này</p></div>';
            return;
        }

        // Group items by category
        const groupedItems = this.groupItemsByCategory(filteredItems);
        
        // Sort categories by name
        const sortedCategories = Object.keys(groupedItems).sort();
        
        container.innerHTML = '';
        
        sortedCategories.forEach(categoryName => {
            const items = groupedItems[categoryName];
            
            // Sort items by time and name
            items.sort((a, b) => {
                // First sort by status (PENDING -> PREPARING -> SERVED)
                const statusOrder = { 'PENDING': 0, 'PREPARING': 1, 'SERVED': 2 };
                const statusDiff = statusOrder[a.status] - statusOrder[b.status];
                if (statusDiff !== 0) return statusDiff;
                
                // Then sort by creation time (newest first)
                const timeDiff = new Date(b.createdAt) - new Date(a.createdAt);
                if (timeDiff !== 0) return timeDiff;
                
                // Finally sort by product name
                return a.product.name.localeCompare(b.product.name);
            });

            const categorySection = this.createCategorySection(categoryName, items);
            container.appendChild(categorySection);
        });
    }

    groupItemsByCategory(items) {
        const grouped = {};
        
        items.forEach(item => {
            const categoryName = item.categoryName;
            if (!grouped[categoryName]) {
                grouped[categoryName] = [];
            }
            grouped[categoryName].push(item);
        });
        
        return grouped;
    }

    createCategorySection(categoryName, items) {
        const section = document.createElement('div');
        section.className = 'category-section';
        
        const count = items.length;
        const categoryIcon = this.getCategoryIcon(categoryName);
        
        section.innerHTML = `
            <div class="category-header">
                <div class="category-header-icon">
                    <i class="${categoryIcon}"></i>
                </div>
                <div class="category-header-title">${categoryName} ${count} món</div>
            </div>
            <div class="order-items-list">
                ${items.map(item => this.createOrderItemCard(item)).join('')}
            </div>
        `;
        
        return section;
    }

    createOrderItemCard(item) {
        const statusClass = this.getStatusClass(item.status);
        const statusText = this.getStatusText(item.status);
        const actionButton = this.createActionButton(item);
        const timeAgo = this.getTimeAgo(item.createdAt);
        
        return `
            <div class="order-item-card ${statusClass}">
                <div class="order-item-info">
                    <h3 class="order-item-name">${item.productName}</h3>
                    <div class="order-item-meta">
                        <span class="order-item-quantity">x${item.quantity}</span>
                        <span class="order-item-status ${statusClass}">${statusText}</span>
                        <span class="order-item-location">
                            <i class="fas fa-utensils"></i>
                            Tại bàn
                        </span>
                    </div>
                </div>
                <div class="order-item-details">
                    <div class="order-item-order">${item.orderInfo.code}</div>
                    <div class="order-item-table">${item.orderInfo.tableName}</div>
                    <div class="order-item-time">
                        <i class="fas fa-clock"></i>
                        ${timeAgo}
                    </div>
                </div>
                <div class="order-item-action">
                    ${actionButton}
                </div>
            </div>
        `;
    }

    createActionButton(item) {
        switch (item.status) {
            case 'PENDING':
                return `<button class="action-btn start" data-action="start" data-item-id="${item.id}">Bắt đầu làm</button>`;
            case 'PREPARING':
                return `<button class="action-btn complete" data-action="complete" data-item-id="${item.id}">Hoàn thành</button>`;
            case 'SERVED':
                return `<button class="action-btn undo" data-action="undo" data-item-id="${item.id}">Hoàn tác</button>`;
            default:
                return '';
        }
    }

    getStatusClass(status) {
        switch (status) {
            case 'PENDING': return 'new';
            case 'PREPARING': return 'preparing';
            case 'SERVED': return 'completed';
            default: return 'new';
        }
    }

    getStatusText(status) {
        switch (status) {
            case 'PENDING': return 'Mới';
            case 'PREPARING': return 'Đang làm';
            case 'SERVED': return 'Hoàn thành';
            default: return 'Mới';
        }
    }

    getCategoryIcon(categoryName) {
        const iconMap = {
            'Khai vị': 'fas fa-leaf',
            'Mì & Phở': 'fas fa-bowl-food',
            'Cơm': 'fas fa-rice',
            'Món chính': 'fas fa-drumstick-bite',
            'Hải sản': 'fas fa-fish',
            'Đồ uống': 'fas fa-glass',
            'Tráng miệng': 'fas fa-ice-cream',
            'Pizza': 'fas fa-pizza-slice',
            'Salad': 'fas fa-seedling'
        };
        
        return iconMap[categoryName] || 'fas fa-utensils';
    }

    getTimeAgo(dateString) {
        const now = new Date();
        const date = new Date(dateString);
        const diffMs = now - date;
        const diffMins = Math.floor(diffMs / 60000);
        
        if (diffMins < 1) return 'Vừa xong';
        if (diffMins < 60) return `${diffMins} phút trước`;
        
        const diffHours = Math.floor(diffMins / 60);
        if (diffHours < 24) return `${diffHours} giờ trước`;
        
        const diffDays = Math.floor(diffHours / 24);
        return `${diffDays} ngày trước`;
    }

    filterByCategory(category) {
        // Update active category
        document.querySelectorAll('.category-item').forEach(item => {
            item.classList.remove('active');
        });
        document.querySelector(`[data-category="${category}"]`).classList.add('active');
        
        this.currentCategory = category;
        this.renderOrderItems();
    }

    async handleItemAction(action, itemId) {
        try {
            const response = await fetch('/kitchen/api/update-item-status', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    itemId: parseInt(itemId),
                    action: action
                })
            });

            if (!response.ok) {
                throw new Error('Failed to update item status');
            }

            // Reload dashboard data
            await this.loadDashboardData();
            this.showSuccess('Cập nhật trạng thái thành công');

        } catch (error) {
            console.error('Error updating item status:', error);
            this.showError('Không thể cập nhật trạng thái món ăn');
        }
    }

    updateLastUpdateTime() {
        const now = new Date();
        const timeString = now.toLocaleTimeString('vi-VN', {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
        document.getElementById('lastUpdateTime').textContent = timeString;
    }

    refreshDashboard() {
        this.loadDashboardData();
        this.showSuccess('Đã làm mới dữ liệu');
    }

    showSuccess(message) {
        this.showNotification(message, 'success');
    }

    showError(message) {
        this.showNotification(message, 'error');
    }

    showNotification(message, type) {
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.textContent = message;
        
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.classList.add('show');
        }, 100);
        
        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => {
                document.body.removeChild(notification);
            }, 300);
        }, 3000);
    }
}

// Initialize dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.kitchenDashboard = new KitchenDashboard();
});

// Global refresh function
function refreshDashboard() {
    if (window.kitchenDashboard) {
        window.kitchenDashboard.refreshDashboard();
    }
}
