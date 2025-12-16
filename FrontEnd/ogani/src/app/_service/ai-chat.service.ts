import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
    providedIn: 'root',
})
export class AiChatService {
    private readonly baseUrl = `${environment.apiUrl}/ai-prompt/ask`;

    constructor(private http: HttpClient) { }

    ask(prompt: string): Observable<string> {
        // Backend expects raw text (String) body and returns plain text
        return this.http.post(this.baseUrl, prompt, {
            responseType: 'text',
        });
    }
}


