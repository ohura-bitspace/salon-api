package jp.bitspace.salon.controller.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jp.bitspace.salon.security.AdminRequestAuthUtil;

/**
 * 管理者専用 画像ファイルアップロード.
 */
@RestController
@RequestMapping("/api/admin/uploads")
public class AdminFileUploadController {

    private final AdminRequestAuthUtil adminRequestAuthUtil;
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    // 許可する画像拡張子
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");
    
    // 許可する MIME type
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    public AdminFileUploadController(AdminRequestAuthUtil adminRequestAuthUtil) {
        this.adminRequestAuthUtil = adminRequestAuthUtil;
    }

    /**
     * 画像ファイルをアップロード.
     * @param httpServletRequest request
     * @param salonId サロンID
     * @param file アップロードファイル
     * @return imageUrl
     */
    @PostMapping
    public ResponseEntity<?> uploadImage(
        HttpServletRequest httpServletRequest,
        @RequestParam(name = "salonId") Long salonId,
        @RequestParam("file") MultipartFile file
    ) {
        // 管理者認可チェック
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        
        // ファイルが空でないかチェック
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "ファイルが選択されていません"));
        }
        
        // MIME type チェック
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "画像ファイル(jpg, png, gif, webp)のみアップロード可能です"));
        }
        
        // 拡張子チェック
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "ファイル名が不正です"));
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return ResponseEntity.badRequest().body(Map.of("error", "許可されていない拡張子です"));
        }
        
        try {
            // ユニークなファイル名を生成
            String fileName = UUID.randomUUID() + extension;
            
            // アップロードディレクトリが存在しない場合は作成
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // ファイルを保存
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // アクセス用URLを返す
            String imageUrl = "/uploads/" + fileName;
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
            
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "ファイルのアップロードに失敗しました: " + e.getMessage()));
        }
    }
    
    /**
     * ファイル名から拡張子を取得.
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}
