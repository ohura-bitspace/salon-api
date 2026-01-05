package jp.bitspace.salon.controller.customer;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jp.bitspace.salon.dto.request.CreateReservationRequest;
import jp.bitspace.salon.dto.response.VisitHistoryDto;
import jp.bitspace.salon.model.Reservation;
import jp.bitspace.salon.security.CustomerPrincipal;
import jp.bitspace.salon.service.CustomerService;
import jp.bitspace.salon.service.ReservationService;

/**
 * 管理側予約テーブルコントローラ.
 */
@RestController
@RequestMapping("/api/customer/reservations")
public class CustomerReservationController {
	
    private final ReservationService reservationService;
    private final CustomerService customerService;

    public CustomerReservationController(ReservationService reservationService, CustomerService customerService) {
        this.reservationService = reservationService;
        this.customerService = customerService;
    }
	
    // TODO 修正
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
    
    // TODO 予約削除
    // TODO [後回し]予約更新
    
    /**
     * 予約履歴一覧（来店済み）.
     * <p>
     * 戻り値は VisitHistoryDto を流用。
     * memo / treatmentMemo は現時点では null を返します。
     */
    @GetMapping("/history")
    public List<VisitHistoryDto> getHistory(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @RequestParam(name = "salonId") Long salonId) {
    	System.out.println("@history:" + "salonId=" + salonId);

        if (principal == null) {
            // セキュリティ設定上ここには来ない想定だが、念のため
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (principal.getSalonId() != null && !principal.getSalonId().equals(salonId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        return customerService.getVisitHistory(principal.getCustomerId(), salonId);
    }
}
