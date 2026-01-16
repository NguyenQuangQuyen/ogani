# 🚀 Fix Upload - Chỉ cần RESTART (không cần rebuild)

## ✅ Đã sửa gì?

1. **docker-compose.yml**: Thêm `UPLOAD_DIR: /app/src/main/resources/static/photos/`
2. **application.properties**: Đọc từ biến môi trường `${UPLOAD_DIR:...}`

## 🎯 Chỉ cần RESTART (KHÔNG cần rebuild!)

```bash
# Trên server, chạy lệnh này:
docker-compose down
docker-compose up -d
```

**Lý do:** Biến môi trường được set khi container khởi động, không cần rebuild image!

## 🔍 Kiểm tra sau khi restart

```bash
# 1. Kiểm tra biến môi trường
docker exec ogani-backend env | grep UPLOAD_DIR

# Kết quả phải là:
# UPLOAD_DIR=/app/src/main/resources/static/photos/

# 2. Xem log khởi động
docker logs ogani-backend 2>&1 | grep "UPLOAD CONFIGURATION"

# Phải thấy:
# Upload directory configured: /app/src/main/resources/static/photos/
# Can write: true

# 3. Kiểm tra file hiện có
docker exec ogani-backend ls -la /app/src/main/resources/static/photos/
```

## 🧪 Test upload

```bash
# Upload file mới
curl -X POST http://localhost:1999/api/image/upload -F "file=@test.jpg"

# Kiểm tra file
docker exec ogani-backend ls -la /app/src/main/resources/static/photos/
```

## ✅ Kết quả mong đợi

- Upload file → Lưu vào `/app/src/main/resources/static/photos/`
- Đọc file → Tìm ở `/app/src/main/resources/static/photos/`
- ✅ Thành công!

## 📝 Tóm tắt

**Chỉ cần:**
1. `docker-compose down`
2. `docker-compose up -d`
3. Xong! 🎉

**KHÔNG cần:**
- ❌ Rebuild image
- ❌ Sửa code thêm
- ❌ Cấu hình phức tạp
