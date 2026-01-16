# 🐳 Hướng dẫn Upload File với Docker

## ⚠️ Vấn đề phát hiện

Từ log của bạn:
```
Saving file to: /app/src/main/resources/static/photos/avatar_admin_1768533622971.jpg
File saved successfully: true
```

Nhưng khi đọc lại:
```
Looking for file at path: /home/dnm/Project/Ogani/BackEnd/ogani/src/main/resources/static/photos/avatar_admin_1768533622971.jpg
File not found
```

**Nguyên nhân:** Ứng dụng chạy trong Docker container, đường dẫn file khác với đường dẫn trên host.

## ✅ Giải pháp đã áp dụng

### 1. Cấu hình trong `application.properties`
```properties
upload.dir=${UPLOAD_DIR:/app/photos/}
```

### 2. Cấu hình trong `docker-compose.yml`
```yaml
environment:
  UPLOAD_DIR: /app/photos/
volumes:
  - ./backend-photos:/app/photos
```

### 3. Dockerfile đã tạo thư mục
```dockerfile
RUN mkdir -p /app/photos
```

## 🚀 Cách triển khai

### Bước 1: Rebuild Docker image

```bash
# Dừng container hiện tại
docker-compose down

# Rebuild image với code mới
docker-compose build backend

# Khởi động lại
docker-compose up -d
```

### Bước 2: Kiểm tra log

```bash
# Xem log của backend
docker-compose logs -f backend
```

Bạn sẽ thấy:
```
=== UPLOAD CONFIGURATION ===
Upload directory configured: /app/photos/
Absolute path: /app/photos
Directory exists: true
Can read: true
Can write: true
=== END UPLOAD CONFIGURATION ===
```

### Bước 3: Test upload

```bash
# Test upload
curl -X POST http://localhost:1999/api/image/upload \
  -F "file=@test-image.jpg"
```

### Bước 4: Kiểm tra file trên host

```bash
# File sẽ được lưu trong thư mục backend-photos trên host
ls -la ./backend-photos/
```

## 📂 Cấu trúc thư mục

```
ogani/
├── docker-compose.yml
├── backend-photos/          ← File upload sẽ ở đây (trên host)
│   └── avatar_xxx.jpg
└── BackEnd/
    └── ogani/
        ├── Dockerfile
        └── src/
            └── main/
                └── resources/
                    └── application.properties
```

**Trong Docker container:**
- `/app/photos/` ← Đường dẫn trong container
- Mount tới `./backend-photos` trên host

## 🔧 Troubleshooting

### Vấn đề 1: File vẫn không tìm thấy sau khi upload

**Kiểm tra:**
```bash
# Vào trong container
docker exec -it ogani-backend bash

# Kiểm tra thư mục
ls -la /app/photos/

# Kiểm tra biến môi trường
echo $UPLOAD_DIR
```

**Giải pháp:**
```bash
# Rebuild lại image
docker-compose build --no-cache backend
docker-compose up -d
```

### Vấn đề 2: Permission denied

**Kiểm tra quyền trên host:**
```bash
ls -la ./backend-photos/
```

**Sửa quyền:**
```bash
chmod 777 ./backend-photos/
```

### Vấn đề 3: Thư mục backend-photos chưa tồn tại

```bash
# Tạo thư mục trên host
mkdir -p ./backend-photos
chmod 755 ./backend-photos
```

### Vấn đề 4: File upload vào sai thư mục

**Kiểm tra log khi upload:**
```bash
docker-compose logs -f backend | grep "Saving file to"
```

Phải thấy:
```
Saving file to: /app/photos/filename.jpg
```

Nếu thấy đường dẫn khác → Biến môi trường chưa được set đúng.

## 🔄 So sánh: Local vs Docker

### Chạy Local (không dùng Docker)
```bash
# Set biến môi trường
export UPLOAD_DIR=D:/ogani/photos/

# Hoặc trong IDE (IntelliJ/Eclipse)
# Run Configuration → Environment Variables:
# UPLOAD_DIR=D:/ogani/photos/
```

### Chạy Docker (production)
```yaml
# docker-compose.yml
environment:
  UPLOAD_DIR: /app/photos/
volumes:
  - ./backend-photos:/app/photos
```

## 📝 Lưu ý quan trọng

1. ✅ **Luôn kết thúc đường dẫn bằng `/`**
   - Đúng: `/app/photos/`
   - Sai: `/app/photos`

2. ✅ **Volume mount phải khớp với UPLOAD_DIR**
   - UPLOAD_DIR: `/app/photos/`
   - Volume: `./backend-photos:/app/photos`

3. ✅ **Rebuild image sau khi thay đổi code**
   ```bash
   docker-compose build backend
   ```

4. ✅ **Kiểm tra log sau khi khởi động**
   ```bash
   docker-compose logs backend | grep "UPLOAD CONFIGURATION"
   ```

5. ✅ **File trên host sẽ ở `./backend-photos/`**
   - Có thể backup, copy, hoặc xem trực tiếp

## 🎯 Kết quả mong đợi

Sau khi áp dụng:

1. Upload file → Lưu vào `/app/photos/` trong container
2. File xuất hiện trong `./backend-photos/` trên host
3. Đọc file → Tìm thấy tại `/app/photos/` trong container
4. Không còn lỗi "File not found"

## 🚀 Quick Fix

Nếu đang gặp lỗi ngay bây giờ:

```bash
# 1. Rebuild
docker-compose build backend

# 2. Restart
docker-compose up -d backend

# 3. Xem log
docker-compose logs -f backend

# 4. Test
curl -X POST http://localhost:1999/api/image/upload -F "file=@test.jpg"
```

Xong! 🎉
