'use strict';

/**
 * Displays a toast notification.
 * This function requires a div with id="toast-container" to be present in the HTML.
 * @param {string} message - The message to display.
 * @param {string} type - 'success', 'error', or 'warning'.
 * @param {number} [duration=3000] - Duration in milliseconds before the toast disappears.
 */
function showToast(message, type, duration = 3000) {
    const container = document.getElementById('toast-container');
    if (!container) {
        console.error('Toast container not found!');
        return;
    }

    const toast = document.createElement('div');
    toast.className = 'toast ' + type;
    toast.textContent = message;

    container.appendChild(toast);

    // Auto-remove the toast
    setTimeout(() => {
        // Animate out
        toast.style.animation = 'slideInRight 0.3s ease reverse forwards';
        // Remove from DOM after animation
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, duration);
}