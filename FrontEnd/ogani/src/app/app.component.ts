import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent {
  title = 'ogani';
  showAiChat = false;

  toggleAiChat(): void {
    this.showAiChat = !this.showAiChat;
  }
}
