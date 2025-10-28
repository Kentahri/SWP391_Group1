class ChatbotWidget {
    constructor() {
        this.isOpen = false;
        this.messages = [];
        this.init();
    }

    init() {
        this.createWidget();
        this.attachEventListeners();
    }

    createWidget() {
        const widget = document.createElement('div');
        widget.innerHTML = `
            <div class="chatbot-widget">
                <button class="chatbot-toggle" id="chatbotToggle">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="white">
                        <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H6l-2 2V4h16v12z"/>
                    </svg>
                </button>
                
                <div class="chatbot-window" id="chatbotWindow">
                    <div class="chatbot-header">
                        <div class="chatbot-title">
                            <span class="chatbot-icon">🤖</span>
                            <div>
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
                            <button class="quick-btn" data-msg="món giá rẻ nhất">
                                💰 Giá rẻ
                            </button>
                            <button class="quick-btn" data-msg="món đắt nhất">
                                💎 Cao cấp
                            </button>
                            <button class="quick-btn" data-msg="khuyến mãi">
                                🎉 Khuyến mãi
                            </button>
                            <button class="quick-btn" data-msg="bán chạy">
                                🔥 Bán chạy
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        document.body.appendChild(widget);
    }

    attachEventListeners() {
        const toggle = document.getElementById('chatbotToggle');
        const close = document.getElementById('chatbotClose');
        const send = document.getElementById('chatbotSend');
        const input = document.getElementById('chatbotInput');
        const quickButtons = document.querySelectorAll('.quick-btn');

        toggle.addEventListener('click', () => this.toggleWidget());
        close.addEventListener('click', () => this.toggleWidget());
        send.addEventListener('click', () => this.sendMessage());
        
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.sendMessage();
        });

        // Quick button handlers
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
        const window = document.getElementById('chatbotWindow');
        const toggle = document.getElementById('chatbotToggle');
        
        if (this.isOpen) {
            window.classList.add('open');
            toggle.classList.add('open');
        } else {
            window.classList.remove('open');
            toggle.classList.remove('open');
        }
    }

    async sendMessage() {
        const input = document.getElementById('chatbotInput');
        const message = input.value.trim();
        
        if (!message) return;

        // Add user message
        this.addMessage(message, 'user');
        input.value = '';
        
        // Show typing indicator
        this.showTyping();

        // Disable input while waiting
        input.disabled = true;
        const sendButton = document.getElementById('chatbotSend');
        sendButton.disabled = true;
        sendButton.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

        try {
            // Get context path from window.location.pathname
            const contextPath = window.location.pathname.split('/')[1];
            const apiUrl = contextPath ? `/${contextPath}/api/chatbot/chat` : '/api/chatbot/chat';
            
            console.log('📤 Sending message to:', apiUrl);
            console.log('📝 Message:', message);
            
            const response = await fetch(apiUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ message: message })
            });

            console.log('📥 Response status:', response.status);
            console.log('📥 Response ok:', response.ok);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const data = await response.json();
            console.log('📦 Response data:', data);
            
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
            console.error('❌ Error:', error);
            this.hideTyping();
            input.disabled = false;
            sendButton.disabled = false;
            sendButton.innerHTML = '<span id="sendIcon">📤</span>';
            this.addMessage('Xin lỗi, không thể kết nối đến server. Vui lòng thử lại sau! 😔', 'bot', true);
        }
    }

    addMessage(text, sender, isError = false) {
        const messagesContainer = document.getElementById('chatbotMessages');
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
        // Convert newlines to HTML breaks
        let formatted = text.replace(/\n/g, '<br>');
        
        // Format lists
        formatted = formatted.replace(/• /g, '• ');
        
        return `<p class="mb-0">${formatted}</p>`;
    }

    showTyping() {
        const messagesContainer = document.getElementById('chatbotMessages');
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
        const typing = document.getElementById('typingIndicator');
        if (typing) typing.remove();
    }
}

// Initialize chatbot when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new ChatbotWidget();
});
