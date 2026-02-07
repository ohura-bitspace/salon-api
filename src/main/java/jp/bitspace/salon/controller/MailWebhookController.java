package jp.bitspace.salon.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * <p>
     * Mailgun から送信される multipart/form-data のパラメータ（body-plain, Message-Id 等）は、
     * Spring の DataBinder によって自動的に MailgunWebhookRequest の camelCase フィールドにマッピングされる。
     * </p>
     *
     * @param request Mailgun Webhook リクエスト（自動バインド）
     * @return 処理結果
     */
    @PostMapping(value = "/hotpepper", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MailWebhookResponse> handleHotpepperMail(
            @ModelAttribute MailgunWebhookRequest request
    ) {
        log.info("ホットペッパー Webhook 受信: subject={}, sender={}", request.getSubject(), request.getSender());

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
