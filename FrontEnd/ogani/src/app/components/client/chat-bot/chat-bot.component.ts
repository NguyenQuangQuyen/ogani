import { Component, OnInit, HostListener, OnDestroy } from '@angular/core';
import { AiChatService } from 'src/app/_service/ai-chat.service';
import { TokenStorageService } from 'src/app/_service/token-storage.service';
import { ChatSession } from 'src/app/_class/chat-session';
import { ChatMessage } from 'src/app/_class/chat-message';
import { Observable, of } from 'rxjs';
import { switchMap, catchError, map } from 'rxjs/operators';

const STORAGE_KEY_PREFIX = 'chat_unsaved_';

@Component({
  selector: 'app-chat-bot',
  templateUrl: './chat-bot.component.html',
  styleUrls: ['./chat-bot.component.css']
})
export class ChatBotComponent implements OnInit, OnDestroy {
    sessions: ChatSession[] = [];
    currentSessionId: number | null = null;
    messages: ChatMessage[] = [];
    input = '';
    isLoading = false;
    username = '';
    isSidebarVisible = false; // For mobile sidebar toggle

    constructor(
        private aiChatService: AiChatService,
        private tokenStorageService: TokenStorageService
    ) { }

    ngOnInit(): void {
        const user = this.tokenStorageService.getUser();
        this.username = user ? user.username : '';

        this.loadSessions();
        if (this.username) {
            this.restoreFromStorage();
        } else {
            this.createNewChat(false);
        }
    }

    ngOnDestroy(): void {
    }

    toggleSidebar(): void {
        this.isSidebarVisible = !this.isSidebarVisible;
    }

    closeSidebar(): void {
        this.isSidebarVisible = false;
    }

    @HostListener('window:beforeunload', ['$event'])
    unloadHandler(event: Event) {
        this.saveToStorage();
    }

    deleteSession(session: ChatSession, event: Event): void {
        event.stopPropagation();
        if (confirm('Bạn có chắc chắn muốn xóa cuộc trò chuyện này không?')) {
            this.aiChatService.deleteSession(session.id).subscribe(() => {
                this.sessions = this.sessions.filter(s => s.id !== session.id);
                if (this.currentSessionId === session.id) {
                    this.createNewChat(false);
                    this.clearStorage();
                }
            });
        }
    }

    loadSessions(): void {
        this.aiChatService.getUserSessions().subscribe({
            next: (data) => {
                this.sessions = data;
            },
            error: (err) => console.error('Error loading sessions:', err)
        });
    }

    private getStorageKeys() {
        return {
            id: `${STORAGE_KEY_PREFIX}session_id_${this.username}`,
            msgs: `${STORAGE_KEY_PREFIX}messages_${this.username}`
        };
    }

    restoreFromStorage(): void {
        const keys = this.getStorageKeys();
        const storedId = localStorage.getItem(keys.id);
        const storedMsgs = localStorage.getItem(keys.msgs);

        if (storedMsgs) {
            try {
                this.messages = JSON.parse(storedMsgs);
                if (storedId && storedId !== 'null') {
                    this.currentSessionId = Number(storedId);
                } else {
                    this.currentSessionId = null;
                }
            } catch (e) {
                console.error('Error parsing stored messages', e);
                this.createNewChat(false);
            }
        } else {
            this.createNewChat(false);
        }
    }

    saveToStorage(): void {
        if (!this.username) return;
        const keys = this.getStorageKeys();
        localStorage.setItem(keys.id, String(this.currentSessionId));
        localStorage.setItem(keys.msgs, JSON.stringify(this.messages));
    }

    clearStorage(): void {
        if (!this.username) return;
        const keys = this.getStorageKeys();
        localStorage.removeItem(keys.id);
        localStorage.removeItem(keys.msgs);
    }

    createNewChat(syncFirst: boolean = true): void {
        if (syncFirst) {
            this.syncCurrentSession().subscribe(() => {
                this.resetChat();
            });
        } else {
            this.resetChat();
        }
    }

    private resetChat(): void {
        this.currentSessionId = null;
        this.messages = [{
            id: 0,
            sender: 'bot',
            content: 'Xin chào, tôi có thể hỗ trợ gì cho bạn hôm nay?',
            createdAt: new Date().toISOString()
        }];
        this.saveToStorage();
    }

