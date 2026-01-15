import { HttpHeaders, HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

const USER_API = `${environment.apiUrl}/user/`;
const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
};

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(private http: HttpClient) {}
  ngOnInit(): void {}

  getUser(username: string): Observable<any> {
    let params = new HttpParams();
    params = params.append('username', username);
    return this.http.get(USER_API, { params: params });
  }

  updateProfile(
    username: string,
    firstname: string,
    lastname: string,
    email: string,
    country: string,
    state: string,
    address: string,
    phone: string
  ): Observable<any> {
    return this.http.put(
      USER_API + 'update',
      { username, firstname, lastname, email, country, state, address, phone },
      httpOptions
    );
  }

  changePassword(
    username: string,
    oldPassword: string,
    newPassword: string
  ): Observable<any> {
    return this.http.put(
      USER_API + 'password',
      { username, oldPassword, newPassword },
      httpOptions
    );
  }

  // Lấy danh sách tất cả người dùng
  getAllUsers(): Observable<any> {
    // Thêm timestamp để ngăn cache
    const timestamp = new Date().getTime();
    return this.http.get(`${USER_API}all?t=${timestamp}`, httpOptions);
  }

  // Cập nhật vai trò (role) của người dùng
  updateUserRole(username: string, roles: string[]): Observable<any> {
    return this.http.put(USER_API + 'role', { username, roles }, httpOptions);
  }

  // Xóa người dùng
  deleteUser(username: string): Observable<any> {
    console.log('Sending delete request for user:', username);
    // Thêm timestamp để ngăn cache
    const timestamp = new Date().getTime();
    return this.http.delete(
      `${USER_API}${username}?t=${timestamp}`,
      httpOptions
    );
  }

  // Upload ảnh đại diện
  uploadProfileImage(username: string, imageFile: File): Observable<any> {
    // Log thông tin file trước khi upload
    console.log(
      'Preparing to upload profile image:',
      imageFile.name,
      'size:',
      imageFile.size,
      'type:',
      imageFile.type
    );

    const formData = new FormData();
    formData.append('file', imageFile);
    formData.append('username', username);

    // Thêm timestamp để ngăn cache
    const timestamp = new Date().getTime();
    return this.http.post(`${USER_API}upload-avatar?t=${timestamp}`, formData);
  }
}
