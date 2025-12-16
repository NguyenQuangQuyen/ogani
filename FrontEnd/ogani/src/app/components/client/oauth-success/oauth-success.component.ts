import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AuthService } from 'src/app/_service/auth.service';
import { StorageService } from 'src/app/_service/storage.service';

@Component({
    selector: 'app-oauth-success',
    templateUrl: './oauth-success.component.html',
    styleUrls: ['./oauth-success.component.css'],
    providers: [MessageService]
})
export class OAuthSuccessComponent implements OnInit {
    loading = true;
    error = false;
    errorMessage = '';

    constructor(
        private authService: AuthService,
        private storageService: StorageService,
        private messageService: MessageService,
        private router: Router
    ) { }

    ngOnInit(): void {
        // Khi component được khởi tạo, lấy thông tin người dùng từ OAuth2
        this.getOAuth2UserInfo();
    }

    getOAuth2UserInfo(): void {
        this.authService.getOAuth2UserInfo().subscribe({
            next: (response) => {
                console.log('OAuth2 success response:', response);
                // Lưu thông tin người dùng vào storage
                this.storageService.saveUser(response);

                // Hiển thị thông báo thành công
                this.messageService.add({
                    severity: 'success',
                    summary: 'Login successful',
                    detail: 'Successfully logged in with Google!'
                });

                this.loading = false;

                // Chuyển hướng người dùng đến trang chính
                setTimeout(() => {
                    this.router.navigate(['/home']);
                }, 1500);
            },
            error: (err) => {
                console.error('OAuth2 success error:', err);
                this.loading = false;
                this.error = true;

                // Xử lý các loại lỗi cụ thể
                if (err.status === 404) {
                    this.errorMessage = 'Account not found. Please try again.';
                } else if (err.status === 401) {
                    this.errorMessage = 'Login session has expired. Please log in again.';
                } else if (err.status === 500) {
                    this.errorMessage = 'System error. Please try again later.';
                } else {
                    this.errorMessage = err.error?.message || 'Login failed';
                }

                // Hiển thị thông báo lỗi
                this.messageService.add({
                    severity: 'error',
                    summary: 'Login Failed',
                    detail: this.errorMessage
                });

                // Chuyển hướng người dùng đến trang đăng nhập sau 3 giây
                setTimeout(() => {
                    this.router.navigate(['/login']);
                }, 3000);
            }
        });
    }
} 