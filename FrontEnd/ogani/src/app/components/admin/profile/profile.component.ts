import { Component, OnInit } from '@angular/core';
import { StorageService } from 'src/app/_service/storage.service';
import { UserService } from 'src/app/_service/user.service';
import { MessageService } from 'primeng/api';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
  providers: [MessageService],
})
export class ProfileComponent implements OnInit {
  username: any;
  user: any;
  profileImageUrl: string | null = null;
  selectedFile: File | null = null;
  timestamp = Date.now();
  backendUrl = environment.apiUrl.replace('/api', '');

  changePassword: boolean = false;
  confirmPassword: string = '';

  updateForm: any = {
    firstname: null,
    lastname: null,
    email: null,
    country: null,
    state: null,
    address: null,
    phone: null,
  };

  changePasswordForm: any = {
    oldPassword: null,
    newPassword: null,
  };

  constructor(
    private storageService: StorageService,
    private userService: UserService,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.username = this.storageService.getUser().username;
    this.getUser();
  }

  getUser() {
    this.userService.getUser(this.username).subscribe({
      next: (res) => {
        this.user = res;
        this.updateForm.firstname = res.firstname;
        this.updateForm.lastname = res.lastname;
        this.updateForm.email = res.email;
        this.updateForm.country = res.country;
        this.updateForm.state = res.state;
        this.updateForm.address = res.address;
        this.updateForm.phone = res.phone;

        if (res.profileImage) {
          this.timestamp = Date.now();

          if (res.profileImage.startsWith('http')) {
            this.profileImageUrl = res.profileImage + '?t=' + this.timestamp;
          } else {
            this.profileImageUrl =
              this.backendUrl + res.profileImage + '?t=' + this.timestamp;
          }
        } else {
          this.profileImageUrl = null;
        }
      },
      error: (err) => {
        console.log(err);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Could not load user information',
        });
      },
    });
  }

  updateProfile() {
    const { firstname, lastname, email, country, state, address, phone } =
      this.updateForm;
    this.userService
      .updateProfile(
        this.username,
        firstname,
        lastname,
        email,
        country,
        state,
        address,
        phone
      )
      .subscribe({
        next: (res) => {
          this.getUser();
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Thông tin đã được cập nhật thành công',
          });
        },
        error: (err) => {
          console.log(err);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Could not update profile information',
          });
        },
      });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (!file.type.match(/image\//)) {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Vui lòng chọn tệp hình ảnh',
        });
        return;
      }

      if (file.size > 5 * 1024 * 1024) {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Kích thước tệp vượt quá giới hạn tối đa (5MB)',
        });
        return;
      }

      this.selectedFile = file;
      this.uploadImage();
    }
  }

  uploadImage(): void {
    if (this.selectedFile) {
      this.messageService.add({
        severity: 'info',
        summary: 'Thông báo',
        detail: 'Đang tải hình ảnh lên...',
      });

      this.userService
        .uploadProfileImage(this.username, this.selectedFile)
        .subscribe({
          next: (response) => {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: 'Hình ảnh hồ sơ đã được cập nhật thành công',
            });

            if (response) {
              this.timestamp = Date.now();

              if (response.imageUrl) {
                if (response.imageUrl.startsWith('http')) {
                  this.profileImageUrl =
                    response.imageUrl + '?t=' + this.timestamp;
                } else {
                  this.profileImageUrl =
                    this.backendUrl +
                    response.imageUrl +
                    '?t=' +
                    this.timestamp;
                }
              }

              setTimeout(() => {
                this.getUser();
              }, 500);
            }
          },
          error: (err) => {
            let errorMessage = 'Không thể tải hình ảnh hồ sơ lên';

            if (err.error) {
              if (typeof err.error === 'string') {
                errorMessage += ': ' + err.error;
              } else if (err.error.message) {
                errorMessage += ': ' + err.error.message;
              }
            } else if (err.status) {
              errorMessage += ' (Error ' + err.status + ')';
            }

            this.messageService.add({
              severity: 'error',
              summary: 'Error',
              detail: errorMessage,
            });
          },
        });
    }
  }

  changePasswordFunc() {
    if (this.changePasswordForm.newPassword !== this.confirmPassword) {
      this.messageService.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: 'Mật khẩu xác nhận không khớp với mật khẩu mới',
      });
      return;
    }

    const { oldPassword, newPassword } = this.changePasswordForm;
    this.userService
      .changePassword(this.username, oldPassword, newPassword)
      .subscribe({
        next: (res) => {
          this.changePassword = false;
          this.changePasswordForm = {
            oldPassword: null,
            newPassword: null,
          };
          this.confirmPassword = '';

          this.messageService.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Mật khẩu đã được thay đổi',
          });
        },
        error: (err) => {
          console.log(err);
          let errorMsg = 'Không thể thay đổi mật khẩu';

          if (err.error && err.error.message) {
            if (err.error.message.includes('incorrect')) {
              errorMsg = 'Mật khẩu hiện tại không chính xác';
            } else {
              errorMsg = err.error.message;
            }
          }

          this.messageService.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: errorMsg,
          });
        },
      });
  }

  showChangePassword() {
    this.changePasswordForm = {
      oldPassword: null,
      newPassword: null,
    };
    this.confirmPassword = '';
    this.changePassword = true;
  }
}
