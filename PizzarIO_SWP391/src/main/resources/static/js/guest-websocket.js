let guestStomp = null;
let guestSessionId = null;

window.initGuestWebSocket = function () {
    // Generate a simple session id for demo
    guestSessionId = (window.GUEST_SESSION || Math.random().toString(36).slice(2));

    const base = (window.APP_CTX || '/');
    const socket = new SockJS(base.replace(/\/$/, '') + '/ws');
    guestStomp = Stomp.over(socket);
    guestStomp.debug = null;

    guestStomp.connect({}, function () {
        // Personal queue
        guestStomp.subscribe('/queue/guest-' + guestSessionId, function (msg) {
            const data = JSON.parse(msg.body);
            console.log('[Guest] personal message:', data);
            const el = document.getElementById('guest-result');
            if (!el) return;
            if (data.type === 'SUCCESS') {
                el.textContent = data.message;
                el.className = 'guest-result success';
                // Redirect to menu with session info
                const params = new URLSearchParams();
                if (data.sessionId) params.set('sessionId', data.sessionId);
                if (data.tableId) params.set('tableId', data.tableId);
                const ctx = (window.APP_CTX || '/').replace(/\/$/, '');
                window.location.href = ctx + '/guest/menu' + (params.toString() ? ('?' + params.toString()) : '');
            } else if (data.type === 'CONFLICT') {
                // Show conflict message and suggest available tables
                el.textContent = (data.message || 'Bàn đã được chọn.') + (Array.isArray(data.availableTables) && data.availableTables.length ? ' Gợi ý bàn trống: ' + data.availableTables.join(', ') : '');
                el.className = 'guest-result error';
                // Optionally highlight suggested tables if present in DOM
                highlightSuggestedTables(data.availableTables);
            } else {
                el.textContent = data.message || 'Có lỗi xảy ra';
                el.className = 'guest-result error';
            }
        });

        // Broadcast updates for guest tablets
        guestStomp.subscribe('/topic/tables-guest', function (msg) {
            const data = JSON.parse(msg.body);
            console.log('[Guest] table broadcast:', data);

            // Xử lý trường hợp bàn bị retired
            if (data.type === 'TABLE_RETIRED') {
                handleGuestTableRetired(data);
                return;
            }

            // Find the table tile in the DOM
            const tableEl = document.getElementById('table-' + data.tableId);
            if (!tableEl) return;

            // Find the status element inside the tile
            const statusEl = tableEl.querySelector('.status');
            if (statusEl) {
                statusEl.textContent = data.newStatus;
            }

            // Update the disabled/enabled state of the tile
            if (data.newStatus === 'AVAILABLE') {
                tableEl.classList.remove('disabled');
            } else {
                tableEl.classList.add('disabled');
            }
        });
    });
};

function highlightSuggestedTables(ids) {
    if (!Array.isArray(ids)) return;
    // Remove previous highlights
    document.querySelectorAll('.t.suggest').forEach(el => el.classList.remove('suggest'));
    ids.forEach(id => {
        const el = document.getElementById('table-' + id);
        if (el) el.classList.add('suggest');
    });
}

/**
 * Xử lý khi bàn được đánh dấu retired trên Guest page
 */
function handleGuestTableRetired(data) {
    console.log('[Guest] Handling table retired:', data);
    
    const tableEl = document.getElementById('table-' + data.tableId);
    if (tableEl) {
        // Thêm animation fade out trước khi xóa
        tableEl.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
        tableEl.style.opacity = '0';
        tableEl.style.transform = 'scale(0.8)';
        
        // Xóa bàn khỏi UI sau animation
        setTimeout(() => {
            tableEl.remove();
        }, 500);
    }
    
    // Hiển thị thông báo cho guest
    const resultEl = document.getElementById('guest-result');
    if (resultEl) {
        resultEl.className = 'guest-result warning';
    }
}

window.guestSelectTable = function (tableId, guestCount = 1) {
    if (!guestStomp || !guestStomp.connected) return;
    const req = {tableId, sessionId: guestSessionId, guestCount};
    guestStomp.send('/app/guest/select-table', {}, JSON.stringify(req));
};



