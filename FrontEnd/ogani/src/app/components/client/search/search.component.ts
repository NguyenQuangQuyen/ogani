import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import {
  faHeart,
  faRetweet,
  faShoppingBag,
} from '@fortawesome/free-solid-svg-icons';
import { MessageService } from 'primeng/api';
import { CartService } from 'src/app/_service/cart.service';
import { CategoryService } from 'src/app/_service/category.service';
import { ProductService } from 'src/app/_service/product.service';
import { WishlistService } from 'src/app/_service/wishlist.service';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css'],
  providers: [MessageService],
})
export class SearchComponent implements OnInit {
  heart = faHeart;
  bag = faShoppingBag;
  retweet = faRetweet;

  keyword: any;
  listProduct: any;
  listProductNewest: any;
  listCategory: any;
  rangeValues = [0, 1000000];
  minPriceInput: number = 0;
  maxPriceInput: number = 1000000;
  isLoading: boolean = false;
  isFilteringByPrice: boolean = false;

  constructor(
    private router: Router,
    private categoryService: CategoryService,
    private route: ActivatedRoute,
    private productService: ProductService,
    private cartService: CartService,
    private messageService: MessageService,
    private wishlistService: WishlistService
  ) {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
  }

  ngOnInit(): void {
    this.keyword = this.route.snapshot.params['keyword'];
    this.getListProduct();
    this.getListCategoryEnabled();
    this.getNewestProduct();

    this.minPriceInput = this.rangeValues[0];
    this.maxPriceInput = this.rangeValues[1];
  }

  getListProduct() {
    this.isLoading = true;
    this.isFilteringByPrice = false;

    this.productService.searchProduct(this.keyword).subscribe({
      next: (res) => {
        this.listProduct = res;
        this.isLoading = false;
        console.log(this.listProduct);
      },
      error: (err) => {
        console.log(err);
        this.isLoading = false;
      },
    });
  }

  getListCategoryEnabled() {
    this.categoryService.getListCategoryEnabled().subscribe({
      next: (res) => {
        this.listCategory = res;
      },
      error: (err) => {
        console.log(err);
      },
    });
  }

  getNewestProduct() {
    this.productService.getListProductNewest(4).subscribe({
      next: (res) => {
        this.listProductNewest = res;
      },
      error: (err) => {
        console.log(err);
      },
    });
  }

  getListProductByPriceRange() {
    this.updatePriceRange(); // Đảm bảo khoảng giá hợp lệ trước khi tìm kiếm
    this.isLoading = true;
    this.isFilteringByPrice = true;

    this.productService
      .searchProductByPriceRange(
        this.keyword,
        this.rangeValues[0],
        this.rangeValues[1]
      )
      .subscribe({
        next: (res) => {
          this.listProduct = res;
          this.isLoading = false;
          console.log('Sản phẩm theo khoảng giá:', this.listProduct);

          if (this.listProduct && this.listProduct.length === 0) {
            this.messageService.add({
              severity: 'info',
              summary: 'Notification',
              detail: `No product found in the price range from ${this.rangeValues[0].toLocaleString()} VNĐ to ${this.rangeValues[1].toLocaleString()} VNĐ`,
            });
          } else if (this.listProduct && this.listProduct.length > 0) {
            this.messageService.add({
              severity: 'success',
              summary: 'Success',
              detail: `Found ${this.listProduct.length} products in the selected price range`,
            });
          }
        },
        error: (err) => {
          this.isLoading = false;
          console.error('Error when filtering by price:', err);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail:
              'Could not search products by price range. Please try again later.',
          });
        },
      });
  }

  addToCart(item: any) {
    this.cartService.getItems();
    const success = this.cartService.addToCart(item, 1);

    if (success) {
      this.messageService.add({
        severity: 'success',
        summary: 'Thành công',
        detail: `${item.name} đã được thêm vào giỏ hàng của bạn!`,
        life: 1500,
      });
    } else {
      // Hiển thị thông báo hết hàng
      this.messageService.add({
        severity: 'warn',
        summary: 'Thông báo',
        detail: `Cửa hàng đã hết sản phẩm "${item.name}"!`,
        life: 1500,
      });
    }
  }

  addToWishList(item: any) {
    if (!this.wishlistService.productInWishList(item)) {
      this.wishlistService.addToWishList(item);
    }
  }

  updatePriceRange() {
    if (this.minPriceInput > this.maxPriceInput) {
      this.minPriceInput = this.maxPriceInput;
    }

    if (this.minPriceInput < 0) this.minPriceInput = 0;
    if (this.maxPriceInput > 5000000) this.maxPriceInput = 5000000;

    this.rangeValues = [this.minPriceInput, this.maxPriceInput];
  }

  resetPriceFilter() {
    this.minPriceInput = 0;
    this.maxPriceInput = 1000000;
    this.rangeValues = [0, 1000000];
    this.isFilteringByPrice = false;
    this.getListProduct(); // Lấy lại danh sách sản phẩm ban đầu
    this.messageService.add({
      severity: 'info',
      summary: 'Reset',
      detail: 'Reset price filter and display all products',
    });
  }

  applyPriceFilter() {
    this.updatePriceRange();
    this.getListProductByPriceRange();
  }
}
