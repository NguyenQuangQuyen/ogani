# ✅ Cách đơn giản nhất để Upload Ảnh

## 🎯 Giải pháp

Sử dụng đường dẫn cố định trong container: `/app/src/main/resources/static/photos/`

## 🚀 Chỉ cần làm 2 bước:

### Bước 1: Rebuild Docker (1 lần duy nhất)

```bash
docker-compose down
docker-compose build backend
docker-compose up -d
```

### Bước 2: Xong!

Không cần:
- ❌ Cấu hình volume
- ❌ Biến môi trường
- ❌ Mount thư mục
- ❌ Phức tạp gì cả

## 📝 Lưu ý

⚠️ **File sẽ bị mất khi xóa container**

Nếu bạn chạy `docker-compose down` hoặc xóa container, file upload sẽ mất.

**Giải pháp nếu muốn giữ file:**
```yaml
# Thêm vào docker-compose.yml
services:
  backend:
    volumes:
      - backend-photos:/app/src/main/resources/static/photos

volumes:
  backend-photos:
```

Nhưng nếu không cần giữ file lâu dài → Không cần làm gì cả!

## 🔍 Kiểm tra

```bash
# Xem log
docker-compose logs backend | grep "UPLOAD"

# Test upload
curl -X POST http://localhost:1999/api/image/upload -F "file=@test.jpg"

# Xem file trong container
docker exec ogani-backend ls -la /app/src/main/resources/static/photos/
```

## ✅ Kết luận

**Đơn giản nhất:**
1. Sửa `application.properties` → `upload.dir=/app/src/main/resources/static/photos/`
2. Rebuild: `docker-compose build backend`
3. Restart: `docker-compose up -d`
4. Xong! 🎉
