import { Component, OnInit } from '@angular/core';
import { MessageService } from 'primeng/api';
import { BlogService } from 'src/app/_service/blog.service';
import { ImageService } from 'src/app/_service/image.service';
import { StorageService } from 'src/app/_service/storage.service';
import { TagService } from 'src/app/_service/tag.service';

@Component({
  selector: 'app-blog',
  templateUrl: './blog.component.html',
  styleUrls: ['./blog.component.css'],
  providers: [MessageService]

})
export class BlogComponent implements OnInit {

  listBlog: any;
  listTag: any[] = [];
  listImage: any;
  username: any;
  selectedTags: any[] = [];
  statusOptions: any[] = [
    { name: 'Active', value: 1 },
    { name: 'Inactive', value: 0 }
  ];

  onUpdate: boolean = false;
  showForm: boolean = false;
  showImage: boolean = false;
  onDelete: boolean = false;
  imageChoosen: any;
  image: any;
  disabled: boolean = true;

  selectedFiles?: FileList;
  currentFile?: File;


  blogForm: any = {
    id: null,
    title: null,
    description: null,
    content: null,
    imageId: null,
    tags: [],
  }

  constructor(private blogService: BlogService, private storageService: StorageService, private tagService: TagService, private imageService: ImageService, private messageService: MessageService) {

  }

  ngOnInit(): void {
    this.username = this.storageService.getUser().username;
    this.getList();
    this.getListTag();
    this.getListImage();
  }


  uploadFile(event: any) {
    this.selectedFiles = event.target.files;
    if (this.selectedFiles && this.selectedFiles.length > 0) {
      const file: File = this.selectedFiles.item(0)!;
      if (file) {
        this.currentFile = file;
        console.log('Selected file:', file.name, 'size:', file.size, 'type:', file.type);

        this.messageService.add({ severity: 'info', summary: 'Notification', detail: 'Uploading image...' });

        this.imageService.upload(this.currentFile).subscribe({
          next: (res: any) => {
            console.log('Upload response:', res);
            this.currentFile = undefined;

            if (res && res.id) {
              this.image = res;
              this.imageChoosen = res;
              this.showSuccess("Image uploaded and selected successfully");
              this.showImage = false;
              this.getListImage();
            } else {
              this.showWarn("Upload successful but no image information received");
              this.getListImage();
            }
          },
          error: (err) => {
            console.error('Upload error:', err);
            if (err.error) {
              console.error('Server error details:', err.error);
            }
            this.showError("Could not upload image: " + (err.error?.message || err.message || "Unknown error"));
            this.currentFile = undefined;
          }
        });
      }
    } else {
      this.showWarn("Please select an image file");
    }
  }

  showNew() {
    this.onUpdate = false;
    this.showForm = true;
    this.image = null;
    this.blogForm = {
      id: null,
      title: null,
      description: null,
      content: null,
      imageId: null,
      tags: [],
    }
  }
  showUpdate(data: any) {
    this.selectedTags = [];
    this.onUpdate = true;
    this.showForm = true;
    this.blogForm.id = data.id;
    this.blogForm.title = data.title;
    this.blogForm.description = data.description;
    this.blogForm.content = data.content;
    this.image = data.image;
    data.tags.forEach((res: any) => {
      this.selectedTags.push(res.id);
    })
  }

  showDelete(id: number, title: string) {
    this.onDelete = true;
    this.blogForm.id = id;
    this.blogForm.title = title;
  }


  onChooseImage() {
    this.showImage = true;
    this.disabled = true;
    let data = document.querySelectorAll('.list-image img');
    data.forEach(i => {
      i.classList.remove('choosen');
    })
  }

