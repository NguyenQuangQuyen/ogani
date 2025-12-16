import { Component, OnInit } from '@angular/core';
import { StorageService } from 'src/app/_service/storage.service';
import { UserService } from 'src/app/_service/user.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-user-detail',
  templateUrl: './user-detail.component.html',
  styleUrls: ['./user-detail.component.css'],
  providers: [MessageService]
})
export class UserDetailComponent implements OnInit {

  username: any;
  user: any;
  profileImageUrl: string | null = null;
  selectedFile: File | null = null;
  timestamp = Date.now();
  backendUrl = 'http://localhost:8080'; // Thêm base URL của backend

  changePassword: boolean = false;
  confirmPassword: string = '';

  updateForm: any = {
    firstname: null,
    lastname: null,
    email: null,
    country: null,
    state: null,
    address: null,
    phone: null
  }

  changePasswordForm: any = {
    oldPassword: null,
    newPassword: null
  }

  constructor(
    private storageService: StorageService,
    private userService: UserService,
    private messageService: MessageService
  ) { }

  ngOnInit(): void {
    this.username = this.storageService.getUser().username;
    this.getUser();
  }

  getUser() {
    this.userService.getUser(this.username).subscribe({
      next: res => {
        this.user = res;
        this.updateForm.firstname = res.firstname;
        this.updateForm.lastname = res.lastname;
        this.updateForm.email = res.email;
        this.updateForm.country = res.country;
        this.updateForm.state = res.state;
        this.updateForm.address = res.address;
        this.updateForm.phone = res.phone;

        console.log('User provider:', res.provider);
        console.log('User profileImage:', res.profileImage);
        console.log('User avatar:', res.avatar);
        console.log('User data:', res);

        // Mặc định, sử dụng timestamp để tránh cache
        this.timestamp = Date.now();

        // Ưu tiên hiển thị theo thứ tự sau:
        // 1. Ảnh được tải lên thủ công (profileImage) nếu có đường dẫn đầy đủ hoặc local path
        // 2. Ảnh từ Google (avatar) nếu người dùng đăng nhập từ Google và chưa tải lên ảnh thủ công
        // 3. Ảnh mặc định nếu không có ảnh nào

        if (res.profileImage && (res.profileImage.startsWith('http') || res.profileImage.startsWith('/api/'))) {
          // Nếu người dùng đã tải lên ảnh (có profileImage), ưu tiên sử dụng profileImage
          if (res.profileImage.startsWith('http')) {
            this.profileImageUrl = res.profileImage;
          } else {
            this.profileImageUrl = this.backendUrl + res.profileImage;
          }
          
          // Thêm timestamp để tránh caching
          if (this.profileImageUrl && !this.profileImageUrl.includes('?')) {
            this.profileImageUrl = this.profileImageUrl + '?t=' + this.timestamp;
          }

          console.log('Using uploaded profile image:', this.profileImageUrl);
        } 
        else if (res.provider === 'google' && res.avatar) {
          // Nếu người dùng chưa tải lên ảnh và đăng nhập từ Google, sử dụng ảnh Google
          this.profileImageUrl = res.avatar;
          console.log('Using Google avatar URL:', this.profileImageUrl);
        }
        else {
          // Nếu không có ảnh đại diện, sử dụng ảnh mặc định
          this.profileImageUrl = null;
          console.log('No profile image found for user, using default image');
        }

        // Lưu URL ảnh đại diện vào localStorage nếu có
        if (this.profileImageUrl) {
          localStorage.setItem('userProfileImage', this.profileImageUrl);
        } else {
          localStorage.removeItem('userProfileImage');
        }
      }, error: err => {
        console.log(err);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Could not load user information'
        });
      }
    })
  }

  updateProfile() {
    const { firstname, lastname, email, country, state, address, phone } = this.updateForm;
    this.userService.updateProfile(this.username, firstname, lastname, email, country, state, address, phone).subscribe({
      next: res => {
        this.getUser();
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: 'Information updated successfully'
        });
      }, error: err => {
        console.log(err);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Could not update information'
        });
      }
    });
  }

  // Xử lý khi người dùng chọn file ảnh
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      // Kiểm tra loại file
      if (!file.type.match(/image\//)) {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Please select an image file'
        });
        return;
      }

      // Giới hạn kích thước file (5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'File size exceeds the maximum limit (5MB)'
        });
        return;
      }

      this.selectedFile = file;
      this.uploadImage();
    }
  }

  // Xử lý upload ảnh từ máy tính
  uploadImage(): void {
    if (this.selectedFile) {
      // Hiển thị thông báo đang xử lý
      this.messageService.add({
        severity: 'info',
        summary: 'Thông báo',
        detail: 'Uploading image...'
      });

      // Log thông tin file trước khi upload
      console.log('Uploading file:', this.selectedFile.name, 'size:', this.selectedFile.size, 'type:', this.selectedFile.type);
      
      // Thêm log cho trường hợp người dùng Google
      if (this.user && this.user.provider === 'google') {
        console.log('User is Google user, replacing Google profile image with uploaded image');
      }

      this.userService.uploadProfileImage(this.username, this.selectedFile).subscribe({
        next: (response) => {
          console.log('Upload response:', response);

          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Profile picture updated successfully'
          });

          // Cập nhật URL ảnh đại diện
          if (response) {
            // Tạo timestamp mới để buộc trình duyệt tải lại ảnh mới nhất
            this.timestamp = Date.now();

            // Cập nhật tạm thời URL ảnh mới
            if (response.fullUrl) {
              this.profileImageUrl = response.fullUrl + '?t=' + this.timestamp;
              console.log('Using fullUrl from server:', this.profileImageUrl);
            } else if (response.imageUrl) {
              // Nếu có imageUrl nhưng không có domain, thêm vào
              if (response.imageUrl.startsWith('http')) {
                this.profileImageUrl = response.imageUrl + '?t=' + this.timestamp;
              } else {
                this.profileImageUrl = this.backendUrl + response.imageUrl + '?t=' + this.timestamp;
              }
              console.log('Using imageUrl from server:', this.profileImageUrl);
            }

            // Cập nhật trong localStorage
            if (this.profileImageUrl) {
              localStorage.setItem('userProfileImage', this.profileImageUrl);
            }

            // Đối với người dùng Google, cập nhật trường thông tin user trong frontend để phản ánh đúng thay đổi
            if (this.user && this.user.provider === 'google') {
              console.log('Updating Google user avatar in local model');
              if (response.imageUrl) {
                this.user.avatar = response.imageUrl;
              }
            }

            // Tải lại thông tin người dùng từ server để đảm bảo dữ liệu đồng bộ
            setTimeout(() => {
              this.getUser();
            }, 500);
          }
        },
        error: (err) => {
          console.error('Upload error:', err);

          let errorMessage = 'Could not upload image';

          // Phân tích lỗi để hiển thị thông báo cụ thể hơn
          if (err.error) {
            if (typeof err.error === 'string') {
              errorMessage += ': ' + err.error;
            } else if (err.error.message) {
              errorMessage += ': ' + err.error.message;

              // Hiển thị gợi ý cụ thể cho các lỗi phổ biến
              if (err.error.message.includes('FileNotFoundException') ||
                err.error.message.includes('cannot find the path')) {
                errorMessage += ' (Storage directory does not exist)';

                this.messageService.add({
                  severity: 'info',
                  summary: 'Gợi ý',
                  detail: 'Please contact administrator to create storage directory on server'
                });
              } else if (err.error.message.includes('Permission')) {
                errorMessage += ' (No permission to write to directory)';
              }
            }
          } else if (err.status) {
            errorMessage += ' (Error ' + err.status + ')';
          }

          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: errorMessage
          });

          // Hiển thị hướng dẫn cụ thể cho từng loại lỗi
          if (err.status === 500) {
            this.messageService.add({
              severity: 'info',
              summary: 'Thông tin',
              detail: 'This could be a server error, please try again later or contact administrator'
            });
          }
        }
      });
    }
  }

  // Xử lý khi ảnh không tải được
  onImageError(event: any): void {
    console.error('Error loading image:', event);
    
    // Thử tải lại ảnh từ Google nếu người dùng đăng nhập bằng Google
    if (this.user && this.user.provider === 'google' && this.user.avatar) {
      console.log('Attempting to use Google avatar URL directly:', this.user.avatar);
      event.target.src = this.user.avatar;
      event.target.setAttribute('referrerpolicy', 'no-referrer'); // Đảm bảo thuộc tính này luôn được áp dụng
      return;
    }
    
    // Nếu vẫn lỗi hoặc không có ảnh Google, sử dụng ảnh mặc định
    event.target.src = 'assets/image/author.jpg';
    console.log('Replaced with default image due to loading error');
  }

  changePasswordFunc() {
    if (this.changePasswordForm.newPassword !== this.confirmPassword) {
      this.messageService.add({
        severity: 'error',
        summary: 'Lỗi',
        detail: 'Mật khẩu xác nhận không khớp với mật khẩu mới'
      });
      return;
    }

    const { oldPassword, newPassword } = this.changePasswordForm;
    this.userService.changePassword(this.username, oldPassword, newPassword).subscribe({
      next: res => {
        this.changePassword = false;
        // Reset the form
        this.changePasswordForm = {
          oldPassword: null,
          newPassword: null
        };
        this.confirmPassword = '';

        this.messageService.add({
          severity: 'success',
          summary: 'Thành công',
          detail: 'Mật khẩu đã được thay đổi'
        });
      },
      error: err => {
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
          detail: 'Mật khẩu hiện tại không chính xác'
        });
      }
    });
  }

  showChangePassword() {
    // Reset form before showing
    this.changePasswordForm = {
      oldPassword: null,
      newPassword: null
    };
    this.confirmPassword = '';
    this.changePassword = true;
  }
}
