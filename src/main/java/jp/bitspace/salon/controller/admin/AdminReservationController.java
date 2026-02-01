package jp.bitspace.salon.controller.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jp.bitspace.salon.dto.request.CreateReservationRequest;
import jp.bitspace.salon.dto.request.UpdateReservationRequest;
import jp.bitspace.salon.dto.response.AdminReservationResponse;
import jp.bitspace.salon.model.Reservation;
import jp.bitspace.salon.security.AdminRequestAuthUtil;
import jp.bitspace.salon.service.ReservationService;

/**
 * 管理側予約テーブルコントローラ.
 */
@RestController
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {
    private final ReservationService reservationService;
    private final AdminRequestAuthUtil adminRequestAuthUtil;

    public AdminReservationController(ReservationService reservationService, AdminRequestAuthUtil adminRequestAuthUtil) {
        this.reservationService = reservationService;
        this.adminRequestAuthUtil = adminRequestAuthUtil;
    }
    
    // 管理側の予約取得（期間指定）.
	@GetMapping
	public List<AdminReservationResponse> getReservations(
			HttpServletRequest httpServletRequest,
			@RequestParam(name = "salonId") Long salonId,
			@RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		// ステータスが「キャンセル」は対象外としている
		
		// 管理API共通の認可チェック（STAFFトークン + salonId一致）
		adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
		// 予約リスト取得
		List<AdminReservationResponse> responseList = reservationService.findBySalonIdAndDateRange(salonId, from, to);
		return responseList;
	}

    @GetMapping("/{id}")
    public ResponseEntity<AdminReservationResponse> getReservation(
            HttpServletRequest httpServletRequest,
            @PathVariable Long id,
            @RequestParam(name = "salonId") Long salonId
    ) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        AdminReservationResponse response = reservationService.getAdminReservationResponse(id, salonId);
        System.out.println("予約情報：" + response);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 予約作成.
     * @param request 予約リクエスト
     * @return レスポンス
     */
    @PostMapping
    public ResponseEntity<?> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        try {
            Reservation created = reservationService.createWithItems(request);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
    
    /**
     * 更新処理
     * @param id id
     * @param request リクエスト
     * @return レスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable Long id, @Valid @RequestBody UpdateReservationRequest request) {
        System.out.println("予約更新：" + request);
    	
    	try {
            Reservation updated = reservationService.updateWithItems(id, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
    
    /**
     * 予約削除.
     * @param id id 
     * @return レスポンス
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable Long id) {
        reservationService.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
