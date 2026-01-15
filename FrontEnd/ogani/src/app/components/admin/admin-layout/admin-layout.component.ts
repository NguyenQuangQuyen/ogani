import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { IconDefinition } from '@fortawesome/free-solid-svg-icons';
import {
  faFaceLaughWink,
  faTag,
  faSearch,
  faBell,
  faEnvelope,
  faTachometerAlt,
  faBookmark,
  faReceipt,
  faCartShopping,
  faRocket,
  faUser,
  faBars,
  faPaperPlane,
  faGear,
  faRightFromBracket,
  faUsers,
  faHome,
  faDownload
} from '@fortawesome/free-solid-svg-icons';
import { AuthService } from 'src/app/_service/auth.service';
import { StorageService } from 'src/app/_service/storage.service';

@Component({
  selector: 'app-admin-layout',
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.css']
})
export class AdminLayoutComponent implements OnInit {
  // Định nghĩa icon đúng kiểu `IconDefinition`
  faceLaugh: IconDefinition = faFaceLaughWink;
  search: IconDefinition = faSearch;
  bell: IconDefinition = faBell;
  envelope: IconDefinition = faEnvelope;
  tachometer: IconDefinition = faTachometerAlt;
  bookmark: IconDefinition = faBookmark;
  receipt: IconDefinition = faReceipt;
  cart: IconDefinition = faCartShopping;
  rocket: IconDefinition = faRocket;
  userIcon: IconDefinition = faUser;
  paperPlane: IconDefinition = faPaperPlane;
  bars: IconDefinition = faBars;
  gear: IconDefinition = faGear;
  logoutIcon: IconDefinition = faRightFromBracket;
  tag: IconDefinition = faTag;
  users: IconDefinition = faUsers;
  home: IconDefinition = faHome;
  download: IconDefinition = faDownload;

  constructor(
    private storageService: StorageService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void { }

  logout() {
    this.authService.logout().subscribe({
      next: data => {
        this.storageService.clean();
        // Chuyển hướng đến trang login
        this.router.navigate(['/login']);
      },
      error: err => {
        console.error(err);
        // Ngay cả khi có lỗi, vẫn logout và chuyển hướng
        this.storageService.clean();
        this.router.navigate(['/login']);
      }
    });
  }
} 
