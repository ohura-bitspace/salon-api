package jp.bitspace.salon.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 画像ファイルアップロード.
 */
@RestController
@RequestMapping("/api/uploads")
public class FileUploadController {

    // 保存先フォルダ (実際は application.properties で設定)
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 1. ファイル名をユニークにする (uuid_originalName.jpg)
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);

            // 2. サーバーに保存
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // 3. アクセス用URLを返す
            return ResponseEntity.ok("/uploads/" + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed");
        }
    }
}
