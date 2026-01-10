package jp.bitspace.salon.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jp.bitspace.salon.model.PaymentMethod;
import lombok.Data;

/**
 * 決済登録リクエスト.
 * <p>
 * 手動で登録された決済（MANUAL）の場合、paymentSourceはMANUALで自動設定されます。
 */
@Data
public class CreatePaymentRequest {
    
    /** 所属サロンID. */
    @NotNull(message = "salonIdは必須です")
    private Long salonId;
    
    /** 関連する予約ID（予約外売上の場合はNULL）. */
    private Long reservationId;
    
    /** 顧客ID（顧客が特定できない場合はNULL）. */
    private Long customerId;
    
    /** 決済金額（単位: 円）. */
    @NotNull(message = "amountは必須です")
    @Positive(message = "amountは正の値である必要があります")
    private Integer amount;
    
    /** 決済方法. */
    @NotNull(message = "paymentMethodは必須です")
    private PaymentMethod paymentMethod;
    
    /** 決済日時（入金日時）. */
    @NotNull(message = "paymentAtは必須です")
    private LocalDateTime paymentAt;
    
    /** 会計に関するメモ. */
    private String memo;
}
