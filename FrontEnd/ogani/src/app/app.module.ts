import { NgModule, LOCALE_ID } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common'; // Fix lỗi thiếu CommonModule
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/vi';
import { DEFAULT_CURRENCY_CODE } from '@angular/core';

// Routing
import { AppRoutingModule } from './app-routing.module';

// Component chính
import { AppComponent } from './app.component';
import { DashboardComponent } from './components/admin/dashboard/dashboard.component';
import { CategoryComponent } from './components/admin/category/category.component';
import { ProductComponent } from './components/admin/product/product.component';
import { OrderComponent } from './components/admin/order/order.component';
import { BlogComponent } from './components/admin/blog/blog.component';
import { AccountComponent } from './components/admin/account/account.component';
import { TagComponent } from './components/admin/tag/tag.component';
import { ProfileComponent } from './components/admin/profile/profile.component';
import { AdminLayoutComponent } from './components/admin/admin-layout/admin-layout.component';

// Component Client
import { IndexComponent } from './components/client/index/index.component';
import { HomeComponent } from './components/client/home/home.component';
import { ShopComponent } from './components/client/shop/shop.component';
import { ProductDetailComponent } from './components/client/product-detail/product-detail.component';
import { CartComponent } from './components/client/cart/cart.component';
import { CheckoutComponent } from './components/client/checkout/checkout.component';
import { BlogClientComponent } from './components/client/blog-client/blog-client.component';
import { BlogDetailComponent } from './components/client/blog-detail/blog-detail.component';
import { UserDetailComponent } from './components/client/user-detail/user-detail.component';
import { MyOrderComponent } from './components/client/my-order/my-order.component';
import { SearchComponent } from './components/client/search/search.component';
import { LoginPageComponent } from './components/client/login-page/login-page.component';
import { ContactComponent } from './components/client/contact/contact.component';
import { AboutComponent } from './components/client/about/about.component';
import { CheckboxModule } from 'primeng/checkbox'; // THÊM DÒNG NÀY
import { OAuthSuccessComponent } from './components/client/oauth-success/oauth-success.component';

// FontAwesome
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

// PrimeNG Modules
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { InputNumberModule } from 'primeng/inputnumber';
import { ToolbarModule } from 'primeng/toolbar';
import { FileUploadModule } from 'primeng/fileupload';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { InputTextModule } from 'primeng/inputtext';
import { RadioButtonModule } from 'primeng/radiobutton';
import { DividerModule } from 'primeng/divider';
import { CarouselModule } from 'primeng/carousel';
import { OverlayPanelModule } from 'primeng/overlaypanel';
import { TabViewModule } from 'primeng/tabview';
import { PasswordModule } from 'primeng/password';
import { SliderModule } from 'primeng/slider';
import { DataViewModule } from 'primeng/dataview';
import { MultiSelectModule } from 'primeng/multiselect';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { CalendarModule } from 'primeng/calendar';
import { GalleriaModule } from 'primeng/galleria';
import { MenubarModule } from 'primeng/menubar';
import { ImageModule } from 'primeng/image';
import { RatingModule } from 'primeng/rating';
import { MessagesModule } from 'primeng/messages';
import { MessageModule } from 'primeng/message';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SkeletonModule } from 'primeng/skeleton';
import { DropdownModule } from 'primeng/dropdown';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService } from 'primeng/api';

registerLocaleData(localeFr, 'vi');

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    CategoryComponent,
    ProductComponent,
    OrderComponent,
    BlogComponent,
    AccountComponent,
    TagComponent,
    ProfileComponent,
    AdminLayoutComponent,
    IndexComponent,
    HomeComponent,
    CartComponent,
    CheckoutComponent,
    ShopComponent,
    ProductDetailComponent,
    BlogClientComponent,
    BlogDetailComponent,
    UserDetailComponent,
    MyOrderComponent,
    SearchComponent,
    LoginPageComponent,
    ContactComponent,
    AboutComponent,
    OAuthSuccessComponent
  ],
  imports: [
    BrowserModule,
    CommonModule,  // Được thêm vào để fix lỗi
    FormsModule,
    HttpClientModule,
    BrowserAnimationsModule,
    RouterModule,
    AppRoutingModule,
    FontAwesomeModule,
    NgbModule,
    CardModule,
    DialogModule,
    ToastModule,
    ButtonModule,
    TableModule,
    InputNumberModule,
    ToolbarModule,
    FileUploadModule,
    ConfirmDialogModule,
    InputTextareaModule,
    InputTextModule,
    RadioButtonModule,
    DividerModule,
    CarouselModule,
    OverlayPanelModule,
    TabViewModule,
    PasswordModule,
    SliderModule,
    DataViewModule,
    MultiSelectModule,
    CalendarModule,
    GalleriaModule,
    MenubarModule,
    ImageModule,
    RatingModule,
    MessagesModule,
    MessageModule,
    ProgressSpinnerModule,
    SkeletonModule,
    DropdownModule,
    TagModule,
    TooltipModule,
    CheckboxModule // THÊM DÒNG NÀY
  ],
  providers: [
    MessageService,
    { provide: LOCALE_ID, useValue: 'vi' },
    { provide: DEFAULT_CURRENCY_CODE, useValue: 'VNĐ' }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