  getListImage() {
    this.imageService.getList().subscribe({
      next: res => {
        // Show loading message
        this.messageService.add({ severity: 'info', summary: 'Notification', detail: 'Loading image list...' });

        // Check if there are any images
        if (!res || (Array.isArray(res) && res.length === 0)) {
          this.messageService.add({ severity: 'warn', summary: 'Warning', detail: 'No images in library' });
          this.listImage = [];
          return;
        }

        // Check response data type
        if (!Array.isArray(res)) {
          console.error('Invalid response format for images:', res);
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Invalid image data format' });
          this.listImage = [];
          return;
        }

        // Sort images to show newest first
        // Assuming images with higher ids are newer
        this.listImage = [...res].sort((a, b) => {
          // If ids exist, sort by id in descending order
          if (a.id && b.id) {
            return b.id - a.id;
          }
          // If no ids, maintain original order
          return 0;
        });

        // Show number of images loaded
        this.messageService.add({
          severity: 'success',
          summary: 'Success',
          detail: `Loaded ${this.listImage.length} images from library`
        });
      },
      error: err => {
        console.error('Error fetching images:', err);
        const errorMessage = err.error?.message || err.message || "Unknown error";
        this.showError("Could not load image list: " + errorMessage);
        this.listImage = [];
      }
    });
  }


  getList() {
    this.blogService.getList().subscribe({
      next: res => {
        this.listBlog = res;
      }, error: err => {
        console.log(err);
      }
    })
  }

  getListTag() {
    this.tagService.getListTag().subscribe({
      next: res => {
        this.listTag = res;
      }, error: err => {
        console.log(err);
      }
    })
  }

  createBlog() {
    this.blogForm.imageId = this.image?.id;
    this.blogForm.tags = this.selectedTags;
    const { title, description, content, imageId, tags } = this.blogForm;
    this.blogService.createBlog(title, description, content, imageId, tags, this.username).subscribe({
      next: (res: any) => {
        this.showSuccess("Blog created successfully");
        this.showForm = false;
        this.getList();
      }, error: (err: any) => {
        this.showError("Could not create blog: " + (err.error?.message || err.message || "Unknown error"));
      }
    })
  }

  updateBlog() {
    this.blogForm.imageId = this.image?.id;
    this.blogForm.tags = this.selectedTags;
    const { id, title, description, content, imageId, tags } = this.blogForm;
    this.blogService.updateBLog(id, title, description, content, imageId, tags).subscribe({
      next: (res: any) => {
        this.showSuccess("Blog updated successfully");
        this.showForm = false;
        this.getList();
      }, error: (err: any) => {
        this.showError("Could not update blog: " + (err.error?.message || err.message || "Unknown error"));
      }
    })
  }

  deleteBlog() {
    this.blogService.deleleBlog(this.blogForm.id).subscribe({
      next: (res: any) => {
        this.showSuccess("Blog deleted successfully");
        this.onDelete = false;
        this.getList();
      }, error: (err: any) => {
        this.showError("Could not delete blog: " + (err.error?.message || err.message || "Unknown error"));
      }
    })
  }

  selectImage(event: any, res: any) {
    let data = document.querySelectorAll('.list-image img');
    data.forEach(i => {
      i.classList.remove('choosen');
    })
    event.target.classList.toggle("choosen");
    this.imageChoosen = res;
    this.disabled = false;
  }

  // Phương thức xử lý sự kiện double-click trên ảnh
  selectAndChooseImage(event: any, res: any) {
    // Đầu tiên chọn ảnh (highlight)
    this.selectImage(event, res);

    // Sau đó chọn ảnh và đóng dialog
    if (this.imageChoosen) {
      this.image = this.imageChoosen;
      this.showSuccess("Đã chọn ảnh cho bài viết");
      this.showImage = false; // Đóng dialog chọn ảnh
    }
  }

  // Phương thức xóa ảnh đã chọn
  removeImage() {
    this.image = null;
    this.showWarn("Đã xóa ảnh");
  }

  chooseImage() {
    this.image = this.imageChoosen;
    this.showImage = false;
  }


  showSuccess(text: string) {
    this.messageService.add({ severity: 'success', summary: 'Success', detail: text }); ``
  }
  showError(text: string) {
    this.messageService.add({ severity: 'error', summary: 'Error', detail: text });
  }

  showWarn(text: string) {
    this.messageService.add({ severity: 'warn', summary: 'Warn', detail: text });
  }
}
