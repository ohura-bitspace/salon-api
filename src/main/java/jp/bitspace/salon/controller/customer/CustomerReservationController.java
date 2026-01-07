package jp.bitspace.salon.controller.customer;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jp.bitspace.salon.dto.request.CreateReservationRequest;
import jp.bitspace.salon.dto.response.ReservationTimeSlotDto;
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
	
    @GetMapping
    // 「引数のsalonId」と「ログイン中のsalonId」が一緒かチェック
    //@PreAuthorize("#salonId == authentication.principal.salonId")
    public List<ReservationTimeSlotDto> getReservations(@RequestParam(name = "salonId") Long salonId) {
    	// TODO 認証チェック（後回し）
    	
    	// 現在日-1日から、60日後で取得する
    	LocalDate from = LocalDate.now().minusDays(1);
    	LocalDate to = LocalDate.now().plusDays(60);
    	
        return customerService.findReservationTimeSlotsBySalonIdAndDateRange(salonId, from, to);
    }
    
    /**
     * 予約作成.
     * @param request 予約リクエスト
     * @return レスポンス
     */
    @PostMapping
    public ResponseEntity<?> createReservation(@Valid @RequestBody CreateReservationRequest request) {
    	// TODO 認証チェック（後回し）
    	
        try {
            Reservation created = reservationService.createWithItems(request);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
    
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

        if (principal == null) {
            // セキュリティ設定上ここには来ない想定だが、念のため
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (principal.getSalonId() != null && !principal.getSalonId().equals(salonId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        
        List<VisitHistoryDto> visitHistoryList = customerService.getVisitHistory(principal.getCustomerId(), salonId);
        for (VisitHistoryDto visitHistoryDto : visitHistoryList) {
        	System.out.println("visitHistoryDto=" + visitHistoryDto);
		}
        
        return visitHistoryList;
    }
    
    /**
     * 予約キャンセル.
     * <p>
     * 顧客が自分の予約をキャンセルします。
     * ステータスを CANCELED に変更します。
     * 
     * @param reservationId 予約ID
     * @param salonId サロンID（バリデーション用）
     * @param principal ログイン中の顧客情報
     * @return キャンセル成功メッセージ
     */
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Map<String, Object>> cancelReservation(
            @PathVariable Long reservationId,
            @RequestParam(name = "salonId") Long salonId,
            @AuthenticationPrincipal CustomerPrincipal principal) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        if (principal.getSalonId() != null && !principal.getSalonId().equals(salonId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        reservationService.cancelReservation(reservationId, principal.getCustomerId(), salonId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "キャンセルしました"
        ));
    }
    
    // TODO [後回し]予約更新
}
