import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-contact',
  templateUrl: './contact.component.html',
  styleUrls: ['./contact.component.css'],
  providers: [MessageService]
})
export class ContactComponent implements OnInit {

  contactForm = {
    name: '',
    email: '',
    message: ''
  };

  // Mảng chứa các liên kết mạng xã hội
  socialLinks = [
    {
      name: 'facebook',
      url: 'https://www.facebook.com/quyen.quy.35380/',
      icon: 'fa fa-facebook',
      label: 'Facebook',
      logoSrc: 'assets/image/facebook-logo.png' // Đường dẫn đến logo Facebook
    },
    {
      name: 'instagram',
      url: 'https://www.instagram.com/quyenquy053/',
      icon: 'fa fa-instagram',
      label: 'Instagram',
      logoSrc: 'assets/image/instagram-logo.png' // Đường dẫn đến logo Instagram
    },
    {
      name: 'youtube',
      url: 'https://www.youtube.com/@Matcha-Turtle-203',
      icon: 'fa fa-youtube-play',
      label: 'YouTube',
      logoSrc: 'assets/image/youtube-logo.png' // Đường dẫn đến logo YouTube
    },
    {
      name: 'tiktok',
      url: 'https://www.tiktok.com/@user708j53dnqe?lang=vi-VN',
      icon: 'fa fa-music', // Font Awesome 5 không có biểu tượng tiktok, dùng biểu tượng music thay thế
      label: 'TikTok',
      logoSrc: 'assets/image/tiktok-logo.png' // Đường dẫn đến logo TikTok
    },
    // {
    //   name: 'zalo',
    //   url: 'https://zalo.me/your-number', // Thay đổi link Zalo của bạn ở đây (https://zalo.me/0123456789)
    //   icon: 'fa fa-comment', // Không có biểu tượng Zalo trong Font Awesome, dùng biểu tượng comment thay thế
    //   label: 'Zalo',
    //   logoSrc: 'assets/image/social/zalo-logo.png' // Đường dẫn đến logo Zalo
    // }
  ];

  constructor(private messageService: MessageService) { }

  ngOnInit(): void {
  }

  sendMessage(): void {
    // Kiểm tra form
    if (!this.contactForm.name.trim()) {
      this.showError('Please enter your name!');
      return;
    }

    if (!this.contactForm.email.trim()) {
      this.showError('Please enter your email!');
      return;
    }

    // Kiểm tra định dạng email
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailPattern.test(this.contactForm.email)) {
      this.showError('Email không đúng định dạng!');
      return;
    }

    if (!this.contactForm.message.trim()) {
      this.showError('Please enter your message content!');
      return;
    }

    // Trong thực tế, đây là nơi gửi dữ liệu đến backend
    // this.contactService.sendMessage(this.contactForm).subscribe({...})

    // Hiển thị thông báo thành công
    this.showSuccess('Tin nhắn của bạn đã được gửi thành công!');

    // Reset form
    this.contactForm = {
      name: '',
      email: '',
      message: ''
    };
  }

  showSuccess(text: string) {
    this.messageService.add({
      severity: 'success',
      summary: 'Thành công',
      detail: text,
      life: 5000 // Hiển thị trong 5 giây
    });
  }

  showError(text: string) {
    this.messageService.add({
      severity: 'error',
      summary: 'Lỗi',
      detail: text,
      life: 5000 // Hiển thị trong 5 giây
    });
  }
}
