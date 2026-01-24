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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jp.bitspace.salon.dto.request.CreateReservationRequest;
import jp.bitspace.salon.dto.response.ReservationTimeSlotDto;
import jp.bitspace.salon.dto.response.StaffResponse;
import jp.bitspace.salon.dto.response.VisitHistoryDto;
import jp.bitspace.salon.model.Reservation;
import jp.bitspace.salon.security.CustomerPrincipal;
import jp.bitspace.salon.service.CustomerService;
import jp.bitspace.salon.service.ReservationService;
import jp.bitspace.salon.service.StaffService;

/**
 * 管理側予約テーブルコントローラ.
 */
@RestController
@RequestMapping("/api/customer/reservations")
public class CustomerReservationController {
	
    private final ReservationService reservationService;
    private final CustomerService customerService;
    private final StaffService staffService;

    public CustomerReservationController(ReservationService reservationService, CustomerService customerService, StaffService staffService) {
        this.reservationService = reservationService;
        this.customerService = customerService;
        this.staffService = staffService;
    }
	
    /**
     * 予約可能時間帯取得.
     * @param principal ログイン中の顧客情報
     * @return 予約可能時間帯リスト
     */
    @GetMapping
    public List<ReservationTimeSlotDto> getReservations(
            @AuthenticationPrincipal CustomerPrincipal principal) {
    	if (principal == null) {
    		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    	}
    	
    	Long salonId = principal.getSalonId();
    	// 現在日-1日から、60日後で取得する
    	LocalDate from = LocalDate.now().minusDays(1);
    	LocalDate to = LocalDate.now().plusDays(60);
    	
    	List<ReservationTimeSlotDto> slList = customerService.findReservationTimeSlotsBySalonIdAndDateRange(salonId, from, to);
    	for (ReservationTimeSlotDto reservationTimeSlotDto : slList) {
			System.out.println(reservationTimeSlotDto);
		}
    	
        return slList;
    }
    
    /**
     * 予約作成.
     * @param request 予約リクエスト
     * @param principal ログイン中の顧客情報
     * @return レスポンス
     */
    @PostMapping
    public ResponseEntity<?> createReservation(
            @Valid @RequestBody CreateReservationRequest request,
            @AuthenticationPrincipal CustomerPrincipal principal) {
    	if (principal == null) {
    		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    	}
    	
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
            @AuthenticationPrincipal CustomerPrincipal principal) {
    	System.out.println("history:" + principal);

        if (principal == null) {
            // セキュリティ設定上ここには来ない想定だが、念のため
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        
        List<VisitHistoryDto> visitHistoryList = customerService.getVisitHistory(principal.getCustomerId(), principal.getSalonId());
        for (VisitHistoryDto visitHistoryDto : visitHistoryList) {
        	System.out.println("visitHistoryDto=" + visitHistoryDto);
		}
        
        return visitHistoryList;
    }
    
    // TODO 余計な情報は送らないようにする
    /**
     * 施術者一覧（予約画面用）.
     */
    @GetMapping("/practitioners")
    public List<StaffResponse> getPractitioners(
            @AuthenticationPrincipal CustomerPrincipal principal) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Long salonId = principal.getSalonId();
        return staffService.findPractitionersResponseBySalonId(salonId);
    }
    
    /**
     * 予約キャンセル.
     * <p>
     * 顧客が自分の予約をキャンセルします。
     * ステータスを CANCELED に変更します。
     * 
     * @param reservationId 予約ID
     * @param principal ログイン中の顧客情報
     * @return キャンセル成功メッセージ
     */
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Map<String, Object>> cancelReservation(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomerPrincipal principal) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        reservationService.cancelReservation(reservationId, principal.getCustomerId(), principal.getSalonId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "キャンセルしました"
        ));
    }
    
    // TODO [後回し]予約更新
}
