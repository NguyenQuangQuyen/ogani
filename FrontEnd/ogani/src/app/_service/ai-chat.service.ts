import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { ChatMessage } from '../_class/chat-message';
import { ChatSession } from '../_class/chat-session';

@Injectable({
    providedIn: 'root',
})
export class AiChatService {
    private readonly baseUrl = `${environment.apiUrl}/chat`;

    constructor(private http: HttpClient) { }

    ask(prompt: string): Observable<string> {
        return this.http.post(`${environment.apiUrl}/ai-prompt/ask`, prompt, {
            responseType: 'text',
        });
    }

    /**
     * Gọi chatbot mới với database integration
     */
    chat(prompt: string): Observable<any> {
        return this.http.post(`${environment.apiUrl}/ai-prompt/chat`, prompt, {
            responseType: 'json',
        });
    }

    createSession(): Observable<ChatSession> {
        return this.http.post<ChatSession>(`${this.baseUrl}/sessions`, {});
    }

    getUserSessions(): Observable<ChatSession[]> {
        return this.http.get<ChatSession[]>(`${this.baseUrl}/sessions`);
    }

    getSessionMessages(sessionId: number): Observable<ChatMessage[]> {
        return this.http.get<ChatMessage[]>(`${this.baseUrl}/sessions/${sessionId}/messages`);
    }

    appendMessages(sessionId: number, messages: ChatMessage[]): Observable<void> {
        return this.http.post<void>(`${this.baseUrl}/sessions/${sessionId}/append`, messages);
    }

    deleteSession(sessionId: number): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}/sessions/${sessionId}`);
    }
}
