package jp.bitspace.salon.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.bitspace.salon.dto.CreateReservationRequest;
import jp.bitspace.salon.model.Reservation;
import jp.bitspace.salon.service.ReservationService;

/**
 * 管理側予約テーブルコントローラ.
 */
@RestController
@RequestMapping("/api/customer/reservations")
public class CustomerReservationController {
	
    private final ReservationService reservationService;

    public CustomerReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }
	
    @GetMapping
    // 「引数のsalonId」と「ログイン中のsalonId」が一緒かチェック
    //@PreAuthorize("#salonId == authentication.principal.salonId")
    public List<Reservation> getReservations(@RequestParam(name = "salonId") Long salonId) {
    	// TODO 1か月単位＋前後マージンとか
        return reservationService.findBySalonId(salonId);
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
}
