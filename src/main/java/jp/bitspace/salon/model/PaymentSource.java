package jp.bitspace.salon.model;

/**
 * 決済データの登録元.
 * <p>
 * 外部決済連携（例: Square）由来か、店舗側の手入力（MANUAL）かを識別します。
 */
public enum PaymentSource {
    /** 外部決済サービス（Square等）からの同期データ. */
    SQUARE,
    /** 管理画面などからの手入力データ. */
    MANUAL
}
