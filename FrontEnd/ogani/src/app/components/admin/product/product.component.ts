import { Component, OnInit } from '@angular/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { CategoryService } from 'src/app/_service/category.service';
import { ImageService } from 'src/app/_service/image.service';
import { ProductService } from 'src/app/_service/product.service';

@Component({
  selector: 'app-product',
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.css'],
  providers: [MessageService, ConfirmationService],
})
export class ProductComponent implements OnInit {
  listProduct: any;
  listCategory: any;
  listImage: any;

  disabled: boolean = true;
  apiUrl = 'http://hgr0a62zxby.sn.mynetname.net:2003/api/image/file/';
  timestamp = Date.now();

  selectedFiles?: FileList;
  currentFile?: File;

  listImageChoosen: any = [];
  imageChoosen: any;

  onUpdate: boolean = false;
  showForm: boolean = false;
  showImage: boolean = false;
  showDelete: boolean = false;

  productForm: any = {
    name: null,
    description: null,
    price: null,
    quantity: null,
    categoryId: null,
    imageIds: [],
  };

  checkCate: boolean = false;

  image: any;

  constructor(
    private messageService: MessageService,
    private productService: ProductService,
    private imageService: ImageService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.getListProduct();
    this.getListCategoryEnabled();
    this.getListImage();
  }

  goToCategories() {
    // Điều hướng đến trang quản lý danh mục
    window.location.href = '/admin/category';
  }

  openNew() {
    this.onUpdate = false;
    this.showForm = true;
    this.listImageChoosen = [];
    this.productForm = {
      id: null,
      name: null,
      description: null,
      price: null,
      quantity: null,
      categoryId: null,
      imageIds: [],
    };
  }

  openUpdate(data: any) {
    // Reset state
    this.listImageChoosen = [];
    this.image = null;
    this.imageChoosen = null;

    // Set form data
    this.onUpdate = true;
    this.showForm = true;
    this.productForm.id = data.id;
    this.productForm.name = data.name;
    this.productForm.description = data.description;
    this.productForm.price = data.price;
    this.productForm.quantity = data.quantity;
    this.productForm.categoryId = data.category.id;
    this.productForm.imageIds = []; // Reset imageIds

    // Add existing images
    if (data.images && data.images.length > 0) {
      data.images.forEach((res: any) => {
        this.listImageChoosen.push(res);
      });

      // Cập nhật timestamp để hiển thị ảnh mới nhất
      this.timestamp = Date.now();
    }
  }

  onChooseImage() {
    this.showImage = true;
    this.disabled = true;
    let data = document.querySelectorAll('.list-image img');
    data.forEach((i) => {
      i.classList.remove('choosen');
    });
  }

  getListProduct() {
    this.productService.getListProduct().subscribe({
      next: (res) => {
        this.listProduct = res;
        // Cập nhật timestamp để hiển thị ảnh mới nhất
        this.timestamp = Date.now();
        console.log('Products loaded:', res);
      },
      error: (err) => {
        console.error('Error loading products:', err);
        this.showError(err.message || 'Không thể tải danh sách sản phẩm');
      },
    });
  }

  getListCategoryEnabled() {
    this.categoryService.getListCategory().subscribe({
      next: (res) => {
        this.listCategory = res;
        console.log('Danh sách category:', this.listCategory);
        // Nếu không có category nào, hiển thị thông báo
        if (this.listCategory.length === 0) {
          this.messageService.add({
            severity: 'warn',
            summary: 'Warning',
            detail: 'No categories available. Please create categories first.',
          });
        }
      },
      error: (err) => {
        console.log(err);
        this.showError('Không thể tải danh sách danh mục');
      },
    });
  }

