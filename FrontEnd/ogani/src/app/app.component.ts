import { Component, HostListener } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent {
  title = 'ogani';
  showAiChat = false;

  toggleAiChat(event: Event): void {
    event.stopPropagation();
    this.showAiChat = !this.showAiChat;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.showAiChat) return;

    const target = event.target as HTMLElement;
    const clickedInside = target.closest('.ai-chat-panel') || target.closest('.ai-chat-fab');

    if (!clickedInside) {
      this.showAiChat = false;
    }
  }
}
