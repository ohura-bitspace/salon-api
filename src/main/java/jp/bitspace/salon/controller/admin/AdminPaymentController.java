package jp.bitspace.salon.controller.admin;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jp.bitspace.salon.dto.request.CreatePaymentRequest;
import jp.bitspace.salon.dto.response.PaymentResponse;
import jp.bitspace.salon.security.AdminRequestAuthUtil;
import jp.bitspace.salon.service.PaymentService;

/**
 * 管理側決済コントローラ.
 * <p>
 * 手動登録された決済（MANUAL）の管理を行います。
 */
@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {
    
    private final PaymentService paymentService;
    private final AdminRequestAuthUtil adminRequestAuthUtil;
    
    public AdminPaymentController(PaymentService paymentService, AdminRequestAuthUtil adminRequestAuthUtil) {
        this.paymentService = paymentService;
        this.adminRequestAuthUtil = adminRequestAuthUtil;
    }
    
    /**
     * 決済を作成（手動登録）.
     * <p>
     * paymentSourceはMANUALで自動設定されます。
     * 
     * @param httpServletRequest HTTPリクエスト
     * @param request 決済リクエスト
     * @return 作成された決済レスポンス
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody CreatePaymentRequest request) {
        
        // 管理API共通の認可チェック（STAFFトークン + salonId一致）
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, request.getSalonId());
        
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 決済IDで取得.
     * 
     * @param httpServletRequest HTTPリクエスト
     * @param id 決済ID
     * @param salonId サロンID
     * @return 決済レスポンス
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(
            HttpServletRequest httpServletRequest,
            @PathVariable Long id,
            @RequestParam(name = "salonId") Long salonId) {
        
        // 管理API共通の認可チェック
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        
        PaymentResponse response = paymentService.getPaymentById(id);
        // 決済のサロンIDが一致するか確認
        if (!response.getSalonId().equals(salonId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * サロンの決済一覧を取得.
     * <p>
     * クエリパラメータで期間指定できます。
     * 
     * @param httpServletRequest HTTPリクエスト
     * @param salonId サロンID
     * @param from 開始日（オプション）
     * @param to 終了日（オプション）
     * @return 決済レスポンスリスト
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPayments(
            HttpServletRequest httpServletRequest,
            @RequestParam(name = "salonId") Long salonId,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        // 管理API共通の認可チェック
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        
        List<PaymentResponse> responses;
        
        if (from != null && to != null) {
            // 期間指定
            responses = paymentService.getPaymentsBySalonIdAndDateRange(salonId, from, to);
        } else {
            // 全件取得
            responses = paymentService.getPaymentsBySalonId(salonId);
        }
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 決済を削除.
     * 
     * @param httpServletRequest HTTPリクエスト
     * @param id 決済ID
     * @param salonId サロンID
     * @return 削除完了レスポンス
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(
            HttpServletRequest httpServletRequest,
            @PathVariable Long id,
            @RequestParam(name = "salonId") Long salonId) {
        
        // 管理API共通の認可チェック
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        
        // 決済のサロンIDが一致するか確認
        PaymentResponse response = paymentService.getPaymentById(id);
        if (!response.getSalonId().equals(salonId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 予約IDで決済一覧を取得.
     * 
     * @param httpServletRequest HTTPリクエスト
     * @param reservationId 予約ID
     * @param salonId サロンID
     * @return 決済レスポンスリスト
     */
    @GetMapping("/by-reservation/{reservationId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByReservation(
            HttpServletRequest httpServletRequest,
            @PathVariable Long reservationId,
            @RequestParam(name = "salonId") Long salonId) {
        
        // 管理API共通の認可チェック
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        
        List<PaymentResponse> responses = paymentService.getPaymentsByReservationId(reservationId);
        return ResponseEntity.ok(responses);
    }
}
