import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AuthService } from 'src/app/_service/auth.service';
import { StorageService } from 'src/app/_service/storage.service';

@Component({
  selector: 'app-login-page',
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.css'],
  providers: [MessageService]
})
export class LoginPageComponent implements OnInit {

  isSuccessful = false;
  isSignUpFailed = false;
  isLoggedIn = false;
  isLoginFailed = false;
  roles: string[] = [];
  errorMessage = '';

  // Variable for login error dialog
  displayErrorDialog = false;
  errorTitle = '';

  // Variable for successful registration dialog
  displayRegisterSuccessDialog = false;

  // Variable for forgot password dialog
  displayForgotPasswordDialog = false;
  forgotPasswordEmail: string = '';
  forgotPasswordUsername: string = '';
  resetPasswordSent = false;
  forgotPasswordStep = 1; // Step 1: Enter information, Step 2: Reset password, Step 3: Success
  newPassword: string = '';
  confirmPassword: string = '';

  loginForm: any = {
    username: null,
    password: null
  }

  registerForm: any = {
    username: null,
    email: null,
    password: null
  }

  constructor(
    private authService: AuthService,
    private storageService: StorageService,
    private messageService: MessageService,
    private router: Router
  ) { }

  ngOnInit(): void {
    // Kiểm tra xem có yêu cầu đăng nhập admin không
    const adminLoginRedirect = localStorage.getItem('adminLoginRedirect');

    if (adminLoginRedirect === 'true') {
      // Xóa flag redirect
      localStorage.removeItem('adminLoginRedirect');

      // Chuyển container sang tab đăng nhập nếu đang ở tab đăng ký
      document.getElementById('container')?.classList.remove("right-panel-active");

      // Có thể thêm thông báo cho người dùng biết
      this.messageService.add({
        severity: 'info',
        summary: 'Notice',
        detail: 'Please log in with your account'
      });
    }
  }

  // Phương thức đăng nhập chung cho cả user và admin
  login(): void {
    console.log('Phương thức login được gọi');

    // Kiểm tra dữ liệu đầu vào
    if (!this.validateLoginForm()) {
      console.log('Validation thất bại, dừng đăng nhập');
      return;
    }

    const { username, password } = this.loginForm;
    console.log('Login attempt with:', { username, password });

    this.authService.login(username, password).subscribe({
      next: (response) => {
        console.log('Login response:', response);
        // Lưu thông tin người dùng vào storage
        this.storageService.saveUser(response);
        this.isLoggedIn = true;
        this.isLoginFailed = false;
        this.roles = response.roles;
        console.log('User saved to storage:', this.storageService.getUser());

        // Đã đăng nhập thành công, luôn chuyển hướng đến trang home
        this.showSuccess("Login successful!");
        this.router.navigate(['/home']);
      },
      error: err => {
        console.error('Login error:', err);
        this.isLoggedIn = false;
        this.isLoginFailed = true;
        this.errorMessage = err.error?.message || 'Login failed';
        this.showError(this.errorMessage);
      }
    });
  }

  register(): void {
    console.log('Phương thức register được gọi');

    // Kiểm tra dữ liệu đầu vào
    if (!this.validateRegisterForm()) {
      console.log('Validation thất bại, dừng đăng ký');
      return;
    }

    const { username, email, password } = this.registerForm;
    console.log('Register attempt with:', { username, email, password });
    this.authService.register(username, email, password).subscribe({
      next: res => {
        console.log('Register response:', res);
        this.isSuccessful = true;
        this.isSignUpFailed = false;

        // Hiển thị dialog đăng ký thành công
        this.displayRegisterSuccessDialog = true;

        // Reset form đăng ký
        this.registerForm = {
          username: null,
          email: null,
          password: null
        };
      },
      error: err => {
        console.error('Register error:', err);
        this.isSuccessful = false;
        this.isSignUpFailed = true;
        this.errorMessage = err.error?.message || 'Đăng ký thất bại';
        this.showError(this.errorMessage);
      }
    })
  }

  loginFormChange() {
    document.getElementById('container')?.classList.remove("right-panel-active");
  }
  registerFormChange() {
    document.getElementById('container')?.classList.add("right-panel-active");
  }

  // Phương thức đóng dialog lỗi
  closeErrorDialog(): void {
    this.displayErrorDialog = false;
  }

  // Hiển thị dialog quên mật khẩu
  showForgotPasswordDialog(): void {
    this.displayForgotPasswordDialog = true;
    this.resetForgotPasswordForm();
  }

  // Đóng dialog quên mật khẩu
  closeForgotPasswordDialog(): void {
    this.displayForgotPasswordDialog = false;
    this.resetForgotPasswordForm();
  }

  // Reset form quên mật khẩu
  resetForgotPasswordForm(): void {
    this.forgotPasswordEmail = '';
    this.forgotPasswordUsername = '';
    this.newPassword = '';
    this.confirmPassword = '';
    this.forgotPasswordStep = 1;
  }

