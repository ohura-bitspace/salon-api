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

    // ── Mailgun のハイフン付きパラメータ名に対応 ────────
    // Spring の DataBinder は "body-plain" を "body_plain" として処理するため、
    // アンダースコア付きの setter を用意することで自動マッピングが可能になる。

    /** Mailgun の "body-plain" パラメータをマッピング */
    public void setBody_plain(String bodyPlain) {
        this.bodyPlain = bodyPlain;
    }

    /** Mailgun の "body-html" パラメータをマッピング */
    public void setBody_html(String bodyHtml) {
        this.bodyHtml = bodyHtml;
    }

    /** Mailgun の "stripped-text" パラメータをマッピング */
    public void setStripped_text(String strippedText) {
        this.strippedText = strippedText;
    }

    /** Mailgun の "stripped-html" パラメータをマッピング */
    public void setStripped_html(String strippedHtml) {
        this.strippedHtml = strippedHtml;
    }

    /** Mailgun の "stripped-signature" パラメータをマッピング */
    public void setStripped_signature(String strippedSignature) {
        this.strippedSignature = strippedSignature;
    }

    /** Mailgun の "Message-Id" パラメータをマッピング */
    public void setMessage_Id(String messageId) {
        this.messageId = messageId;
    }

    /** Mailgun の "content-type" パラメータをマッピング */
    public void setContent_type(String contentType) {
        this.contentType = contentType;
    }
}
