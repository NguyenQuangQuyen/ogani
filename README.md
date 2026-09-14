# 🌱 OGANI E-COMMERCE - HƯỚNG DẪN CÀI ĐẶT & CHẠY DỰ ÁN TỪ A-Z

> Tài liệu hướng dẫn chi tiết từng bước để thiết lập môi trường, cấu hình cơ sở dữ liệu, khởi chạy Backend (Spring Boot) và Frontend (Angular) sau khi tải source code về máy.

---

## 📑 MỤC LỤC
1. [Tổng quan công nghệ](#1-tổng-quan-công-nghệ)
2. [Yêu cầu môi trường cần cài đặt (Prerequisites)](#2-yêu-cầu-môi-trường-cần-cài-đặt-prerequisites)
3. [Bước 1: Cấu hình Cơ sở dữ liệu (PostgreSQL)](#3-bước-1-cấu-hình-cơ-sở-dữ-liệu-postgresql)
4. [Bước 2: Cài đặt và Chạy Backend (Spring Boot)](#4-bước-2-cài-đặt-và-chạy-backend-spring-boot)
5. [Bước 3: Cài đặt và Chạy Frontend (Angular)](#5-bước-3-cài-đặt-và-chạy-frontend-angular)
6. [Bước 4: Tài khoản và Kiểm tra hệ thống](#6-bước-4-tài-khoản-và-kiểm-tra-hệ-thống)
7. [Các lỗi thường gặp và cách xử lý (Troubleshooting)](#7-các-lỗi-thường-gặp-và-cách-xử-lý-troubleshooting)

---

## 1. 🛠 TỔNG QUAN CÔNG NGHỆ

| Thành phần | Công nghệ / Framework | Phiên bản khuyến nghị |
| :--- | :--- | :--- |
| **Backend** | Java Spring Boot (Data JPA, Security, Mail, WebClient) | **Java 17**, Spring Boot 3.2.3 |
| **Frontend** | Angular, PrimeNG, Bootstrap 5 | **Angular 15**, Node.js 18.x LTS |
| **Database** | PostgreSQL | **PostgreSQL 14 / 15 / 16** |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) | Swagger v3 |

---

## 2. 📋 YÊU CẦU MÔI TRƯỜNG CẦN CÀI ĐẶT (PREREQUISITES)

Trước khi chạy dự án, hãy đảm bảo máy tính của bạn đã cài đặt các công cụ sau:

### 1. Cài đặt **Java JDK 17**
- Tải JDK 17 tại: [Eclipse Adoptium (Temurin 17)](https://adoptium.net/temurin/releases/?version=17) hoặc [Oracle JDK 17](https://www.oracle.com/java/technologies/downloads/#java17).
- Kiểm tra sau khi cài đặt (mở Terminal/Command Prompt):
  ```bash
  java -version
  ```
  *(Kết quả hiển thị phiên bản `17.x.x` là thành công)*

### 2. Cài đặt **Node.js (Khuyến nghị bản v18 LTS)**
- Tải Node.js v18 tại: [Node.js Official Releases](https://nodejs.org/dist/latest-v18.x/)
- Kiểm tra sau khi cài đặt:
  ```bash
  node -v
  npm -v
  ```
  *(Node nên ở dải `v18.x.x` để tương thích hoàn hảo nhất với Angular 15)*

### 3. Cài đặt **PostgreSQL** & **pgAdmin**
- Tải và cài đặt PostgreSQL tại: [PostgreSQL Downloads](https://www.postgresql.org/download/)
- Ghi nhớ **Port** (mặc định là `5432`) và **Password** của user `postgres` mà bạn đặt trong quá trình cài đặt.

### 4. Công cụ lập trình (IDE) gợi ý
- **Backend:** IntelliJ IDEA (Community hoặc Ultimate), Eclipse, hoặc VS Code (cài Extension Pack for Java).
- **Frontend:** Visual Studio Code.

---

## 3. 🗄 BƯỚC 1: CẤU HÌNH CƠ SỞ DỮ LIỆU (POSTGRESQL)

Dự án đã có sẵn file script tạo bảng và dữ liệu mẫu tại:
`BackEnd/ogani/ogani_postgres.sql`

### Cách 1: Sử dụng giao diện **pgAdmin 4** (Dễ nhất cho người mới)
1. Mở ứng dụng **pgAdmin 4** và đăng nhập.
2. Tại cây danh mục bên trái, mở rộng: `Servers` -> `PostgreSQL...`.
3. Nhấp chuột phải vào **Databases** -> Chọn **Create** -> **Database...**
4. Đặt tên Database: `ogani` -> Nhấn **Save**.
5. Nhấp chuột phải vào database `ogani` vừa tạo -> Chọn **Query Tool**.
6. Nhấn nút **Open File** (biểu tượng thư mục) trên thanh công cụ của Query Tool và chọn file:
   `BackEnd/ogani/ogani_postgres.sql`
   *(hoặc mở file `ogani_postgres.sql` bằng Notepad/VS Code, sao chép toàn bộ nội dung và dán vào Query Tool)*.
7. Nhấn nút **Execute** (biểu tượng hình tam giác `▶` hoặc phím `F5`) để chạy file SQL.
8. Khi có thông báo `Query returned successfully`, cơ sở dữ liệu đã sẵn sàng.

### Cách 2: Sử dụng dòng lệnh `psql` (Terminal / CMD)
```bash
# Đăng nhập vào postgres và tạo database
psql -U postgres
# Nhập mật khẩu postgres của bạn khi được hỏi, sau đó gõ:
CREATE DATABASE ogani;
\q

# Import file dữ liệu vào database vừa tạo
psql -U postgres -d ogani -f "BackEnd/ogani/ogani_postgres.sql"
```

---

## 4. 🚀 BƯỚC 2: CÀI ĐẶT VÀ CHẠY BACKEND (SPRING BOOT)

### 1. Cập nhật thông tin Database trong cấu hình
Mở file cấu hình:
📁 `BackEnd/ogani/src/main/resources/application.properties`

Kiểm tra và sửa lại `username` / `password` nếu khác với máy tính của bạn:
```properties
# Cấu hình kết nối PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/ogani
spring.datasource.username=postgres
spring.datasource.password=123456 # <-- Thay bằng mật khẩu postgres của máy bạn
```

> **Lưu ý:** Các cấu hình bên dưới như `PayOS`, `Gemini AI`, `Google OAuth2`, `Gmail SMTP` đã được cấu hình sẵn mẫu. Bạn có thể giữ nguyên để chạy hoặc thay key riêng của bạn nếu cần phát triển thêm.

---

### 2. Khởi chạy Backend

#### **Cách 1: Chạy bằng dòng lệnh Terminal / Command Prompt**
1. Mở Terminal và di chuyển vào thư mục backend:
   ```bash
   cd BackEnd/ogani
   ```
2. Chạy ứng dụng:
   - **Trên Windows (CMD / PowerShell):**
     ```cmd
     mvnw.cmd spring-boot:run
     ```
   - **Trên macOS / Linux:**
     ```bash
     ./mvnw spring-boot:run
     ```

#### **Cách 2: Chạy trực tiếp từ IntelliJ IDEA (Khuyến nghị)**
1. Mở **IntelliJ IDEA** -> Chọn **Open** -> Điều hướng đến thư mục `BackEnd/ogani` (chọn file `pom.xml`).
2. Chờ IntelliJ tải các thư viện Maven hoàn tất (quan sát thanh tiến trình ở góc dưới bên phải).
3. Tìm đến file: `src/main/java/com/example/ogani/OganiApplication.java`.
4. Nhấp chuột phải vào file `OganiApplication.java` -> Chọn **Run 'OganiApplication'** (hoặc nhấn nút tam giác màu xanh `▶`).

---

### 3. Kiểm tra Backend đã hoạt động
- **Server URL:** `http://localhost:8080`
- **Tài liệu API (Swagger UI):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- Khi mở Swagger UI và thấy danh sách API hiện lên đầy đủ nghĩa là Backend đã chạy thành công! 🎉

---

## 5. 💻 BƯỚC 3: CÀI ĐẶT VÀ CHẠY FRONTEND (ANGULAR)

### 1. Di chuyển vào thư mục Frontend
Mở một cửa sổ **Terminal mới** (giữ cửa sổ Backend vẫn đang chạy) và gõ:
```bash
cd FrontEnd/ogani
```

### 2. Cài đặt các thư viện (Node Modules)
Chạy lệnh sau để tải các phụ thuộc:
```bash
npm install
```
> 💡 *Nếu bạn gặp lỗi xung đột version phụ thuộc (dependency conflict), hãy chạy lệnh:*
> ```bash
> npm install --legacy-peer-deps
> ```

### 3. Kiểm tra cấu hình kết nối API
File cấu hình URL Backend nằm tại: `FrontEnd/ogani/src/environments/environment.ts`
Mặc định đã trỏ đúng tới Backend:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
};
```

### 4. Khởi chạy ứng dụng Frontend
Chạy lệnh:
```bash
npm start
```
*(hoặc `npx ng serve --open`)*

### 5. Truy cập giao diện người dùng
- Trình duyệt sẽ tự động mở hoặc bạn có thể truy cập thủ công vào:
  👉 **[http://localhost:4200](http://localhost:4200)**

---

## 6. 👤 BƯỚC 4: TÀI KHOẢN VÀ KIỂM TRA HỆ THỐNG

### Tài khoản mẫu (Từ file SQL Dump):
- **Tài khoản Quản trị viên (Admin):**
  - **Username:** `admin`
  - **Email:** `admin@gmail.com`
  - **Quyền:** `ROLE_ADMIN`, `ROLE_MODERATOR`, `ROLE_USER`
- **Đăng ký tài khoản mới:** Bạn có thể nhấn vào nút **Register/Đăng ký** trên giao diện để tạo tài khoản người dùng mới và trải nghiệm mua hàng.

---

## 7. ❓ CÁC LỖI THƯỜNG GẶP VÀ CÁCH XỬ LÝ (TROUBLESHOOTING)

### ⚠️ Lỗi 1: `Connection to localhost:5432 refused` hoặc `password authentication failed for user "postgres"`
- **Nguyên nhân:** Dịch vụ PostgreSQL chưa được bật, hoặc sai mật khẩu trong `application.properties`.
- **Cách sửa:**
  - Mở **Services** trên Windows (gõ `services.msc` trong Start menu) -> Tìm `postgresql-x64-...` -> Nhấn **Start / Restart**.
  - Kiểm tra lại mật khẩu user `postgres` trong file `application.properties`.

---

### ⚠️ Lỗi 2: `npm install` báo lỗi `ERESOLVE could not resolve dependency`
- **Nguyên nhân:** Phiên bản Node.js/npm mới có cơ chế kiểm tra dependency nghiêm ngặt hơn.
- **Cách sửa:** Chạy cài đặt với cờ bỏ qua xung đột peer dependency:
  ```bash
  npm install --legacy-peer-deps
  ```

---

### ⚠️ Lỗi 3: Port `8080` hoặc `4200` đã bị chiếm dụng (`Port already in use`)
- **Nguyên nhân:** Có một ứng dụng khác (hoặc lần chạy trước chưa tắt hẳn) đang dùng port này.
- **Cách sửa:**
  - **Tắt tiến trình chiếm port trên Windows:**
    ```cmd
    # Tìm PID chiếm port 8080:
    netstat -ano | findstr :8080
    # Tắt tiến trình (thay <PID> bằng số tiến trình tìm được):
    taskkill /PID <PID> /F
    ```
  - Hoặc đối với Backend, bạn có thể đổi `server.port=8081` trong `application.properties` và cập nhật lại `apiUrl` tương ứng trong `FrontEnd/ogani/src/environments/environment.ts`.

---

### ⚠️ Lỗi 4: `class file has wrong version 61.0, should be 52.0...` hoặc `java.lang.UnsupportedClassVersionError`
- **Nguyên nhân:** Bạn đang dùng Java cũ (như Java 8 hoặc 11) trong khi dự án yêu cầu Java 17.
- **Cách sửa:** Cài đặt JDK 17 và cấu hình biến môi trường `JAVA_HOME` trỏ tới thư mục cài đặt JDK 17.

---

Chúc bạn cài đặt và trải nghiệm dự án **Ogani** thành công! 🎉 Nếu gặp bất kỳ khó khăn nào trong quá trình chạy, hãy kiểm tra lại từng bước theo hướng dẫn ở trên.
