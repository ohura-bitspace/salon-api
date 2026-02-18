package jp.bitspace.salon.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jp.bitspace.salon.dto.request.CreatePaymentRequest;
import jp.bitspace.salon.dto.response.PaymentResponse;
import jp.bitspace.salon.model.Payment;
import jp.bitspace.salon.model.PaymentSource;
import jp.bitspace.salon.model.Reservation;
import jp.bitspace.salon.model.ReservationStatus;
import jp.bitspace.salon.repository.PaymentRepository;
import jp.bitspace.salon.repository.ReservationRepository;

/**
 * 決済サービス.
 * <p>
 * 手動登録（MANUAL）の決済を処理します。
 */
@Service
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    public PaymentService(PaymentRepository paymentRepository, ReservationRepository reservationRepository) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
    }
    
    /**
     * 決済を作成.
     * <p>
     * 手動登録の場合、paymentSourceは自動的にMANUALに設定されます。
     * 
     * @param request 決済リクエスト
     * @return 作成された決済レスポンス
     */
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        // リクエスト値の検証
        validateCreatePaymentRequest(request);
        
        // Paymentエンティティを作成
        Payment payment = new Payment();
        payment.setSalonId(request.getSalonId());
        payment.setReservationId(request.getReservationId());
        payment.setCustomerId(request.getCustomerId());
        payment.setOriginalAmount(request.getOriginalAmount());
        payment.setDiscountAmount(request.getOriginalAmount() - request.getAmount());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        // 手動登録なのでMANUALで設定
        payment.setPaymentSource(PaymentSource.MANUAL);
        payment.setPaymentAt(LocalDateTime.now());
        payment.setMemo(request.getMemo());
        
        // 保存
        Payment savedPayment = paymentRepository.save(payment);

        // 予約IDが指定されている場合、予約ステータスを来店済みに変更
        if (request.getReservationId() != null) {
            Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
            reservation.setStatus(ReservationStatus.VISITED);
            reservationRepository.save(reservation);
        }

        return PaymentResponse.fromEntity(savedPayment);
    }
    
    /**
     * 決済IDで取得.
     * 
     * @param id 決済ID
     * @return 決済レスポンス
     */
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        return PaymentResponse.fromEntity(payment);
    }
    
    /**
     * サロンIDで決済一覧を取得.
     * 
     * @param salonId サロンID
     * @return 決済レスポンスリスト
     */
    public List<PaymentResponse> getPaymentsBySalonId(Long salonId) {
        List<Payment> payments = paymentRepository.findBySalonId(salonId);
        return payments.stream()
            .map(PaymentResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * サロンIDと期間で決済一覧を取得.
     * 
     * @param salonId サロンID
     * @param fromDate 開始日
     * @param toDate 終了日
     * @return 決済レスポンスリスト
     */
    public List<PaymentResponse> getPaymentsBySalonIdAndDateRange(Long salonId, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.atTime(LocalTime.MAX);
        
        List<Payment> payments = paymentRepository.findBySalonIdAndDateRange(salonId, startDateTime, endDateTime);
        return payments.stream()
            .map(PaymentResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * 予約IDで決済一覧を取得.
     * 
     * @param reservationId 予約ID
     * @return 決済レスポンスリスト
     */
    public List<PaymentResponse> getPaymentsByReservationId(Long reservationId) {
        List<Payment> payments = paymentRepository.findByReservationId(reservationId);
        return payments.stream()
            .map(PaymentResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * 顧客IDで決済一覧を取得.
     * 
     * @param customerId 顧客ID
     * @return 決済レスポンスリスト
     */
    public List<PaymentResponse> getPaymentsByCustomerId(Long customerId) {
        List<Payment> payments = paymentRepository.findByCustomerId(customerId);
        return payments.stream()
            .map(PaymentResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * 決済を削除.
     * 
     * @param id 決済ID
     */
    @Transactional
    public void deletePayment(Long id) {
        // 決済が存在するかチェック
        if (!paymentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        }
        paymentRepository.deleteById(id);
    }
    
    /**
     * 決済リクエストの検証.
     * 
     * @param request 決済リクエスト
     */
    private void validateCreatePaymentRequest(CreatePaymentRequest request) {
        // 基本的な検証（@NotNullなどのアノテーションで対応）
        
        // 元金額が正の値か確認
        if (request.getOriginalAmount() == null || request.getOriginalAmount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Original amount must be positive");
        }

        // 決済金額が正の値か確認
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }

        // 決済金額が元金額を超えないか確認
        if (request.getAmount() > request.getOriginalAmount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must not exceed original amount");
        }
    }
}
