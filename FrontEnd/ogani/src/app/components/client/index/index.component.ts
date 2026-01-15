import { Component, OnInit, ChangeDetectorRef, AfterContentChecked } from '@angular/core';
import { Router } from '@angular/router';
import { faBars, faHeart, faRightFromBracket, faUser, faAngleDown, faShoppingBag, faPhone, faSearch, faUserShield } from '@fortawesome/free-solid-svg-icons'
import { MessageService } from 'primeng/api';
import { AuthService } from 'src/app/_service/auth.service';
import { CartService } from 'src/app/_service/cart.service';
import { CategoryService } from 'src/app/_service/category.service';
import { StorageService } from 'src/app/_service/storage.service';
import { WishlistService } from 'src/app/_service/wishlist.service';



@Component({
  selector: 'app-index',
  templateUrl: './index.component.html',
  styleUrls: ['./index.component.css'],
  providers: [MessageService]

})
export class IndexComponent implements OnInit {

  listItemInCart: any[] = [];
  totalPrice = 0;
  heart = faHeart;
  bag = faShoppingBag;
  phone = faPhone;
  userIcon = faUser;
  adminIcon = faUserShield;
  logoutIcon = faRightFromBracket;
  bars = faBars;
  angleDown = faAngleDown;
  searchIcon = faSearch;

  showDepartment = false;
  showMobileMenu = false;




  loginForm: any = {
    username: null,
    password: null
  }

  registerForm: any = {
    username: null,
    email: null,
    password: null
  }

  isSuccessful = false;
  isSignUpFailed = false;
  isLoggedIn = false;
  isLoginFailed = false;
  roles: string[] = [];
  errorMessage = '';
  authModal: boolean = false;
  listCategoryEnabled: any;

  // Biến cho dialog lỗi đăng nhập
  displayErrorDialog = false;
  errorTitle = '';

  keyword: any;

  // Mảng chứa các liên kết mạng xã hội cho footer
  socialLinks = [
    {
      name: 'facebook',
      url: 'https://www.facebook.com/quyen.quy.35380/',
      icon: 'fa fa-facebook',
      label: 'Facebook',
      logoSrc: 'assets/image/facebook-logo.png'
    },
    {
      name: 'instagram',
      url: 'https://www.instagram.com/quyenquy053/',
      icon: 'fa fa-instagram',
      label: 'Instagram',
      logoSrc: 'assets/image/instagram-logo.png'
    },
    {
      name: 'youtube',
      url: 'https://www.youtube.com/@Matcha-Turtle-203',
      icon: 'fa fa-youtube-play',
      label: 'YouTube',
      logoSrc: 'assets/image/youtube-logo.png'
    },
    {
      name: 'tiktok',
      url: 'https://www.tiktok.com/@user708j53dnqe?lang=vi-VN',
      icon: 'fa fa-music',
      label: 'TikTok',
      logoSrc: 'assets/image/tiktok-logo.png'
    }
  ];

  // Getter để lấy username người dùng đang đăng nhập
  get username(): string {
    return this.isLoggedIn ? this.storageService.getUser().username : 'Guest';
  }

  loading = false; // Thêm thuộc tính loading cho spinner

  constructor(
    public cartService: CartService,
    public wishlistService: WishlistService,
    private authService: AuthService,
    private storageService: StorageService,
    private messageService: MessageService,
    private categoryService: CategoryService,
    private router: Router) {

  }

  ngOnInit(): void {
    this.getCategoryEnbled();
    this.isLoggedIn = this.storageService.isLoggedIn();

    // Khôi phục thông tin vai trò từ session storage nếu người dùng đã đăng nhập
    if (this.isLoggedIn) {
      const user = this.storageService.getUser();
      console.log('User object from storage:', user);

      // Kiểm tra cấu trúc của đối tượng roles
      if (user && user.roles) {
        console.log('User roles type:', typeof user.roles);
        console.log('User roles is array:', Array.isArray(user.roles));
        console.log('User roles:', user.roles);
      }

      // Gán roles từ user object
      if (user && user.roles) {
        // Kiểm tra nếu roles là mảng thì gán trực tiếp
        if (Array.isArray(user.roles)) {
          this.roles = user.roles;
        }
        // Nếu roles không phải mảng mà là object với tên thuộc tính là role name
        else if (typeof user.roles === 'object') {
          this.roles = Object.keys(user.roles);
        }
        // Trường hợp roles là string
        else if (typeof user.roles === 'string') {
          this.roles = [user.roles];
        }
      } else {
        this.roles = [];
      }

      console.log('Loaded user roles:', this.roles);

      // Kiểm tra xem có phải là admin không
      const isAdmin = this.roles.some((role: any) => {
        console.log('Checking role:', role);
        return role === 'ROLE_ADMIN' || (typeof role === 'object' && role.name === 'ROLE_ADMIN');
      });
      console.log('Is admin?', isAdmin);
    }

    this.wishlistService.loadWishList();
    this.cartService.loadCart();
  }

