import { Injectable } from '@angular/core';

const USER_KEY = 'auth-user';
const TOKEN_KEY = 'auth-token';

@Injectable({
  providedIn: 'root'
})
export class StorageService {

  constructor() { }

  clean(): void {
    window.sessionStorage.clear();

    // Xóa dữ liệu ảnh đại diện lưu trong localStorage nếu có
    localStorage.removeItem('userProfileImage');
    console.log('Cleaned all user data from storage including profile image');
  }

  saveUser(user: any): void {
    console.log('Saving user to storage:', user);
    window.sessionStorage.removeItem(USER_KEY);
    window.sessionStorage.setItem(USER_KEY, JSON.stringify(user));

    // Lưu token nếu có
    if (user && user.token) {
      this.saveToken(user.token);
    }

    // Lưu ảnh đại diện từ Google (nếu có) hoặc từ nguồn khác
    if (user && user.picture) {
      // Đây là URL ảnh từ Google OAuth
      let googleImageUrl = user.picture;

      // Loại bỏ timestamp từ URL nếu đã có (tránh trùng lặp)
      if (googleImageUrl.indexOf('?') > 0) {
        googleImageUrl = googleImageUrl.substring(0, googleImageUrl.indexOf('?'));
      }

      console.log('Saving Google profile image URL to storage:', googleImageUrl);
      localStorage.setItem('userProfileImage', googleImageUrl);

      // Lưu thêm vào user object để đồng bộ
      user.profileImage = googleImageUrl;
      window.sessionStorage.setItem(USER_KEY, JSON.stringify(user));
    } else if (user && user.profileImage) {
      // Đây là URL ảnh từ hệ thống
      console.log('Saving local profile image URL to storage:', user.profileImage);
      localStorage.setItem('userProfileImage', user.profileImage);
    }
  }

  saveToken(token: string): void {
    window.sessionStorage.removeItem(TOKEN_KEY);
    window.sessionStorage.setItem(TOKEN_KEY, token);
  }

  getToken(): string | null {
    return window.sessionStorage.getItem(TOKEN_KEY);
  }

  getUser(): any {
    const user = window.sessionStorage.getItem(USER_KEY);
    if (user) {
      return JSON.parse(user);
    }
    return null;
  }

  isLoggedIn(): boolean {
    const user = window.sessionStorage.getItem(USER_KEY);
    if (user) {
      const userObj = JSON.parse(user);
      return userObj && userObj.username;
    }
    return false;
  }
}
