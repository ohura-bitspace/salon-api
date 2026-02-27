package jp.bitspace.salon.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jp.bitspace.salon.model.PaymentMethod;
import lombok.Data;

/**
 * 決済登録リクエスト.
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

    /** 元金額（割引前、単位: 円）. */
    @NotNull(message = "originalAmountは必須です")
    @PositiveOrZero(message = "originalAmountは0以上である必要があります")
    private Integer originalAmount;

    /** 通常割引額（単位: 円）. */
    @PositiveOrZero(message = "discountAmountは0以上である必要があります")
    private Integer discountAmount = 0;

    /** ポイント割引額（単位: 円）. */
    @PositiveOrZero(message = "pointDiscountAmountは0以上である必要があります")
    private Integer pointDiscountAmount = 0;

    /** 実際の決済金額（単位: 円）. */
    @NotNull(message = "amountは必須です")
    @PositiveOrZero(message = "amountは0以上である必要があります")
    private Integer amount;

    /** 決済方法. */
    @NotNull(message = "paymentMethodは必須です")
    private PaymentMethod paymentMethod;

    /** お預かり金額（現金払い時のみ）. */
    @PositiveOrZero(message = "receivedAmountは0以上である必要があります")
    private Integer receivedAmount;

    /** 会計に関するメモ. */
    private String memo;
}
