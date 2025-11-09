class ChatbotWidget {
    // Constants
    static BUBBLE_SIZE = 64;
    static WINDOW_WIDTH = 400;
    static WINDOW_HEIGHT = 600;
    static GAP = 20;
    static PADDING = 10;
    static DRAG_THRESHOLD = 5;

    constructor() {
        this.isOpen = false;
        this.messages = [];
        this.isDragging = false;
        this.hasDragged = false;
        this.dragOffset = { x: 0, y: 0 };
        this.bubblePosition = { x: 20, y: 20 };
        this.windowPosition = { x: 20, y: 20 };
        this.init();
    }

    init() {
        this.loadPosition();
        this.createWidget();
        this.attachEventListeners();
        this.setupDragAndDrop();
        this.setupResizeHandler();
    }

    setupResizeHandler() {
        window.addEventListener('resize', () => {
            const container = document.getElementById('chatbotWidgetContainer');
            if (!container) return;

            const rect = container.getBoundingClientRect();
            const windowWidth = window.innerWidth;
            const windowHeight = window.innerHeight;
            
            let newX = this.bubblePosition.x;
            let newY = this.bubblePosition.y;
            
            if (rect.right > windowWidth) newX = 0;
            if (rect.bottom > windowHeight) newY = 0;
            
            if (newX !== this.bubblePosition.x || newY !== this.bubblePosition.y) {
                this.bubblePosition.x = newX;
                this.bubblePosition.y = newY;
                this.updatePosition();
                this.savePosition();
            }
        });
    }

    loadPosition() {
        const savedBubblePosition = localStorage.getItem('chatbotBubblePosition');
        const savedWindowPosition = localStorage.getItem('chatbotWindowPosition');
        
        if (savedBubblePosition) {
            try {
                this.bubblePosition = JSON.parse(savedBubblePosition);
            } catch (e) {
                console.error('Error loading chatbot bubble position:', e);
            }
        }
        
        if (savedWindowPosition) {
            try {
                this.windowPosition = JSON.parse(savedWindowPosition);
            } catch (e) {
                console.error('Error loading chatbot window position:', e);
            }
        }
    }

    savePosition() {
        localStorage.setItem('chatbotBubblePosition', JSON.stringify(this.bubblePosition));
        localStorage.setItem('chatbotWindowPosition', JSON.stringify(this.windowPosition));
    }

    createWidget() {
        const widget = document.createElement('div');
        widget.innerHTML = `
            <div class="chatbot-widget" id="chatbotWidgetContainer">
                <button class="chatbot-toggle" id="chatbotToggle">
                    <svg width="28" height="28" viewBox="0 0 24 24" fill="white">
                        <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>
                    </svg>
                </button>
                
                <div class="chatbot-window" id="chatbotWindow">
                    <div class="chatbot-header">
                        <div class="chatbot-header-left">
                            <div class="chatbot-avatar-draggable" id="chatbotAvatarDraggable">
                                <span class="chatbot-avatar-icon">🤖</span>
                            </div>
                            <div class="chatbot-title">
                                <h3>PizzarIO Assistant</h3>
                                <p>Trực tuyến</p>
                            </div>
                        </div>
                        <button class="chatbot-close" id="chatbotClose">×</button>
                    </div>
                    
                    <div class="chatbot-messages" id="chatbotMessages">
                        <div class="message bot">
                            <div class="message-content">
                                <div class="message-avatar">🤖</div>
                                <div class="message-bubble">
                                    <p class="mb-0">Xin chào! 👋</p>
                                    <p class="mb-0">Tôi là trợ lý ảo của PizzarIO. Tôi có thể giúp bạn:</p>
                                    <ul class="mb-0 mt-2 ps-4">
                                        <li>💰 Tìm món ăn giá rẻ nhất</li>
                                        <li>💎 Tìm món ăn đắt nhất</li>
                                        <li>🎉 Xem các khuyến mãi hiện có</li>
                                        <li>🍕 Tìm combo</li>
                                        <li>🔥 Xem món ăn bán chạy nhất</li>
                                    </ul>
                                    <p class="mb-0 mt-2">Hoặc dùng các nút bên dưới để hỏi nhanh! 😊</p>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="chatbot-input">
                        <div class="input-group">
                            <input type="text" id="chatbotInput" placeholder="Nhập tin nhắn của bạn..." autocomplete="off" />
                            <button id="chatbotSend">
                                <span id="sendIcon">📤</span>
                            </button>
                        </div>
                        <div class="quick-buttons">
                            <button class="quick-btn" data-msg="món giá rẻ nhất">💰 Giá rẻ</button>
                            <button class="quick-btn" data-msg="món đắt nhất">💎 Cao cấp</button>
                            <button class="quick-btn" data-msg="khuyến mãi">🎉 Khuyến mãi</button>
                            <button class="quick-btn" data-msg="bán chạy">🔥 Bán chạy</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        document.body.appendChild(widget);
        this.updatePosition();
    }

    updatePosition() {
        const container = document.getElementById('chatbotWidgetContainer');
        const chatWindow = document.getElementById('chatbotWindow');
        const windowWidth = window.innerWidth;
        const windowHeight = window.innerHeight;
        
        // Update bubble position
        if (container) {
            const left = windowWidth - this.bubblePosition.x - ChatbotWidget.BUBBLE_SIZE;
            const top = windowHeight - this.bubblePosition.y - ChatbotWidget.BUBBLE_SIZE;
            container.style.left = `${left}px`;
            container.style.top = `${top}px`;
            container.style.right = 'auto';
            container.style.bottom = 'auto';
        }
        
        // Update window position
        if (this.isOpen && chatWindow) {
            const left = windowWidth - this.windowPosition.x - ChatbotWidget.WINDOW_WIDTH;
            const top = windowHeight - this.windowPosition.y - ChatbotWidget.WINDOW_HEIGHT;
            chatWindow.style.left = `${left}px`;
            chatWindow.style.top = `${top}px`;
            chatWindow.style.right = 'auto';
            chatWindow.style.bottom = 'auto';
            chatWindow.classList.add('open');
            this.constrainWindowToViewport(chatWindow);
        } else if (chatWindow) {
            chatWindow.classList.remove('open');
        }
    }

    constrainWindowToViewport(windowElement) {
        const windowWidth = window.innerWidth;
        const windowHeight = window.innerHeight;
        const rect = windowElement.getBoundingClientRect();
        
        let newLeft = Math.max(ChatbotWidget.PADDING, 
            Math.min(rect.left, windowWidth - ChatbotWidget.WINDOW_WIDTH - ChatbotWidget.PADDING));
        let newTop = Math.max(ChatbotWidget.PADDING, 
            Math.min(rect.top, windowHeight - ChatbotWidget.WINDOW_HEIGHT - ChatbotWidget.PADDING));
        
        if (newLeft !== rect.left || newTop !== rect.top) {
            windowElement.style.left = `${newLeft}px`;
            windowElement.style.top = `${newTop}px`;
            this.windowPosition.x = windowWidth - newLeft - ChatbotWidget.WINDOW_WIDTH;
            this.windowPosition.y = windowHeight - newTop - ChatbotWidget.WINDOW_HEIGHT;
        }
    }

    calculateWindowPositionRelativeToBubble() {
        const windowWidth = window.innerWidth;
        const windowHeight = window.innerHeight;
        const bubbleLeft = windowWidth - this.bubblePosition.x - ChatbotWidget.BUBBLE_SIZE;
        const bubbleTop = windowHeight - this.bubblePosition.y - ChatbotWidget.BUBBLE_SIZE;
        
        // Try positions: left -> right -> above -> below
        let windowLeft = bubbleLeft - ChatbotWidget.WINDOW_WIDTH - ChatbotWidget.GAP;
        let windowTop = bubbleTop;
        
        if (windowLeft < ChatbotWidget.PADDING) {
            windowLeft = bubbleLeft + ChatbotWidget.BUBBLE_SIZE + ChatbotWidget.GAP;
            if (windowLeft + ChatbotWidget.WINDOW_WIDTH > windowWidth - ChatbotWidget.PADDING) {
                windowLeft = bubbleLeft;
                windowTop = bubbleTop - ChatbotWidget.WINDOW_HEIGHT - ChatbotWidget.GAP;
                if (windowTop < ChatbotWidget.PADDING) {
                    windowTop = bubbleTop + ChatbotWidget.BUBBLE_SIZE + ChatbotWidget.GAP;
                    if (windowTop + ChatbotWidget.WINDOW_HEIGHT > windowHeight - ChatbotWidget.PADDING) {
                        windowTop = Math.max(ChatbotWidget.PADDING, 
                            windowHeight - ChatbotWidget.WINDOW_HEIGHT - ChatbotWidget.PADDING);
                        windowLeft = Math.max(ChatbotWidget.PADDING, 
                            Math.min(windowLeft, windowWidth - ChatbotWidget.WINDOW_WIDTH - ChatbotWidget.PADDING));
                    }
                }
            }
        }
        
        this.windowPosition.x = windowWidth - windowLeft - ChatbotWidget.WINDOW_WIDTH;
        this.windowPosition.y = windowHeight - windowTop - ChatbotWidget.WINDOW_HEIGHT;
        
        return { left: windowLeft, top: windowTop };
    }

    setupDragAndDrop() {
        const toggle = document.getElementById('chatbotToggle');
        const container = document.getElementById('chatbotWidgetContainer');
        const avatarDraggable = document.getElementById('chatbotAvatarDraggable');
        const chatWindow = document.getElementById('chatbotWindow');
        
        if (!toggle || !container) return;

        let isDraggingBubble = false;
        let isTouchDraggingBubble = false;
        let startX = 0, startY = 0;
        let touchStartX = 0, touchStartY = 0;
        let touchHasDragged = false;

        // Mouse events - Bubble drag
        toggle.addEventListener('mousedown', (e) => {
            if (this.isOpen) return;
            this.startDrag(e, container, true);
            isDraggingBubble = true;
        });

        // Mouse events - Window drag
        if (avatarDraggable) {
            avatarDraggable.addEventListener('mousedown', (e) => {
                if (!this.isOpen) return;
                this.startDrag(e, chatWindow, false);
                isDraggingBubble = false;
            });
        }

        // Mouse move
        document.addEventListener('mousemove', (e) => {
            if (!this.isDragging) return;
            
            const deltaX = Math.abs(e.clientX - startX);
            const deltaY = Math.abs(e.clientY - startY);
            if (deltaX > ChatbotWidget.DRAG_THRESHOLD || deltaY > ChatbotWidget.DRAG_THRESHOLD) {
                this.hasDragged = true;
            }
            
            this.handleDrag(e.clientX, e.clientY, isDraggingBubble, container, chatWindow);
            e.preventDefault();
        });

        // Mouse up
        document.addEventListener('mouseup', () => {
            if (this.isDragging) {
                this.endDrag(isDraggingBubble, container, chatWindow);
                isDraggingBubble = false;
            }
        });

        // Touch events - Bubble drag
        toggle.addEventListener('touchstart', (e) => {
            if (this.isOpen) return;
            const touch = e.touches[0];
            touchStartX = touch.clientX;
            touchStartY = touch.clientY;
            this.startDrag(touch, container, true);
            isTouchDraggingBubble = true;
            touchHasDragged = false;
        });

        // Touch events - Window drag
        if (avatarDraggable) {
            avatarDraggable.addEventListener('touchstart', (e) => {
                if (!this.isOpen) return;
                const touch = e.touches[0];
                touchStartX = touch.clientX;
                touchStartY = touch.clientY;
                this.startDrag(touch, chatWindow, false);
                isTouchDraggingBubble = false;
                touchHasDragged = false;
            });
        }

        // Touch move
        document.addEventListener('touchmove', (e) => {
            if (!this.isDragging) return;
            
            const touch = e.touches[0];
            const deltaX = Math.abs(touch.clientX - touchStartX);
            const deltaY = Math.abs(touch.clientY - touchStartY);
            if (deltaX > ChatbotWidget.DRAG_THRESHOLD || deltaY > ChatbotWidget.DRAG_THRESHOLD) {
                touchHasDragged = true;
                this.hasDragged = true;
            }
            
            this.handleDrag(touch.clientX, touch.clientY, isTouchDraggingBubble, container, chatWindow);
            e.preventDefault();
        });

        // Touch end
        document.addEventListener('touchend', () => {
            if (this.isDragging) {
                this.endDrag(isTouchDraggingBubble, container, chatWindow);
                
                if (!touchHasDragged && isTouchDraggingBubble) {
                    this.toggleWidget();
                } else {
                    this.savePosition();
                }
                
                isTouchDraggingBubble = false;
            }
        });
    }

    startDrag(event, element, isBubble) {
        this.hasDragged = false;
        this.isDragging = true;
        
        const rect = element.getBoundingClientRect();
        this.dragOffset.x = event.clientX - rect.left;
        this.dragOffset.y = event.clientY - rect.top;
        
        if (isBubble) {
            element.classList.add('dragging');
        } else {
            document.getElementById('chatbotAvatarDraggable')?.classList.add('dragging');
            document.getElementById('chatbotWindow')?.classList.add('dragging');
        }
        
        event.preventDefault();
        event.stopPropagation();
    }

    handleDrag(clientX, clientY, isBubble, container, chatWindow) {
        const windowWidth = window.innerWidth;
        const windowHeight = window.innerHeight;
        let newLeft = clientX - this.dragOffset.x;
        let newTop = clientY - this.dragOffset.y;
        
        if (isBubble) {
            newLeft = Math.max(0, Math.min(newLeft, windowWidth - ChatbotWidget.BUBBLE_SIZE));
            newTop = Math.max(0, Math.min(newTop, windowHeight - ChatbotWidget.BUBBLE_SIZE));
            
            container.style.left = `${newLeft}px`;
            container.style.top = `${newTop}px`;
            container.style.right = 'auto';
            container.style.bottom = 'auto';
            
            this.bubblePosition.x = windowWidth - newLeft - ChatbotWidget.BUBBLE_SIZE;
            this.bubblePosition.y = windowHeight - newTop - ChatbotWidget.BUBBLE_SIZE;
        } else if (this.isOpen) {
            newLeft = Math.max(ChatbotWidget.PADDING, 
                Math.min(newLeft, windowWidth - ChatbotWidget.WINDOW_WIDTH - ChatbotWidget.PADDING));
            newTop = Math.max(ChatbotWidget.PADDING, 
                Math.min(newTop, windowHeight - ChatbotWidget.WINDOW_HEIGHT - ChatbotWidget.PADDING));
            
            if (chatWindow) {
                chatWindow.style.left = `${newLeft}px`;
                chatWindow.style.top = `${newTop}px`;
                chatWindow.style.right = 'auto';
                chatWindow.style.bottom = 'auto';
            }
        }
    }

    endDrag(isBubble, container, chatWindow) {
        this.isDragging = false;
        document.getElementById('chatbotToggle')?.classList.remove('dragging');
        document.getElementById('chatbotAvatarDraggable')?.classList.remove('dragging');
        document.getElementById('chatbotWindow')?.classList.remove('dragging');
        
        const windowWidth = window.innerWidth;
        const windowHeight = window.innerHeight;
        
        if (isBubble && container) {
            const rect = container.getBoundingClientRect();
            this.bubblePosition.x = windowWidth - rect.left - ChatbotWidget.BUBBLE_SIZE;
            this.bubblePosition.y = windowHeight - rect.top - ChatbotWidget.BUBBLE_SIZE;
        } else if (!isBubble && this.isOpen && chatWindow) {
            const rect = chatWindow.getBoundingClientRect();
            this.windowPosition.x = windowWidth - rect.left - ChatbotWidget.WINDOW_WIDTH;
            this.windowPosition.y = windowHeight - rect.top - ChatbotWidget.WINDOW_HEIGHT;
        }
        
        this.savePosition();
    }

    attachEventListeners() {
        const toggle = document.getElementById('chatbotToggle');
        const close = document.getElementById('chatbotClose');
        const send = document.getElementById('chatbotSend');
        const input = document.getElementById('chatbotInput');
        const quickButtons = document.querySelectorAll('.quick-btn');

        toggle?.addEventListener('click', () => {
            setTimeout(() => {
                if (!this.hasDragged && !this.isDragging) {
                    this.toggleWidget();
                }
                this.hasDragged = false;
            }, 10);
        });
        
        close?.addEventListener('click', () => this.toggleWidget());
        send?.addEventListener('click', () => this.sendMessage());
        input?.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.sendMessage();
        });

        quickButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                const message = btn.getAttribute('data-msg');
                input.value = message;
                this.sendMessage();
            });
        });
    }

    toggleWidget() {
        this.isOpen = !this.isOpen;
        const chatWindow = document.getElementById('chatbotWindow');
        
        if (this.isOpen) {
            this.calculateWindowPositionRelativeToBubble();
            chatWindow?.classList.add('open');
            this.updatePosition();
            
            setTimeout(() => {
                document.getElementById('chatbotInput')?.focus();
            }, 300);
        } else {
            chatWindow?.classList.remove('open');
        }
    }

    async sendMessage() {
        const input = document.getElementById('chatbotInput');
        const message = input?.value.trim();
        
        if (!message) return;

        this.addMessage(message, 'user');
        input.value = '';
        this.showTyping();

        input.disabled = true;
        const sendButton = document.getElementById('chatbotSend');
        sendButton.disabled = true;
        sendButton.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

        try {
            const contextPath = window.location.pathname.split('/')[1];
            const apiUrl = contextPath ? `/${contextPath}/api/chatbot/chat` : '/api/chatbot/chat';
            
            const response = await fetch(apiUrl, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: message })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const data = await response.json();
            this.hideTyping();
            input.disabled = false;
            sendButton.disabled = false;
            sendButton.innerHTML = '<span id="sendIcon">📤</span>';

            if (data.success) {
                this.addMessage(data.response, 'bot');
            } else {
                this.addMessage('Xin lỗi, đã có lỗi xảy ra: ' + (data.error || 'Unknown error'), 'bot', true);
            }
        } catch (error) {
            console.error('Error:', error);
            this.hideTyping();
            input.disabled = false;
            sendButton.disabled = false;
            sendButton.innerHTML = '<span id="sendIcon">📤</span>';
            this.addMessage('Xin lỗi, không thể kết nối đến server. Vui lòng thử lại sau! 😔', 'bot', true);
        }
    }

    addMessage(text, sender, isError = false) {
        const messagesContainer = document.getElementById('chatbotMessages');
        if (!messagesContainer) return;

        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender}`;
        
        const avatar = sender === 'bot' ? '🤖' : '👤';
        const bgClass = isError ? 'danger' : (sender === 'bot' ? 'light' : 'primary');
        
        messageDiv.innerHTML = `
            <div class="message-content ${sender === 'user' ? 'flex-row-reverse' : ''}">
                <div class="message-avatar">${avatar}</div>
                <div class="message-bubble ${bgClass}">
                    ${this.formatMessage(text)}
                </div>
            </div>
        `;
        
        messagesContainer.appendChild(messageDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    formatMessage(text) {
        return `<p class="mb-0">${text.replace(/\n/g, '<br>')}</p>`;
    }

    showTyping() {
        const messagesContainer = document.getElementById('chatbotMessages');
        if (!messagesContainer) return;

        const typing = document.createElement('div');
        typing.id = 'typingIndicator';
        typing.className = 'message bot typing';
        typing.innerHTML = `
            <div class="message-content">
                <div class="message-avatar">🤖</div>
                <div class="message-bubble light">
                    <span class="dot"></span>
                    <span class="dot"></span>
                    <span class="dot"></span>
                </div>
            </div>
        `;
        messagesContainer.appendChild(typing);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    hideTyping() {
        document.getElementById('typingIndicator')?.remove();
    }
}

// Initialize chatbot when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new ChatbotWidget();
});
