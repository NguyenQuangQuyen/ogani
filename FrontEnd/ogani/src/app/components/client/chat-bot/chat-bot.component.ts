import { Component } from '@angular/core';
import { AiChatService } from 'src/app/_service/ai-chat.service';

interface ChatMessage {
    from: 'user' | 'bot';
    text: string;
}

@Component({
    selector: 'app-chat-bot',
    templateUrl: './chat-bot.component.html',
    styleUrls: ['./chat-bot.component.css'],
})
export class ChatBotComponent {
    messages: ChatMessage[] = [
        {
            from: 'bot',
            text: 'Xin chào , tôi có thể hỗ trợ gì cho bạn hôm nay?',
        },
    ];

    input = '';
    isLoading = false;

    constructor(private aiChatService: AiChatService) { }

    send(): void {
        const trimmed = this.input.trim();
        if (!trimmed || this.isLoading) {
            return;
        }

        // Push user message
        this.messages.push({
            from: 'user',
            text: trimmed,
        });

        this.input = '';
        this.isLoading = true;

        // Call backend
        this.aiChatService.ask(trimmed).subscribe({
            next: (response) => {
                this.animateBotResponse(response || 'Xin lỗi, tôi không nhận được phản hồi.');
            },
            error: () => {
                this.isLoading = false;
                this.messages.push({
                    from: 'bot',
                    text: 'Oops! Đã có lỗi xảy ra. Vui lòng thử lại sau.',
                });
            },
        });
    }

    private animateBotResponse(fullText: string): void {
        this.isLoading = false;
        if (!fullText) {
            return;
        }

        const botMessage: ChatMessage = {
            from: 'bot',
            text: '',
        };
        this.messages.push(botMessage);

        const chars = Array.from(fullText);
        let index = 0;

        const interval = setInterval(() => {
            if (index >= chars.length) {
                clearInterval(interval);
                return;
            }
            botMessage.text += chars[index];
            index += 1;
        }, 20); // small delay to simulate streaming
    }
}


