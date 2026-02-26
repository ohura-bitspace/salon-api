package jp.bitspace.salon.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * 画像ファイルの検証・保存を担う共通サービス.
 * メニュー写真・施術写真など用途を問わず使用する.
 */
@Service
public class ImageUploadService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS =
            Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private static final List<String> ALLOWED_MIME_TYPES =
            Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp");

    /**
     * ファイルを検証してディスクに保存し、アクセス用URLを返す.
     *
     * @param file アップロードされたファイル
     * @return 画像アクセスURL（例: /uploads/xxxx.jpg）
     * @throws ResponseStatusException バリデーション失敗時は 400、保存失敗時は 500
     */
    public String save(MultipartFile file) {
        validate(file);

        String extension = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "ファイルの保存に失敗しました: " + e.getMessage());
        }

        return "/uploads/" + fileName;
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ファイルが選択されていません");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "画像ファイル(jpg, png, gif, webp)のみアップロード可能です");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ファイル名が不正です");
        }

        if (!ALLOWED_EXTENSIONS.contains(getExtension(originalFilename))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "許可されていない拡張子です");
        }
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx == -1 ? "" : filename.substring(idx).toLowerCase();
    }
}
