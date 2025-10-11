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
            } else {
                el.textContent = data.message || 'Có lỗi xảy ra';
                el.className = 'guest-result error';
            }
        });

        // Broadcast updates for guest tablets
        guestStomp.subscribe('/topic/tables-guest', function (msg) {
            const data = JSON.parse(msg.body);
            console.log('[Guest] table broadcast:', data);
        });
    });
};

window.guestSelectTable = function (tableId, guestCount = 1) {
    if (!guestStomp || !guestStomp.connected) return;
    const req = { tableId, sessionId: guestSessionId, guestCount };
    guestStomp.send('/app/guest/select-table', {}, JSON.stringify(req));
};



