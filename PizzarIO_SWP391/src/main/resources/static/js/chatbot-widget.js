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
                            <span class="chatbot-icon">🍕</span>
                            <div>
                                <h3>PizzarIO Assistant</h3>
                                <p>Chúng tôi luôn sẵn sàng giúp bạn!</p>
                            </div>
                        </div>
                        <button class="chatbot-close" id="chatbotClose">×</button>
                    </div>
                    
                    <div class="chatbot-messages" id="chatbotMessages">
                        <div class="message bot">
                            <div class="message-avatar">🤖</div>
                            <div class="message-content">
                                Xin chào! Tôi là trợ lý ảo của PizzarIO. Tôi có thể giúp bạn về menu, đặt bàn, hoặc bất kỳ thông tin nào về nhà hàng. Hãy hỏi tôi nhé! 😊
                            </div>
                        </div>
                    </div>
                    
                    <div class="chatbot-input">
                        <input type="text" id="chatbotInput" placeholder="Nhập tin nhắn..." />
                        <button id="chatbotSend">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                                <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
                            </svg>
                        </button>
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

        toggle.addEventListener('click', () => this.toggleWidget());
        close.addEventListener('click', () => this.toggleWidget());
        send.addEventListener('click', () => this.sendMessage());
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.sendMessage();
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

        try {
            // Tự động lấy context path từ URL hiện tại
            const contextPath = window.location.pathname.split('/')[1];
            const apiUrl = contextPath ? `/${contextPath}/api/chatbot/chat` : '/api/chatbot/chat';
            
            console.log('🚀 Sending request to:', apiUrl);
            console.log('📝 Message:', message);
            
            const response = await fetch(apiUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ message: message })
            });

            console.log('📥 Response status:', response.status);
            
            const data = await response.json();
            console.log('📦 Response data:', data);
            
            this.hideTyping();

            if (data.success) {
                this.addMessage(data.response, 'bot');
            } else {
                this.addMessage('Xin lỗi, đã có lỗi xảy ra: ' + (data.error || 'Unknown error'), 'bot');
                console.error('❌ Error from server:', data.error);
            }
        } catch (error) {
            console.error('❌ Fetch error:', error);
            this.hideTyping();
            this.addMessage('Không thể kết nối đến server. Error: ' + error.message, 'bot');
        }
    }

    addMessage(text, sender) {
        const messagesContainer = document.getElementById('chatbotMessages');
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${sender}`;
        
        const avatar = sender === 'bot' ? '🤖' : '👤';
        
        messageDiv.innerHTML = `
            <div class="message-avatar">${avatar}</div>
            <div class="message-content">${this.escapeHtml(text)}</div>
        `;
        
        messagesContainer.appendChild(messageDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    showTyping() {
        const messagesContainer = document.getElementById('chatbotMessages');
        const typing = document.createElement('div');
        typing.id = 'typingIndicator';
        typing.className = 'message bot typing';
        typing.innerHTML = `
            <div class="message-avatar">🤖</div>
            <div class="message-content">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
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

