package jp.bitspace.salon.dto.response;

/**
 * メール Webhook 処理結果レスポンス.
 */
public record MailWebhookResponse(
    /** 処理成功フラグ */
    boolean success,

    /** メッセージ */
    String message,

    /** 作成された予約 ID（予約が作成された場合） */
    Long reservationId
) {
    public static MailWebhookResponse ok(String message, Long reservationId) {
        return new MailWebhookResponse(true, message, reservationId);
    }

    public static MailWebhookResponse ok(String message) {
        return new MailWebhookResponse(true, message, null);
    }

    public static MailWebhookResponse error(String message) {
        return new MailWebhookResponse(false, message, null);
    }
}
