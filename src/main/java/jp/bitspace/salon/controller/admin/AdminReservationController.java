package jp.bitspace.salon.controller.admin;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import jp.bitspace.salon.dto.response.AdminReservationResponse;
import jp.bitspace.salon.model.Reservation;
import jp.bitspace.salon.model.ReservationItem;
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
            @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        // 管理API共通の認可チェック（STAFFトークン + salonId一致）
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);

        return reservationService.findBySalonIdAndDateRange(salonId, from, to);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReservation(@PathVariable Long id) {
        Optional<Reservation> reservation = reservationService.findById(id);
        if (reservation.isPresent()) {
            return ResponseEntity.ok(reservation.get());
        }
        return ResponseEntity.status(404).body(Map.of("error", "Reservation not found"));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<?> getReservationItems(@PathVariable Long id) {
        Optional<Reservation> reservation = reservationService.findById(id);
        if (reservation.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Reservation not found"));
        }
        List<ReservationItem> items = reservationService.findItemsByReservationId(id);
        return ResponseEntity.ok(items);
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable Long id, @RequestBody Reservation updated) {
        Optional<Reservation> existingOpt = reservationService.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Reservation not found"));
        }
        Reservation existing = existingOpt.get();

        existing.setStaffId(updated.getStaffId());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setStatus(updated.getStatus());
        existing.setMemo(updated.getMemo());

        return ResponseEntity.ok(reservationService.save(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable Long id) {
        reservationService.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
