package com.example.ogani.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.ArrayList;
import java.nio.file.Files;
import java.util.HashMap;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

import com.example.ogani.entity.Image;
import com.example.ogani.exception.BadRequestException;
import com.example.ogani.exception.InternalServerException;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.service.ImageService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/image")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class ImageController {
    private static String UPLOAD_DIR = "D:/VS CODE/Angular/work/ogani/BackEnd/ogani/src/main/resources/static/photos/";
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    @Autowired
    private ImageService imageService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Khởi tạo và kiểm tra thư mục ảnh khi khởi động
    @PostConstruct
    public void init() {
        // Kiểm tra thư mục ảnh
        File uploadPath = new File(UPLOAD_DIR);
        
        // Tạo thư mục nếu không tồn tại
        if (!uploadPath.exists()) {
            boolean created = uploadPath.mkdirs();
            logger.info("Created photos directory: {} - Success: {}", uploadPath.getAbsolutePath(), created);
        }
        
        logger.info("Photos directory initialized at: {}", uploadPath.getAbsolutePath());
        
        // Liệt kê các file trong thư mục để debug
        File[] files = uploadPath.listFiles();
        if (files != null && files.length > 0) {
            logger.info("Found {} files in photos directory", files.length);
            for (int i = 0; i < Math.min(files.length, 5); i++) {
                logger.info(" - {}", files[i].getName());
            }
            if (files.length > 5) {
                logger.info(" - ... and {} more files", files.length - 5);
            }
        } else {
            logger.warn("No files found in photos directory");
        }
    }

    @GetMapping("/")
    public ResponseEntity<?> getList() {
        List<Image> listImage = imageService.getListImage();
        return ResponseEntity.ok(listImage);
    }

    @GetMapping("/user/{id}")
    @Operation(summary = "Lấy ra danh sách hình ảnh của user bằng user_id")
    public ResponseEntity<?> getListByUser(@PathVariable long userId) {
        List<Image> listImage = imageService.getListByUser(userId);
        return ResponseEntity.ok(listImage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy hình ảnh theo ID")
    @CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
    public ResponseEntity<Resource> getImage(@PathVariable long id) {
        try {
            logger.info("Request received for image with ID: {}", id);
            Image image = imageService.getImageById(id);
            
            // Nếu không có dữ liệu trong DB, tìm trong thư mục static/photos
            if (image.getData() == null || image.getData().length == 0) {
                logger.info("Image data is null, looking for file in filesystem: {}", image.getName());
                
                // Tìm file trong thư mục static/photos dựa vào tên lưu trong DB
                File file = new File(UPLOAD_DIR + image.getName());
                
                if (file.exists()) {
                    logger.info("File found: {}", file.getAbsolutePath());
                    
                    // Trả về file từ filesystem
                    FileSystemResource resource = new FileSystemResource(file);
                    
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getName() + "\"");
                    
                    return ResponseEntity.ok()
                            .headers(headers)
                            .contentLength(file.length())
                            .contentType(MediaType.parseMediaType("image/" + image.getType()))
                            .body(resource);
                } else {
                    logger.warn("File not found: {}", file.getAbsolutePath());
                    
                    // Kiểm tra tất cả các file trong thư mục
                    logger.info("Listing all files in directory: {}", UPLOAD_DIR);
                    File[] files = new File(UPLOAD_DIR).listFiles();
                    if (files != null) {
                        for (File f : files) {
                            logger.info("Found file: {}", f.getName());
                        }
                    }
                    
                    return ResponseEntity.notFound().build();
                }
            }
            
            // Nếu có dữ liệu trong DB, trả về như bình thường
            ByteArrayResource resource = new ByteArrayResource(image.getData());
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getName() + "\"");
            
            logger.info("Returning image with type: {}, size: {} bytes", image.getType(), image.getSize());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(image.getSize())
                    .contentType(MediaType.parseMediaType("image/" + image.getType()))
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error retrieving image with ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload")
    @Operation(summary = "Endpoint mới để upload ảnh vào hệ thống, khắc phục lỗi bytea/oid")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        logger.info("API /upload được gọi với file: {}", file.getOriginalFilename());
        
        // Tạo thư mục nếu chưa tồn tại
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            logger.info("Đã tạo thư mục: {}", UPLOAD_DIR);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            logger.error("Tên file không hợp lệ");
            throw new BadRequestException("Tên file không hợp lệ");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!extension.equals("png") && !extension.equals("jpg") && !extension.equals("gif")
                && !extension.equals("svg") && !extension.equals("jpeg")) {
            logger.error("Không hỗ trợ định dạng file: {}", extension);
                throw new BadRequestException("Không hỗ trợ định dạng file này");
            }

        try {
            // Tạo UUID cho tên file
                String uid = UUID.randomUUID().toString();
            String fileName = uid + "." + extension;
            String link = UPLOAD_DIR + fileName;
            
            logger.info("Đang lưu file vào: {}", link);
            
            // Lưu file vào thư mục static/photos
                File serverFile = new File(link);
            try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile))) {
                stream.write(file.getBytes());
            }
            
            logger.info("Đã lưu file thành công: {}", fileName);

            // Sử dụng JDBC trực tiếp thay vì JPA để khắc phục lỗi bytea/oid
            String sql = "INSERT INTO image (name, size, type, data) VALUES (?, ?, ?, NULL) RETURNING id";
            
            Long imageId = jdbcTemplate.queryForObject(
                sql, 
                new Object[]{fileName, file.getSize(), extension},
                Long.class
            );
            
            logger.info("Đã lưu thông tin ảnh vào DB với ID: {}", imageId);
            
            // Map cho response rõ ràng hơn
            Map<String, Object> response = new HashMap<>();
            response.put("id", imageId);
            response.put("name", fileName);
            response.put("size", file.getSize());
            response.put("type", extension);
            response.put("url", "/api/image/file/" + fileName);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Lỗi khi upload file: {}", e.getMessage(), e);
            throw new InternalServerException("Lỗi khi upload file: " + e.getMessage());
        }
    }

    @GetMapping("/update-photos")
    @Operation(summary = "Quét thư mục photos và cập nhật metadata vào database")
    public ResponseEntity<?> updatePhotosDatabase() {
        try {
            File directory = new File(UPLOAD_DIR);
            File[] files = directory.listFiles();
            List<Map<String, Object>> updatedImages = new ArrayList<>();
            
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                        
                        // Kiểm tra xem có phải file ảnh không
                        if (extension.equals("jpg") || extension.equals("jpeg") || 
                            extension.equals("png") || extension.equals("gif")) {
                            
                            try {
                                // Đọc file thành byte array
                                byte[] fileContent = Files.readAllBytes(file.toPath());
                                
                                // Tạo bản ghi mới trong database
                                Image image = new Image();
                                image.setName(fileName);
                                image.setSize(file.length());
                                image.setType(extension);
                                image.setData(fileContent); // Lưu dữ liệu binary
                                
                                image = imageService.save(image);
                                
                                Map<String, Object> imageInfo = new HashMap<>();
                                imageInfo.put("id", image.getId());
                                imageInfo.put("name", image.getName());
                                imageInfo.put("size", image.getSize());
                                imageInfo.put("type", image.getType());
                                updatedImages.add(imageInfo);
                                
                                logger.info("Added image to database: {}", fileName);
                            } catch (Exception e) {
                                logger.error("Error processing file " + fileName, e);
                            }
                        }
                    }
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Đã cập nhật " + updatedImages.size() + " ảnh vào database",
                "images", updatedImages
            ));
        } catch (Exception e) {
            logger.error("Error updating photos database", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    @GetMapping("/update-photos-new")
    @Operation(summary = "Quét thư mục photos và cập nhật metadata vào database (phiên bản mới)")
    public ResponseEntity<?> updatePhotosDatabaseNew() {
        try {
            logger.info("Starting to update photos from: {}", UPLOAD_DIR);
            File directory = new File(UPLOAD_DIR);
            
            if (!directory.exists()) {
                logger.error("Directory does not exist: {}", directory.getAbsolutePath());
                return ResponseEntity.badRequest().body("Directory not found: " + directory.getAbsolutePath());
            }
            
            File[] files = directory.listFiles();
            List<Map<String, Object>> updatedImages = new ArrayList<>();
            
            if (files == null) {
                logger.error("Failed to list files in directory");
                return ResponseEntity.badRequest().body("Failed to list files in directory");
            }
            
            logger.info("Found {} files in directory", files.length);
            
            for (File file : files) {
                if (!file.isFile()) {
                    logger.info("Skipping non-file: {}", file.getName());
                    continue;
                }
                
                String fileName = file.getName();
                logger.info("Processing file: {}", fileName);
                
                if (!fileName.toLowerCase().endsWith(".jpg") && 
                    !fileName.toLowerCase().endsWith(".jpeg") && 
                    !fileName.toLowerCase().endsWith(".png") && 
                    !fileName.toLowerCase().endsWith(".gif")) {
                    logger.info("Skipping non-image file: {}", fileName);
                    continue;
                }
                
                try {
                    // Tạo bản ghi mới không lưu dữ liệu nhị phân
                    Image image = new Image();
                    image.setName(fileName);
                    image.setSize(file.length());
                    
                    String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                    image.setType(extension);
                    
                    // Important: Set data to null explicitly
                    image.setData(null);
                    
                    // Use direct JDBC to insert
                    String sql = "INSERT INTO image (name, size, type, data) VALUES (?, ?, ?, NULL) RETURNING id";
                    Long id = jdbcTemplate.queryForObject(
                        sql, 
                        new Object[]{fileName, file.length(), extension},
                        Long.class
                    );
                    
                    logger.info("Successfully inserted image: {} with ID: {}", fileName, id);
                    
                    Map<String, Object> imageInfo = new HashMap<>();
                    imageInfo.put("id", id);
                    imageInfo.put("name", fileName);
                    imageInfo.put("size", file.length());
                    imageInfo.put("type", extension);
                    updatedImages.add(imageInfo);
                } catch (Exception e) {
                    logger.error("Error processing file {}: {}", fileName, e.getMessage());
                }
            }
            
            logger.info("Successfully updated {} images", updatedImages.size());
            
            return ResponseEntity.ok(Map.of(
                "message", "Đã cập nhật " + updatedImages.size() + " ảnh vào database",
                "images", updatedImages
            ));
        } catch (Exception e) {
            logger.error("Error updating photos database", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    @PostMapping("/link-to-product")
    @Operation(summary = "Liên kết ảnh với sản phẩm")
    public ResponseEntity<?> linkImageToProduct(
            @RequestParam("imageId") long imageId,
            @RequestParam("productId") long productId) {
        try {
            // Gọi service để liên kết image với product
            // (bạn cần tạo thêm service này)
            boolean success = imageService.linkImageToProduct(imageId, productId);
            
            if (success) {
                return ResponseEntity.ok("Đã liên kết ảnh với sản phẩm thành công");
            } else {
                return ResponseEntity.badRequest().body("Không thể liên kết ảnh với sản phẩm");
            }
            } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/check-broken-images")
    @Operation(summary = "Kiểm tra các ảnh bị lỗi trong database")
    public ResponseEntity<?> checkBrokenImages() {
        try {
            List<Image> images = imageService.getListImage();
            List<Long> brokenImageIds = new ArrayList<>();
            
            for (Image image : images) {
                // Kiểm tra xem ảnh có tồn tại trong thư mục không
                File file = new File(UPLOAD_DIR + image.getName());
                if (!file.exists()) {
                    brokenImageIds.add(image.getId());
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "totalImages", images.size(),
                "brokenImages", brokenImageIds.size(),
                "brokenImageIds", brokenImageIds
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/file/{fileName}")
    @Operation(summary = "Lấy hình ảnh theo tên file")
    @CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
    public ResponseEntity<Resource> getImageByFileName(@PathVariable String fileName) {
        try {
            logger.info("Getting image by fileName: {}", fileName);
            
            // Tạo đối tượng File từ UPLOAD_DIR và fileName
            File file = new File(UPLOAD_DIR + fileName);
            logger.info("Looking for file at path: {}", file.getAbsolutePath());
            
            if (!file.exists()) {
                logger.warn("File not found: {}", file.getAbsolutePath());
                
                // Liệt kê các file trong thư mục để debug
                logger.info("Files in directory {}:", UPLOAD_DIR);
                File[] files = new File(UPLOAD_DIR).listFiles();
                if (files != null && files.length > 0) {
                    for (int i = 0; i < Math.min(files.length, 5); i++) {
                        logger.info(" - {}", files[i].getName());
                    }
                    if (files.length > 5) {
                        logger.info(" - ... and {} more files", files.length - 5);
                    }
                } else {
                    logger.info("No files found in directory or directory empty");
                }
                
                return ResponseEntity.notFound().build();
            }
            
            logger.info("File exists, size: {} bytes", file.length());
            
            FileSystemResource resource = new FileSystemResource(file);
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.parseMediaType("image/" + extension))
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error getting image by fileName: {} - Error: {}", fileName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/check-path")
    public ResponseEntity<?> checkPath() {
        File directory = new File(UPLOAD_DIR);
        String[] fileList = directory.exists() ? directory.list() : new String[0];
        
        return ResponseEntity.ok(Map.of(
            "path", directory.getAbsolutePath(),
            "exists", directory.exists(),
            "fileCount", fileList != null ? fileList.length : 0,
            "files", fileList != null ? fileList : new String[0],
            "userDir", System.getProperty("user.dir")
        ));
    }

    @PostMapping("/upload-file")
    @Operation(summary = "Upload file vào thư mục static/photos (legacy endpoint)")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        logger.info("Legacy API /upload-file được gọi, chuyển tiếp tới /upload");
        // Chuyển tiếp tới phương thức mới
        return uploadImage(file);
    }

    /**
     * Trả về ảnh đại diện của người dùng
     */
    @GetMapping("/avatar/{filename:.+}")
    @Operation(summary = "Hiển thị ảnh đại diện người dùng")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        try {
            logger.info("Requested avatar: {}", filename);
            
            // Tạo đường dẫn đến file ảnh đại diện
            Path avatarPath = Paths.get("uploads/avatars/").resolve(filename);
            Resource resource = new UrlResource(avatarPath.toUri());
            
            // Kiểm tra xem file có tồn tại và có thể đọc được không
            if (resource.exists() && resource.isReadable()) {
                logger.info("Avatar found: {}", avatarPath.toString());
                
                // Xác định content type dựa trên phần mở rộng của file
                String contentType = "image/jpeg"; // Mặc định là JPEG
                if (filename.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.toLowerCase().endsWith(".gif")) {
                    contentType = "image/gif";
                }
                
                // Trả về file ảnh với content type phù hợp
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                logger.warn("Avatar not found: {}", avatarPath.toString());
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving avatar: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
