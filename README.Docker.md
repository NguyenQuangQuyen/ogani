# Docker Deployment Guide

## Cấu trúc Dockerfile

Dự án bao gồm các Dockerfile sau:
- `BackEnd/ogani/Dockerfile` - Backend Spring Boot
- `FrontEnd/ogani/Dockerfile` - Frontend Angular
- `docker-compose.yml` - Orchestration cho toàn bộ hệ thống

## Chạy ứng dụng với Docker Compose

### 1. Build và chạy tất cả services:
```bash
docker-compose up --build
```

### 2. Chạy ở chế độ background:
```bash
docker-compose up -d
```

### 3. Xem logs:
```bash
# Tất cả services
docker-compose logs -f

# Chỉ backend
docker-compose logs -f backend

# Chỉ frontend
docker-compose logs -f frontend
```

### 4. Dừng các services:
```bash
docker-compose down
```

### 5. Dừng và xóa volumes:
```bash
docker-compose down -v
```

## Chạy riêng từng service

### Backend only:
```bash
cd BackEnd/ogani
docker build -t ogani-backend .
docker run -p 1999:1999 ogani-backend
```

### Frontend only:
```bash
cd FrontEnd/ogani
docker build -t ogani-frontend .
docker run -p 80:80 ogani-frontend
```

## Truy cập ứng dụng

- **Frontend**: http://localhost
- **Backend API**: http://localhost:1999
- **Swagger UI**: http://localhost:1999/swagger-ui.html
- **PostgreSQL**: localhost:5432

## Lưu ý

1. Đảm bảo ports 80, 1999, 5432 không bị sử dụng bởi ứng dụng khác
2. Database sẽ được khởi tạo tự động từ file `ogani_postgres.sql`
3. Dữ liệu database được lưu trong Docker volume `postgres_data`
4. Photos được lưu trong Docker volume `backend_photos`

## Troubleshooting

### Backend không kết nối được database:
```bash
docker-compose logs postgres
docker-compose restart backend
```

### Rebuild một service cụ thể:
```bash
docker-compose up -d --build backend
```

### Xem container đang chạy:
```bash
docker-compose ps
```
