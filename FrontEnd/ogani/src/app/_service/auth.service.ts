import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

const AUTH_API = 'https://hgr0a62zxby.sn.mynetname.net:2003/api/auth/';
const USER_API = 'https://hgr0a62zxby.sn.mynetname.net:2003/api/user/';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
};

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  constructor(private http: HttpClient) {}

  register(username: string, email: string, password: string): Observable<any> {
    return this.http.post(
      AUTH_API + 'register',
      {
        username,
        email,
        password,
      },
      httpOptions
    );
  }

  login(username: string, password: string): Observable<any> {
    return this.http.post(
      AUTH_API + 'login',
      {
        username,
        password,
      },
      httpOptions
    );
  }

  // Phương thức đăng nhập với Google
  loginWithGoogle(idToken: string): Observable<any> {
    return this.http.post(
      AUTH_API + 'google-signin',
      {
        idToken,
      },
      httpOptions
    );
  }

  // Method to get user information after successful OAuth2 login
  getOAuth2UserInfo(): Observable<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Accept: 'application/json',
    });

    return this.http.get(AUTH_API + 'current-user', {
      headers: headers,
      withCredentials: true,
    });
  }

  // Khởi tạo đăng nhập bằng Google OAuth2
  initiateGoogleLogin(): void {
    window.location.href = 'https://hgr0a62zxby.sn.mynetname.net:2003/oauth2/authorization/google';
  }

  logout(): Observable<any> {
    return this.http.post(
      AUTH_API + 'logout',
      {},
      {
        headers: httpOptions.headers,
        withCredentials: true,
      }
    );
  }

  // Phương thức quên mật khẩu (gửi yêu cầu đặt lại)
  forgotPassword(email: string): Observable<any> {
    return this.http.post(
      AUTH_API + 'forgot-password',
      {
        email,
      },
      httpOptions
    );
  }

  // Kiểm tra email có khớp với tài khoản không
  verifyEmailForUser(username: string, email: string): Observable<any> {
    return this.http.post(
      AUTH_API + 'verify-email',
      {
        username,
        email,
      },
      httpOptions
    );
  }

  // Đặt lại mật khẩu mới
  resetPassword(
    username: string,
    email: string,
    newPassword: string
  ): Observable<any> {
    return this.http.post(
      AUTH_API + 'reset-password',
      {
        username,
        email,
        newPassword,
      },
      httpOptions
    );
  }

  // Phương thức kiểm tra vai trò hiện tại của người dùng từ server
  getCurrentUserRoles(): Observable<any> {
    return this.http.get(USER_API + 'current-user-info', httpOptions);
  }

  // Phương thức làm mới token khi vai trò thay đổi
  refreshToken(): Observable<any> {
    return this.http.post(AUTH_API + 'refresh-token', {}, httpOptions);
  }
}
