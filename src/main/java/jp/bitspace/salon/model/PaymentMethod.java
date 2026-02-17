package jp.bitspace.salon.model;

/**
 * 決済方法.
 * <p>
 * 会計時の支払い手段を表します。
 */
public enum PaymentMethod {
    /** 現金. */
    CASH,
    /** クレジットカード. */
    CREDIT_CARD,
    /** QR決済. */
    QR_PAY,
    /** その他. */
    OTHER
}