  showDepartmentClick() {
    this.showDepartment = !this.showDepartment;
    console.log("Toggled departments menu, now:", this.showDepartment ? "SHOWN" : "HIDDEN");
  }

  toggleMobileMenu() {
    this.showMobileMenu = !this.showMobileMenu;
    console.log("Toggled mobile menu, now:", this.showMobileMenu ? "SHOWN" : "HIDDEN");

    // Add/remove class to body to prevent scrolling when menu is open
    if (this.showMobileMenu) {
      document.body.classList.add('over_hid');
    } else {
      document.body.classList.remove('over_hid');
    }
  }

  closeMobileMenu() {
    this.showMobileMenu = false;
    document.body.classList.remove('over_hid');
  }

  getCategoryEnbled() {
    this.categoryService.getListCategory().subscribe({
      next: res => {
        this.listCategoryEnabled = res;
        console.log('Categories loaded:', this.listCategoryEnabled);

        if (!this.listCategoryEnabled || this.listCategoryEnabled.length === 0) {
          console.warn('No categories available');
          this.showWarn('Không có danh mục nào được kích hoạt. Vui lòng kích hoạt danh mục trong trang quản trị.');
        }
      }, error: err => {
        console.error('Error loading categories:', err);
        this.showError('Không thể tải danh sách danh mục');
      }
    })
  }

  removeFromCart(item: any) {
    this.cartService.remove(item);
  }

  removeWishList(item: any) {
    this.wishlistService.remove(item);
  }

  showAuthForm() {
    if (!this.isLoggedIn) {
      this.router.navigate(['/login']);
    }
  }

  login(): void {
    const { username, password } = this.loginForm;
    console.log('Login attempt:', this.loginForm);

    this.authService.login(username, password).subscribe({
      next: res => {
        console.log('Login response:', res);

        // Lưu thông tin người dùng vào storage
        this.storageService.saveUser(res);
        this.isLoggedIn = true;
        this.isLoginFailed = false;

        // Xử lý roles từ response
        if (res.roles) {
          if (Array.isArray(res.roles)) {
            this.roles = res.roles;
          } else if (typeof res.roles === 'object') {
            this.roles = Object.keys(res.roles);
          } else if (typeof res.roles === 'string') {
            this.roles = [res.roles];
          } else {
            this.roles = [];
          }
        } else {
          this.roles = [];
        }

        console.log('User roles after login:', this.roles);
        console.log('Is admin after login:', this.isAdmin());

        // Thành công - đóng dialog và hiển thị thông báo
        this.showSuccess("Login successful!!");
        this.authModal = false;

        // Luôn chuyển hướng đến trang home (trang người dùng), bất kể vai trò là gì
        this.router.navigate(['/home']);
      },
      error: err => {
        console.error('Login error:', err);
        this.isLoggedIn = false;
        this.isLoginFailed = true;
        this.showError(err.message || 'Login failed');
      }
    });
  }

