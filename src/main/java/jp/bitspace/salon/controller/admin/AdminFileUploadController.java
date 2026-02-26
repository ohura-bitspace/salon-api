package jp.bitspace.salon.controller.admin;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jp.bitspace.salon.security.AdminRequestAuthUtil;
import jp.bitspace.salon.service.ImageUploadService;

/**
 * 管理者専用 画像ファイルアップロード（メニュー写真等の汎用アップロード）.
 */
@RestController
@RequestMapping("/api/admin/uploads")
public class AdminFileUploadController {

    private final AdminRequestAuthUtil adminRequestAuthUtil;
    private final ImageUploadService imageUploadService;

    public AdminFileUploadController(AdminRequestAuthUtil adminRequestAuthUtil, ImageUploadService imageUploadService) {
        this.adminRequestAuthUtil = adminRequestAuthUtil;
        this.imageUploadService = imageUploadService;
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
            @RequestParam("file") MultipartFile file) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        try {
            String imageUrl = imageUploadService.save(file);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }
}
