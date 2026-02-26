package jp.bitspace.salon.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jp.bitspace.salon.dto.response.VisitPhotoDto;
import jp.bitspace.salon.security.AdminRequestAuthUtil;
import jp.bitspace.salon.service.VisitPhotoService;

/**
 * 施術写真コントローラ.
 */
@RestController
@RequestMapping("/api/visit-histories/{visitId}/photos")
public class AdminVisitPhotoController {

    private final VisitPhotoService visitPhotoService;
    private final AdminRequestAuthUtil adminRequestAuthUtil;

    public AdminVisitPhotoController(VisitPhotoService visitPhotoService, AdminRequestAuthUtil adminRequestAuthUtil) {
        this.visitPhotoService = visitPhotoService;
        this.adminRequestAuthUtil = adminRequestAuthUtil;
    }

    /**
     * 施術写真一覧取得.
     * GET /api/visit-histories/{visitId}/photos?salonId={salonId}
     */
    @GetMapping
    public ResponseEntity<List<VisitPhotoDto>> getPhotos(
            HttpServletRequest httpServletRequest,
            @PathVariable Long visitId,
            @RequestParam Long salonId) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        return ResponseEntity.ok(visitPhotoService.getPhotos(visitId, salonId));
    }

    /**
     * 施術写真アップロード.
     * POST /api/visit-histories/{visitId}/photos?salonId={salonId}
     */
    @PostMapping
    public ResponseEntity<VisitPhotoDto> uploadPhoto(
            HttpServletRequest httpServletRequest,
            @PathVariable Long visitId,
            @RequestParam Long salonId,
            @RequestParam("file") MultipartFile file) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        VisitPhotoDto dto = visitPhotoService.uploadPhoto(visitId, salonId, file);
        return ResponseEntity.ok(dto);
    }

    /**
     * 施術写真削除.
     * DELETE /api/visit-histories/{visitId}/photos/{photoId}?salonId={salonId}
     */
    @DeleteMapping("/{photoId}")
    public ResponseEntity<?> deletePhoto(
            HttpServletRequest httpServletRequest,
            @PathVariable Long visitId,
            @PathVariable Long photoId,
            @RequestParam Long salonId) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        visitPhotoService.deletePhoto(visitId, photoId, salonId);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