    selectSession(session: ChatSession): void {
        if (this.currentSessionId === session.id) return;

        this.syncCurrentSession().subscribe(() => {
            this.currentSessionId = session.id;
            this.isLoading = true;
            this.messages = [];
            this.clearStorage();

            this.aiChatService.getSessionMessages(session.id).subscribe({
                next: (msgs) => {
                    this.messages = msgs;
                    this.isLoading = false;
                    this.saveToStorage();
                },
                error: (err) => {
                    console.error('Error loading messages:', err);
                    this.isLoading = false;
                }
            });
        });
    }

    syncCurrentSession(): Observable<void> {
        // Only sync if user is logged in
        if (!this.username) {
            return of(void 0);
        }

        const unsaved = this.messages.filter(m => m.id < 0);
        if (unsaved.length === 0) return of(void 0);

        if (!this.currentSessionId) {
            return this.aiChatService.createSession().pipe(
                switchMap(session => {
                    this.currentSessionId = session.id;
                    this.sessions.unshift(session);
                    return this.aiChatService.appendMessages(session.id, unsaved);
                }),
                map(() => void 0),
                catchError(err => {
                    console.error('Sync failed', err);
                    return of(void 0);
                })
            );
        } else {
            return this.aiChatService.appendMessages(this.currentSessionId, unsaved).pipe(
                map(() => void 0),
                catchError(err => {
                    console.error('Sync failed', err);
                    return of(void 0);
                })
            );
        }
    }

    send(): void {
        const trimmed = this.input.trim();
        if (!trimmed || this.isLoading) return;

        const tempUserMsg: ChatMessage = {
            id: -Date.now(),
            sender: 'user',
            content: trimmed,
            createdAt: new Date().toISOString()
        };
        this.messages.push(tempUserMsg);
        this.input = '';
        this.isLoading = true;
        this.saveToStorage();

        // Sync immediately to create session and save user message
        this.syncCurrentSession().subscribe({
            next: () => {
                this.loadSessions();
                if (this.currentSessionId) {
                    this.aiChatService.getSessionMessages(this.currentSessionId).subscribe({
                        next: (msgs) => {
                            this.messages = msgs;
                            this.callAi(trimmed);
                        },
                        error: () => this.callAi(trimmed)
                    });
                } else {
                    this.callAi(trimmed);
                }
            },
            error: () => this.callAi(trimmed)
        });
    }

    callAi(prompt: string): void {
        this.aiChatService.ask(prompt).subscribe({
            next: (response) => {
                const botMsg: ChatMessage = {
                    id: -(Date.now() + 1),
                    sender: 'bot',
                    content: response,
                    createdAt: new Date().toISOString()
                };
                this.animateBotResponse(botMsg);
            },
            error: () => {
                this.isLoading = false;
                const errorMsg: ChatMessage = {
                    id: -(Date.now() + 1),
                    sender: 'bot',
                    content: 'Oops! Đã có lỗi xảy ra. Vui lòng thử lại sau.',
                    createdAt: new Date().toISOString()
                };
                this.messages.push(errorMsg);
                this.saveToStorage();
            }
        });
    }

    private animateBotResponse(msg: ChatMessage): void {
        this.isLoading = false;
        const displayMsg: ChatMessage = { ...msg, content: '' };
        this.messages.push(displayMsg);

        const chars = Array.from(msg.content);
        let index = 0;

        const interval = setInterval(() => {
            if (index >= chars.length) {
                clearInterval(interval);
                this.saveToStorage();
                this.syncAndRefresh();
                return;
            }
            displayMsg.content += chars[index];
            index += 1;
        }, 10);
    }

    syncAndRefresh(): void {
        this.syncCurrentSession().subscribe(() => {
            this.loadSessions();
            if (this.currentSessionId) {
                this.aiChatService.getSessionMessages(this.currentSessionId).subscribe({
                    next: (msgs) => {
                        this.messages = msgs;
                        this.saveToStorage();
                    },
                    error: (err) => console.error('Error reloading messages:', err)
                });
            }
        });
    }
}