  getListImage() {
    this.imageService.getList().subscribe({
      next: (res) => {
        // Kiểm tra xem có ảnh nào không
        if (!res || (Array.isArray(res) && res.length === 0)) {
          this.messageService.add({
            severity: 'warn',
            summary: 'Warning',
            detail: 'No images available in the library',
          });
          this.listImage = [];
          return;
        }

        // Kiểm tra kiểu dữ liệu trả về
        if (!Array.isArray(res)) {
          console.error('Invalid response format for images:', res);
          this.messageService.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Image data format is not valid',
          });
          this.listImage = [];
          return;
        }

        // Sắp xếp ảnh để hiển thị ảnh mới nhất trước
        // Giả định rằng ảnh có id lớn hơn là ảnh mới nhất
        this.listImage = [...res].sort((a, b) => {
          // Nếu có id, sắp xếp theo id giảm dần
          if (a.id && b.id) {
            return b.id - a.id;
          }
          // Nếu không có id, giữ nguyên thứ tự
          return 0;
        });
      },
      error: (err) => {
        console.error('Error fetching images:', err);
        const errorMessage =
          err.error?.message || err.message || 'Lỗi không xác định';
        this.showError('Không thể tải danh sách ảnh: ' + errorMessage);
        this.listImage = [];
      },
    });
  }

  uploadFile(event: any) {
    this.selectedFiles = event.target.files;
    if (this.selectedFiles && this.selectedFiles.length > 0) {
      const file: File = this.selectedFiles.item(0)!;
      if (file) {
        this.currentFile = file;
        // Log chi tiết về file
        console.log(
          'Selected file:',
          file.name,
          'size:',
          file.size,
          'type:',
          file.type
        );

        // Hiển thị thông báo đang tải
        this.messageService.add({
          severity: 'info',
          summary: 'Info',
          detail: 'Đang tải ảnh lên...',
        });

        this.imageService.upload(this.currentFile).subscribe({
          next: (res: any) => {
            console.log('Upload response:', res);
            this.currentFile = undefined;

            // Xử lý phản hồi từ API upload mới
            if (res && res.id) {
              // API trả về đối tượng có ID và thông tin khác
              this.image = res;
              this.imageChoosen = res;

              // Thêm ảnh mới vào listImageChoosen (xóa các ảnh cũ)
              this.listImageChoosen = [res];

              this.showSuccess('Đã tải lên và chọn ảnh thành công');
              this.showImage = false; // Đóng dialog chọn ảnh

              // Làm mới danh sách ảnh và cập nhật timestamp
              this.getListImage();
              this.timestamp = Date.now();
            } else {
              this.showWarn(
                'Tải lên thành công nhưng không nhận được thông tin ảnh'
              );
              this.getListImage();
            }
          },
          error: (err) => {
            console.error('Upload error:', err);
            if (err.error) {
              console.error('Server error details:', err.error);
            }
            this.showError(
              'Không thể tải ảnh lên: ' +
                (err.error?.message || err.message || 'Lỗi không xác định')
            );
            this.currentFile = undefined;
          },
        });
      }
    } else {
      this.showWarn('Please select an image file');
    }
  }

  createProduct() {
    this.productForm.imageIds = []; // Reset imageIds array
    let data = this.listImageChoosen;
    data.forEach((res: any) => {
      this.productForm.imageIds.push(res.id);
    });

    const { name, description, price, quantity, categoryId, imageIds } =
      this.productForm;

    // Chuyển đổi categoryId thành số nếu cần
    const numericCategoryId =
      typeof categoryId === 'string' ? parseInt(categoryId, 10) : categoryId;

    console.log('Form data:', this.productForm);
    console.log(
      'Category ID:',
      numericCategoryId,
      'Type:',
      typeof numericCategoryId
    );

    this.productService
      .createProduct(
        name,
        description,
        price,
        quantity,
        numericCategoryId,
        imageIds
      )
      .subscribe({
        next: (res) => {
          this.getListProduct();
          this.showForm = false;
          this.showSuccess('Thêm mới thành công');
        },
        error: (err) => {
          this.showError(err.message || 'Lỗi khi tạo sản phẩm');
        },
      });
  }

  updateProduct() {
    this.productForm.imageIds = []; // Reset imageIds array
    let data = this.listImageChoosen;
    data.forEach((res: any) => {
      this.productForm.imageIds.push(res.id);
    });

    const { id, name, description, price, quantity, categoryId, imageIds } =
      this.productForm;

    // Chuyển đổi categoryId thành số nếu cần
    const numericCategoryId =
      typeof categoryId === 'string' ? parseInt(categoryId, 10) : categoryId;

    console.log('Form data:', this.productForm);
    console.log(
      'Category ID:',
      numericCategoryId,
      'Type:',
      typeof numericCategoryId
    );

    this.productService
      .updateProduct(
        id,
        name,
        description,
        price,
        quantity,
        numericCategoryId,
        imageIds
      )
      .subscribe({
        next: (res) => {
          this.getListProduct();
          this.showForm = false;
          this.showSuccess('Cập nhật thành công');
        },
        error: (err) => {
          this.showError(err.message || 'Lỗi khi cập nhật sản phẩm');
        },
      });
  }

  onDelete(id: number, name: string) {
    this.productForm.id = null;
    this.showDelete = true;
    this.productForm.id = id;
    this.productForm.name = name;
  }

  deleteProduct() {
    this.productService.deleteProduct(this.productForm.id).subscribe({
      next: (res) => {
        this.getListProduct();
        this.showWarn('Xóa thành công');
        this.showDelete = false;
      },
      error: (err) => {
        this.showError(err.message || 'Lỗi khi xóa sản phẩm');
      },
    });
  }

  // Phương thức xử lý khi chọn ảnh từ thư viện
  selectImage(event: any, res: any) {
    // Xóa lớp 'choosen' khỏi tất cả hình ảnh
    let data = document.querySelectorAll('.list-image img');
    data.forEach((i) => {
      i.classList.remove('choosen');
    });

    // Thêm lớp 'choosen' vào hình ảnh được chọn
    event.target.classList.add('choosen');
    this.imageChoosen = res;
    this.disabled = false;

    console.log('Selected image:', res);
  }

  // Phương thức xác nhận chọn ảnh từ thư viện
  chooseImage() {
    if (this.imageChoosen) {
      // Nếu đây là cập nhật, ta cần xóa ảnh cũ và thêm ảnh mới
      if (this.onUpdate) {
        // Cập nhật lại danh sách ảnh (chỉ lấy ảnh mới nhất)
        this.listImageChoosen = [this.imageChoosen];
      } else {
        // Nếu là tạo mới, ta chỉ lưu ảnh mới nhất
        this.listImageChoosen = [this.imageChoosen];
      }

      // Cập nhật biến image để hiển thị ảnh đã chọn
      this.image = this.imageChoosen;

      console.log('Images chosen:', this.listImageChoosen);
      this.showSuccess('Đã chọn ảnh cho sản phẩm');
      this.showImage = false;

      // Cập nhật timestamp để hiển thị ảnh mới nhất
      this.timestamp = Date.now();
    } else {
      this.showWarn('Please select an image');
    }
  }

  // Phương thức xử lý khi double-click vào ảnh (chọn nhanh)
  selectAndChooseImage(event: any, res: any) {
    // Chọn ảnh
    this.selectImage(event, res);

    // Xác nhận chọn ảnh
    this.imageChoosen = res;

    // Cập nhật danh sách ảnh được chọn (chỉ lấy ảnh mới nhất)
    this.listImageChoosen = [this.imageChoosen];

    // Cập nhật biến image để hiển thị ảnh đã chọn
    this.image = this.imageChoosen;

    this.showSuccess('Đã chọn ảnh cho sản phẩm');
    this.showImage = false; // Đóng dialog chọn ảnh

    // Cập nhật timestamp để hiển thị ảnh mới nhất
    this.timestamp = Date.now();
  }

  // Phương thức xóa ảnh đã chọn
  removeImage() {
    this.image = null;
    this.showWarn('Đã xóa ảnh');
  }

  showSuccess(text: string) {
    this.messageService.add({
      severity: 'success',
      summary: 'Success',
      detail: text,
    });
  }

  showError(text: string) {
    this.messageService.add({
      severity: 'error',
      summary: 'Error',
      detail: text,
    });
  }

  showWarn(text: string) {
    this.messageService.add({
      severity: 'warn',
      summary: 'Warning',
      detail: text,
    });
  }

  // Lấy tên danh mục từ ID
  getCategoryName(categoryId: number): string {
    if (!this.listCategory) return '';

    const category = this.listCategory.find(
      (cat: any) => cat.id === categoryId
    );
    return category ? category.name : '';
  }

  // Xử lý lỗi khi không tải được ảnh
  handleImageError(event: any): void {
    event.target.src = 'assets/image/cat-1.jpg';
  }
}
