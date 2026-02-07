package jp.bitspace.salon.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.bitspace.salon.dto.request.MailgunWebhookRequest;
import jp.bitspace.salon.dto.response.MailWebhookResponse;
import jp.bitspace.salon.service.MailWebhookService;
import lombok.RequiredArgsConstructor;

/**
 * メール Webhook コントローラ.
 * <p>
 * Mailgun の受信 Webhook を受け付け、メール本文を解析して予約に反映する。
 * Mailgun は multipart/form-data で POST するため {@code consumes} を指定する。
 * </p>
 */
@RestController
@RequestMapping("/api/webhooks/mail")
@RequiredArgsConstructor
public class MailWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MailWebhookController.class);

    private final MailWebhookService mailWebhookService;

    /**
     * ホットペッパー予約通知メールの受信エンドポイント.
     * <p>
     * Mailgun から転送されたメールを解析し、予約テーブルに登録する。
     * Mailgun はレスポンスの HTTP ステータスで成功/失敗を判断し、
     * 200 以外の場合はリトライを行う。
     * </p>
     *
     * @param sender         送信元メールアドレス
     * @param from           From ヘッダ
     * @param recipient      受信先メールアドレス
     * @param to             To ヘッダ
     * @param subject        件名
     * @param bodyPlain      プレーンテキスト本文
     * @param bodyHtml       HTML 本文
     * @param strippedText   引用除去済みテキスト
     * @param strippedHtml   引用除去済み HTML
     * @param timestamp      タイムスタンプ
     * @param token          トークン
     * @param signature      HMAC署名
     * @param messageId      メッセージ ID
     * @return 処理結果
     */
    @PostMapping(value = "/hotpepper", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MailWebhookResponse> handleHotpepperMail(
            @RequestParam(value = "sender", required = false) String sender,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "recipient", required = false) String recipient,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "body-plain", required = false) String bodyPlain,
            @RequestParam(value = "body-html", required = false) String bodyHtml,
            @RequestParam(value = "stripped-text", required = false) String strippedText,
            @RequestParam(value = "stripped-html", required = false) String strippedHtml,
            @RequestParam(value = "timestamp", required = false) String timestamp,
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "signature", required = false) String signature,
            @RequestParam(value = "Message-Id", required = false) String messageId
    ) {
        log.info("ホットペッパー Webhook 受信: subject={}, sender={}", subject, sender);

        // DTO に詰め替え
        MailgunWebhookRequest request = new MailgunWebhookRequest();
        request.setSender(sender);
        request.setFrom(from);
        request.setRecipient(recipient);
        request.setTo(to);
        request.setSubject(subject);
        request.setBodyPlain(bodyPlain);
        request.setBodyHtml(bodyHtml);
        request.setStrippedText(strippedText);
        request.setStrippedHtml(strippedHtml);
        request.setTimestamp(timestamp);
        request.setToken(token);
        request.setSignature(signature);
        request.setMessageId(messageId);

        try {
            MailWebhookResponse response = mailWebhookService.processHotpepperMail(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("ホットペッパー Webhook 処理エラー", e);
            // Mailgun にリトライさせないため 200 を返す（不正メールでリトライしても意味がない）
            return ResponseEntity.ok(MailWebhookResponse.error("処理中にエラーが発生しました: " + e.getMessage()));
        }
    }
}
