import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { faHeart, faRetweet, faShoppingBag, faSearch } from '@fortawesome/free-solid-svg-icons';
import { MessageService } from 'primeng/api';
import { CartService } from 'src/app/_service/cart.service';
import { CategoryService } from 'src/app/_service/category.service';
import { ProductService } from 'src/app/_service/product.service';
import { WishlistService } from 'src/app/_service/wishlist.service';


@Component({
  selector: 'app-shop',
  templateUrl: './shop.component.html',
  styleUrls: ['./shop.component.css'],
  providers: [MessageService]

})
export class ShopComponent implements OnInit {

  heart = faHeart;
  bag = faShoppingBag;
  retweet = faRetweet;
  search = faSearch;

  id: number = 0;
  listProduct: any;
  listCategory: any;
  listProductNewest: any[] = [];
  currentCategory: string = ''; // Tên danh mục hiện tại

  rangeValues = [0, 1000000]; // Giá trị mặc định cho VNĐ
  minPriceInput: number = 0; // Giá trị input tối thiểu
  maxPriceInput: number = 1000000; // Giá trị input tối đa

  constructor(
    private categoryService: CategoryService,
    private productService: ProductService,
    private router: Router,
    private route: ActivatedRoute,
    public cartService: CartService,
    public wishlistService: WishlistService,
    private messageService: MessageService) {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;

  }

  ngOnInit(): void {
    this.id = this.route.snapshot.params['id'];

    // Lấy danh sách danh mục trước
    this.getListCategoryEnabled();
    this.getNewestProduct();

    // Sau đó lấy sản phẩm dựa trên id hoặc tất cả sản phẩm
    setTimeout(() => {
      if (this.id) {
        // Nếu có ID danh mục, lấy sản phẩm theo danh mục
        this.getListProductByCategory();
      } else {
        // Nếu không có ID danh mục, lấy tất cả sản phẩm
        this.getAllProducts();
      }
    }, 100);

    // Khởi tạo giá trị input ban đầu
    this.minPriceInput = this.rangeValues[0];
    this.maxPriceInput = this.rangeValues[1];
  }

  getAllProducts() {
    this.productService.getListProduct().subscribe({
      next: res => {
        this.listProduct = res;
      },
      error: err => {
        console.log(err);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Could not load product list'
        });
      }
    });
  }

  getListProductByCategory() {
    this.productService.getListByCategory(this.id).subscribe({
      next: res => {
        this.listProduct = res;
        // Tìm tên danh mục theo ID
        if (this.listCategory && this.listCategory.length > 0) {
          const category = this.listCategory.find((cat: any) => cat.id === this.id);
          if (category) {
            this.currentCategory = category.name;
          }
        }
      }, error: err => {
        console.log(err);
      }
    })
  }

  getListCategoryEnabled() {
    this.categoryService.getListCategoryEnabled().subscribe({
      next: res => {
        this.listCategory = res;
      }, error: err => {
        console.log(err);
      }
    })
  }

  getNewestProduct() {
    this.productService.getListProductNewest(4).subscribe({
      next: res => {
        this.listProductNewest = res;
      }, error: err => {
        console.log(err);
      }
    })
  }

  getListProductByPriceRange() {
    if (this.id) {
      // Nếu có ID danh mục, lọc sản phẩm theo danh mục và khoảng giá
      this.productService.getListByPriceRange(this.id, this.rangeValues[0], this.rangeValues[1]).subscribe({
        next: res => {
          this.listProduct = res;
          console.log(this.listProduct);
        }, error: err => {
          console.log(err);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Could not filter products by price range'
          });
        }
      });
    } else {
      // Nếu không có ID danh mục, lọc tất cả sản phẩm theo khoảng giá
      this.getAllProductsByPriceRange();
    }
  }

  getAllProductsByPriceRange() {
    // Lấy tất cả sản phẩm và lọc trên client
    this.productService.getListProduct().subscribe({
      next: res => {
        // Lọc sản phẩm theo khoảng giá trên client
        const min = this.rangeValues[0];
        const max = this.rangeValues[1];

        // Chuyển giá thành số để so sánh
        this.listProduct = res.filter((product: any) => {
          const price = parseFloat(product.price);
          return price >= min && price <= max;
        });

        console.log('Tất cả sản phẩm trong khoảng giá (lọc client):', this.listProduct);

        // Hiển thị thông báo tùy thuộc vào kết quả
        if (this.listProduct.length === 0) {
          this.messageService.add({
            severity: 'info',
            summary: 'Thông báo',
            detail: `Không tìm thấy sản phẩm nào trong khoảng giá từ ${min.toLocaleString()} VNĐ đến ${max.toLocaleString()} VNĐ`
          });
        } else {
          this.messageService.add({
            severity: 'success',
            summary: 'Success',
            detail: `Đã tìm thấy ${this.listProduct.length} sản phẩm trong khoảng giá đã chọn`
          });
        }
      },
      error: err => {
        console.log(err);
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Could not filter all products by price range'
        });
      }
    });
  }

  // Cập nhật slider khi thay đổi giá trị input
  updatePriceRange() {
    // Đảm bảo min không lớn hơn max
    if (this.minPriceInput > this.maxPriceInput) {
      this.minPriceInput = this.maxPriceInput;
    }

    // Đảm bảo giá trị trong phạm vi hợp lệ
    if (this.minPriceInput < 0) this.minPriceInput = 0;
    if (this.maxPriceInput > 5000000) this.maxPriceInput = 5000000;

    // Cập nhật giá trị cho slider
    this.rangeValues = [this.minPriceInput, this.maxPriceInput];
  }

  // Áp dụng bộ lọc giá
  applyPriceFilter() {
    this.updatePriceRange();
    this.getListProductByPriceRange();
  }

  addToCart(item: any) {
    this.cartService.getItems();
    this.cartService.addToCart(item, 1);

    // Hiển thị thông báo trực tiếp từ component
    this.messageService.add({
      severity: 'success',
      summary: 'Success',
      detail: `${item.name} has been added to your cart!`,
      life: 3000
    });
  }

  addToWishList(item: any) {
    if (!this.wishlistService.productInWishList(item)) {
      this.wishlistService.addToWishList(item);
    }
  }

  // Đặt lại bộ lọc giá
  resetPriceFilter() {
    this.minPriceInput = 0;
    this.maxPriceInput = 1000000;
    this.rangeValues = [0, 1000000];

    // Nếu có ID danh mục, lấy lại sản phẩm theo danh mục
    if (this.id) {
      this.getListProductByCategory();
    } else {
      // Nếu không có ID danh mục, lấy lại tất cả sản phẩm
      this.getAllProducts();
    }

    this.messageService.add({
      severity: 'info',
      summary: 'Đặt lại',
      detail: 'Đã xóa bộ lọc giá và hiển thị tất cả sản phẩm'
    });
  }

}