  register(): void {
    const { username, email, password } = this.registerForm;
    console.log(this.registerForm);
    this.authService.register(username, email, password).subscribe({
      next: res => {
        this.isSuccessful = true;
        this.isSignUpFailed = false;
        this.showSuccess("Đăng ký thành công! Vui lòng đăng nhập để tiếp tục.");

        // Đóng modal
        this.authModal = false;

        // Chuyển sang trang đăng nhập
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1500);
      }, error: err => {
        this.showError(err.message);
        this.errorMessage = err.error.message;
        this.isSignUpFailed = true;
      }
    })
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: res => {
        this.storageService.clean();
        this.isLoggedIn = false;
        this.roles = [];
        this.authModal = false;

        // Chuyển hướng đến trang login
        this.router.navigate(['/login']);
      },
      error: err => {
        this.errorMessage = err.error.message;
      }
    });
  }

  // Phương thức đóng dialog lỗi
  closeErrorDialog(): void {
    this.displayErrorDialog = false;
  }

  showSuccess(text: string) {
    this.messageService.add({ severity: 'success', summary: 'Success', detail: text });
  }
  showError(text: string) {
    this.messageService.add({ severity: 'error', summary: 'Error', detail: text });
  }

  showWarn(text: string) {
    this.messageService.add({ severity: 'warn', summary: 'Warn', detail: text });
  }

  simulateGoogleLogin(): void {
    this.messageService.add({
      severity: 'info',
      summary: 'Thông báo',
      detail: 'Tính năng đăng nhập bằng Google đang được phát triển!'
    });
    this.authModal = false;
  }

  // Xử lý khi người dùng nhấp vào biểu tượng admin
  navigateToAdmin(): void {
    if (!this.isLoggedIn) {
      // Nếu chưa đăng nhập, chuyển đến trang đăng nhập
      this.router.navigate(['/login']);
      this.messageService.add({
        severity: 'info',
        summary: 'Thông báo',
        detail: 'Please log in to continue',
        life: 1500
      });
      return;
    }

    // Hiển thị loading spinner
    this.loading = true;
    console.log("Đang làm mới token và kiểm tra quyền admin...");

    // Sử dụng API làm mới token để cập nhật cả backend (SecurityContext) và frontend (session storage)
    this.authService.refreshToken().subscribe({
      next: (userData) => {
        this.loading = false;
        console.log('===== DEBUG =====');
        console.log('Updated user data after refresh token:', userData);
        console.log('Type of roles:', typeof userData.roles);
        console.log('Roles content:', JSON.stringify(userData.roles));

        // Kiểm tra nếu người dùng có vai trò admin
        let isAdmin = false;
        if (userData && userData.roles) {
          let roles = userData.roles;
          console.log('Roles before processing:', roles);

          if (Array.isArray(roles)) {
            console.log('Roles is an array with length:', roles.length);
            isAdmin = roles.some((role: any) => {
              console.log('Checking role (array):', role, typeof role);
              if (typeof role === 'string') {
                const isAdminRole = role.toLowerCase() === 'role_admin' || role.toLowerCase() === 'admin';
                console.log(`Is admin role (string): ${isAdminRole}`);
                return isAdminRole;
              } else if (role && typeof role === 'object' && role.name) {
                const isAdminRole = role.name.toLowerCase() === 'role_admin' || role.name.toLowerCase() === 'admin';
                console.log(`Is admin role (object): ${isAdminRole}`);
                return isAdminRole;
              }
              return false;
            });
          } else if (typeof roles === 'object') {
            console.log('Roles is an object');
            isAdmin = Object.values(roles).some((role: any) => {
              console.log('Checking role (object):', role, typeof role);
              if (typeof role === 'string') {
                return role.toLowerCase() === 'role_admin' || role.toLowerCase() === 'admin';
              } else if (role && typeof role === 'object' && role.name) {
                return role.name.toLowerCase() === 'role_admin' || role.name.toLowerCase() === 'admin';
              }
              return false;
            });
          } else if (typeof roles === 'string') {
            console.log('Roles is a string:', roles);
            isAdmin = roles.toLowerCase() === 'role_admin' || roles.toLowerCase() === 'admin';
          }
        }

        console.log('Is admin after checking roles:', isAdmin);

        // Xóa hoàn toàn thông tin người dùng khỏi storage và lưu lại với thông tin mới
        window.sessionStorage.removeItem('auth-user');
        window.sessionStorage.removeItem('auth-token');

        // Lưu thông tin người dùng mới
        const currentUser: any = {
          id: userData.id,
          username: userData.username,
          email: userData.email,
          roles: userData.roles
        };

        // Nếu có token trong userData, sử dụng token đó
        if (userData.token) {
          console.log('Using new token from refresh-token API');
          currentUser.token = userData.token;
          window.sessionStorage.setItem('auth-token', userData.token);
        }

        // Lưu thông tin người dùng mới vào storage
        console.log('Saving updated user info to storage:', currentUser);
        this.storageService.saveUser(currentUser);

        // Cập nhật roles trong component
        this.roles = Array.isArray(userData.roles) ? userData.roles :
          (typeof userData.roles === 'object' ? Object.values(userData.roles) : [userData.roles]);

        if (isAdmin) {
          // Có quyền admin, chuyển đến trang admin ngay lập tức
          console.log('Has admin role after token refresh, navigating to admin page');
          this.messageService.add({
            severity: 'success',
            summary: 'Thành công',
            detail: 'Đang chuyển hướng đến trang Admin...',
            life: 2000
          });

          // Đợi một chút để hiển thị thông báo trước khi chuyển hướng
          setTimeout(() => {
            this.router.navigate(['/admin']);
          }, 1000);
        } else {
          // Không có quyền admin, hiển thị thông báo lỗi
          console.log('Not an admin after token refresh, showing error dialog');
          this.errorTitle = "Không có quyền truy cập!";
          this.errorMessage = "Bạn không phải là admin. Chỉ tài khoản có quyền Admin mới có thể truy cập trang quản trị.";
          this.displayErrorDialog = true;
        }
      },
      error: (err) => {
        this.loading = false;
        console.error('Error refreshing token:', err);

        // Nếu không thể làm mới token, thử sử dụng API hiện tại
        this.authService.getCurrentUserRoles().subscribe({
          next: (userData) => {
            console.log('Fallback to current-user-info API');

            // Kiểm tra nếu có quyền admin
            const isAdmin = this.checkAdminRole(userData.roles);

            if (isAdmin) {
              this.router.navigate(['/admin']);
            } else {
              this.errorTitle = "Không có quyền truy cập!";
              this.errorMessage = "Bạn không phải là admin. Chỉ tài khoản có quyền Admin mới có thể truy cập trang quản trị.";
              this.displayErrorDialog = true;
            }
          },
          error: () => {
            // Nếu cả hai API đều thất bại, sử dụng phương thức cũ dựa vào session storage
            if (this.isAdmin()) {
              this.router.navigate(['/admin']);
            } else {
              this.errorTitle = "Không có quyền truy cập!";
              this.errorMessage = "Bạn không phải là admin. Chỉ tài khoản có quyền Admin mới có thể truy cập trang quản trị.";
              this.displayErrorDialog = true;
            }
          }
        });
      }
    });
  }

  // Helper method để kiểm tra vai trò admin
  private checkAdminRole(roles: any): boolean {
    if (!roles) return false;

    if (Array.isArray(roles)) {
      return roles.some((role: any) => {
        if (typeof role === 'string') {
          return role.toLowerCase() === 'role_admin' || role.toLowerCase() === 'admin';
        } else if (role && typeof role === 'object' && role.name) {
          return role.name.toLowerCase() === 'role_admin' || role.name.toLowerCase() === 'admin';
        }
        return false;
      });
    } else if (typeof roles === 'object') {
      return Object.values(roles).some((role: any) => {
        if (typeof role === 'string') {
          return role.toLowerCase() === 'role_admin' || role.toLowerCase() === 'admin';
        } else if (role && typeof role === 'object' && role.name) {
          return role.name.toLowerCase() === 'role_admin' || role.name.toLowerCase() === 'admin';
        }
        return false;
      });
    } else if (typeof roles === 'string') {
      return roles.toLowerCase() === 'role_admin' || roles.toLowerCase() === 'admin';
    }

    return false;
  }

  // Kiểm tra người dùng có quyền admin hay không
  isAdmin(): boolean {
    if (!this.isLoggedIn) return false;

    // Lấy thông tin user mới nhất từ storage
    const currentUser = this.storageService.getUser();
    if (!currentUser) return false;

    // Lấy roles từ user hiện tại
    let userRoles = [];
    if (currentUser.roles) {
      if (Array.isArray(currentUser.roles)) {
        userRoles = currentUser.roles;
      } else if (typeof currentUser.roles === 'object') {
        userRoles = Object.values(currentUser.roles);
      } else if (typeof currentUser.roles === 'string') {
        userRoles = [currentUser.roles];
      }
    }

    console.log('Current user roles in isAdmin():', userRoles);

    // Kiểm tra nếu có vai trò 'ROLE_ADMIN' hoặc 'admin'
    return userRoles.some((role: any) => {
      if (typeof role === 'string') {
        return role.toLowerCase() === 'role_admin' || role.toLowerCase() === 'admin';
      } else if (role && typeof role === 'object' && role.name) {
        return role.name.toLowerCase() === 'role_admin' || role.name.toLowerCase() === 'admin';
      }
      return false;
    });
  }

}
