import { Component, OnInit } from '@angular/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { UserService } from 'src/app/_service/user.service';

@Component({
  selector: 'app-account',
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class AccountComponent implements OnInit {
  users: any[] = [];
  selectedUser: any = null;

  displayEditDialog: boolean = false;
  displayDeleteDialog: boolean = false;

  availableRoles: any[] = [
    { name: 'User', code: 'ROLE_USER' },
    { name: 'Admin', code: 'ROLE_ADMIN' }
  ];

  selectedRoles: string[] = [];

  constructor(
    private userService: UserService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) { }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers() {
    // Thêm timestamp để đảm bảo không bị cache
    const timestamp = new Date().getTime();

    this.userService.getAllUsers().subscribe({
      next: (data) => {
        // Lọc ra các tài khoản đã xóa nếu vẫn còn
        this.users = data.filter((user: any) =>
          !user.username.startsWith('DELETED_')
        );
        console.log('Users loaded:', this.users.length, 'users');
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Could not load user list: ' + (err.error?.message || err.message || "Unknown error")
        });
      }
    });
  }

  openEditDialog(user: any) {
    this.selectedUser = { ...user };

    // Reset selectedRoles array
    this.selectedRoles = [];

    // Check if user has roles
    if (user.roles && Array.isArray(user.roles)) {
      // Extract role codes from role names (ROLE_ADMIN -> ROLE_ADMIN, ROLE_USER -> ROLE_USER)
      user.roles.forEach((role: any) => {
        if (role.name) {
          this.selectedRoles.push(role.name);
        }
      });

      console.log('Current user roles:', user.roles);
      console.log('Selected roles for editing:', this.selectedRoles);
    }

    this.displayEditDialog = true;
  }

  saveUserRole() {
    if (!this.selectedUser || !this.selectedRoles.length) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Please select at least one role for the user'
      });
      return;
    }

    console.log('Saving roles:', this.selectedUser.username, this.selectedRoles);

    this.userService.updateUserRole(this.selectedUser.username, this.selectedRoles).subscribe({
      next: (response) => {
        // Kiểm tra xem phản hồi có chứa thông báo cần làm mới token không
        const message = response.message || 'Role updated successfully';
        const needTokenRefresh = message.includes('refresh your token');

        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: message
        });

        // Nếu đây là cập nhật vai trò của chính người dùng đang đăng nhập
        if (needTokenRefresh) {
          // Hiển thị thông báo gợi ý đăng xuất và đăng nhập lại
          this.confirmationService.confirm({
            message: 'Bạn đã cập nhật vai trò của chính mình. Có muốn đăng xuất và đăng nhập lại để áp dụng thay đổi không?',
            header: 'Xác nhận',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Có, đăng xuất ngay',
            rejectLabel: 'Không, tôi sẽ làm sau',
            accept: () => {
              // Logic đăng xuất ở đây (có thể chuyển đến trang login)
              window.location.href = '/login'; // Hoặc sử dụng AuthService.logout()
            }
          });
        }

        this.displayEditDialog = false;
        this.loadUsers();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Could not update role: ' + (err.error?.message || err.message || "Unknown error")
        });
      }
    });
  }

  confirmDelete(user: any) {
    // Không còn kiểm tra tài khoản admin nữa, cho phép xóa tất cả tài khoản
    this.selectedUser = user;
    this.displayDeleteDialog = true;
  }

  deleteUser() {
    if (!this.selectedUser) return;

    console.log('Deleting user:', this.selectedUser.username);

    // Thêm thông báo đang xử lý
    this.messageService.add({
      severity: 'info',
      summary: 'Đang xử lý',
      detail: 'Đang xóa tài khoản người dùng...'
    });

    // Xóa tài khoản ngay lập tức không cần kiểm tra
    this.userService.deleteUser(this.selectedUser.username).subscribe({
      next: (response) => {
        console.log('Delete user response:', response);
        this.messageService.add({
          severity: 'success',
          summary: 'Thành công',
          detail: 'Tài khoản người dùng đã được xóa hoàn toàn'
        });
        this.displayDeleteDialog = false;

        // Làm mới danh sách người dùng
        this.loadUsers();
      },
      error: (err) => {
        console.error('Delete user error:', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Lỗi',
          detail: 'Không thể xóa người dùng: ' + (err.error?.message || 'Lỗi không xác định')
        });
        this.displayDeleteDialog = false;
        this.loadUsers();
      }
    });
  }

  getRoleNames(roles: any[]): string {
    return roles.map(role => role.name.replace('ROLE_', '')).join(', ');
  }

  showSuccess(detail: string) {
    this.messageService.add({
      severity: 'success',
      summary: 'Success',
      detail
    });
  }

  showError(detail: string) {
    this.messageService.add({
      severity: 'error',
      summary: 'Error',
      detail
    });
  }
}