  // Kiểm tra thông tin quên mật khẩu
  verifyForgotPasswordInfo(): void {
    // Kiểm tra username đã nhập chưa
    if (!this.forgotPasswordUsername || this.forgotPasswordUsername.trim() === '') {
      this.showError('Please enter your username!');
      return;
    }

    // Kiểm tra email đã nhập chưa
    if (!this.forgotPasswordEmail || this.forgotPasswordEmail.trim() === '') {
      this.showError('Please enter your email!');
      return;
    }

    // Kiểm tra định dạng email
    if (!this.isValidEmail(this.forgotPasswordEmail)) {
      this.showError('Invalid email format!');
      return;
    }

    // Gọi API kiểm tra thông tin tài khoản và email
    // Mô phỏng xác thực thành công
    setTimeout(() => {
      // Chuyển sang bước 2: Đặt lại mật khẩu
      this.forgotPasswordStep = 2;
    }, 1000);
  }

  // Đặt lại mật khẩu
  resetPassword(): void {
    // Kiểm tra mật khẩu mới đã nhập chưa
    if (!this.newPassword || this.newPassword.trim() === '') {
      this.showError('Vui lòng nhập mật khẩu mới!');
      return;
    }

    // Kiểm tra xác nhận mật khẩu đã nhập chưa
    if (!this.confirmPassword || this.confirmPassword.trim() === '') {
      this.showError('Vui lòng xác nhận mật khẩu mới!');
      return;
    }

    // Kiểm tra mật khẩu mới và xác nhận mật khẩu có khớp nhau không
    if (this.newPassword !== this.confirmPassword) {
      this.showError('Xác nhận mật khẩu không khớp với mật khẩu mới!');
      return;
    }

    // Kiểm tra độ dài mật khẩu
    if (this.newPassword.length < 6) {
      this.showError('Mật khẩu phải có ít nhất 6 ký tự');
      return;
    }

    // Gọi API đặt lại mật khẩu
    // Mô phỏng đặt lại mật khẩu thành công
    setTimeout(() => {
      // Chuyển sang bước 3: Thành công
      this.forgotPasswordStep = 3;
    }, 1000);
  }

  // Đóng dialog quên mật khẩu và chuyển sang form đăng nhập
  closeForgotPasswordAndLogin(): void {
    this.displayForgotPasswordDialog = false;
    this.resetForgotPasswordForm();
    // Chuyển container sang tab đăng nhập
    document.getElementById('container')?.classList.remove("right-panel-active");
  }

  // Mô phỏng gửi yêu cầu quên mật khẩu
  submitForgotPassword(): void {
    this.resetPasswordSent = true;
  }

  showSuccess(text: string) {
    this.messageService.add({
      severity: 'success',
      summary: 'Success',
      detail: text,
      life: 1500
    });
  }

  showError(text: string) {
    this.messageService.add({
      severity: 'error',
      summary: 'Error',
      detail: text,
      life: 1500
    });
  }

  showWarn(text: string) {
    this.messageService.add({
      severity: 'warn',
      summary: 'Warning',
      detail: text,
      life: 1500
    });
  }

  // Phương thức đăng nhập bằng Google 
  simulateGoogleLogin(): void {
    // Bắt đầu đăng nhập bằng Google OAuth2
    this.authService.initiateGoogleLogin();
  }

  // Đóng dialog đăng ký thành công
  closeRegisterSuccessDialog(): void {
    this.displayRegisterSuccessDialog = false;
    // Chuyển sang giao diện đăng nhập
    document.getElementById('container')?.classList.remove("right-panel-active");
  }

  // Kiểm tra dữ liệu đăng nhập
  validateLoginForm(): boolean {
    // Kiểm tra username đã nhập chưa
    if (!this.loginForm.username || this.loginForm.username.trim() === '') {
      this.showError('Vui lòng nhập tên đăng nhập!');
      return false;
    }

    // Kiểm tra password đã nhập chưa
    if (!this.loginForm.password || this.loginForm.password.trim() === '') {
      this.showError('Vui lòng nhập mật khẩu!');
      return false;
    }

    return true;
  }

  // Kiểm tra dữ liệu đăng ký
  validateRegisterForm(): boolean {
    // Kiểm tra username đã nhập chưa
    if (!this.registerForm.username || this.registerForm.username.trim() === '') {
      this.showError('Vui lòng nhập tên đăng nhập!');
      return false;
    }

    // Kiểm tra email đã nhập chưa
    if (!this.registerForm.email || this.registerForm.email.trim() === '') {
      this.showError('Vui lòng nhập email!');
      return false;
    }

    // Kiểm tra định dạng email
    if (!this.isValidEmail(this.registerForm.email)) {
      this.showError('Email không hợp lệ!');
      return false;
    }

    // Kiểm tra password đã nhập chưa
    if (!this.registerForm.password || this.registerForm.password.trim() === '') {
      this.showError('Vui lòng nhập mật khẩu!');
      return false;
    }

    // Kiểm tra độ dài mật khẩu
    if (this.registerForm.password.length < 6) {
      this.showError('Mật khẩu phải có ít nhất 6 ký tự!');
      return false;
    }

    return true;
  }

  // Kiểm tra định dạng email
  isValidEmail(email: string): boolean {
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
    return emailPattern.test(email);
  }
}
