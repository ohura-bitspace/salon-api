package jp.bitspace.salon.dto.request;

import lombok.Data;

/**
 * Mailgun Webhook リクエスト.
 * <p>
 * Mailgun は multipart/form-data でデータを POST するため、
 * フォームパラメータとしてバインドする。
 * </p>
 *
 * @see <a href="https://documentation.mailgun.com/docs/mailgun/api-reference/openapi-final/tag/Webhooks/">Mailgun Webhooks</a>
 */
@Data
public class MailgunWebhookRequest {

    // ── 送信者・受信者情報 ──────────────────────────
    /** 送信元メールアドレス */
    private String sender;

    /** 送信者 (envelope MAIL FROM) */
    private String from;

    /** 受信先メールアドレス */
    private String recipient;

    /** To ヘッダ */
    private String to;

    /** Subject ヘッダ */
    private String subject;

    // ── 本文 ──────────────────────────────────────
    /** プレーンテキスト本文 (stripped-text) */
    private String bodyPlain;

    /** HTML 本文 (stripped-html) */
    private String bodyHtml;

    /** HTML タグを除去した本文 */
    private String strippedText;

    /** 引用部分を除去した HTML */
    private String strippedHtml;

    /** 署名を除去した本文 */
    private String strippedSignature;

    // ── Mailgun 署名検証用 ─────────────────────────
    /** タイムスタンプ (Unix epoch seconds) */
    private String timestamp;

    /** トークン */
    private String token;

    /** HMAC-SHA256 署名 */
    private String signature;

    // ── メッセージ情報 ─────────────────────────────
    /** Mailgun 内部メッセージ ID */
    private String messageId;

    /** Date ヘッダ */
    private String date;

    /** Content-Type ヘッダ */
    private String contentType;
}
