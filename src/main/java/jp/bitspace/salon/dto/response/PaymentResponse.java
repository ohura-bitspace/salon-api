package jp.bitspace.salon.dto.response;

import java.time.LocalDateTime;

import jp.bitspace.salon.model.Payment;
import jp.bitspace.salon.model.PaymentMethod;
import jp.bitspace.salon.model.PaymentSource;
import lombok.Builder;
import lombok.Data;

/**
 * 決済レスポンス.
 */
@Data
@Builder
public class PaymentResponse {
    
    /** 決済ID. */
    private Long id;
    
    /** 所属サロンID. */
    private Long salonId;
    
    /** 関連する予約ID. */
    private Long reservationId;
    
    /** 顧客ID. */
    private Long customerId;
    
    /** 決済金額（単位: 円）. */
    private Integer amount;
    
    /** 決済方法. */
    private PaymentMethod paymentMethod;
    
    /** 決済データの登録元. */
    private PaymentSource paymentSource;
    
    /** 外部決済ID. */
    private String externalTransactionId;
    
    /** 決済日時. */
    private LocalDateTime paymentAt;
    
    /** メモ. */
    private String memo;
    
    /** 作成日時. */
    private LocalDateTime createdAt;
    
    /** 更新日時. */
    private LocalDateTime updatedAt;
    
    /**
     * PaymentエンティティからPaymentResponseへ変換.
     */
    public static PaymentResponse fromEntity(Payment payment) {
        return PaymentResponse.builder()
            .id(payment.getId())
            .salonId(payment.getSalonId())
            .reservationId(payment.getReservationId())
            .customerId(payment.getCustomerId())
            .amount(payment.getAmount())
            .paymentMethod(payment.getPaymentMethod())
            .paymentSource(payment.getPaymentSource())
            .externalTransactionId(payment.getExternalTransactionId())
            .paymentAt(payment.getPaymentAt())
            .memo(payment.getMemo())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .build();
    }
}
