package jp.bitspace.salon.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.bitspace.salon.service.LineWebhookService;

/**
 * LINE Messaging API Webhook コントローラ.
 * <p>
 * LINE Platform からのイベント（メッセージ受信等）を受け付けます。
 * Webhook URL: POST /api/webhooks/line/{salonId}
 */
@RestController
@RequestMapping("/api/webhooks/line")
public class LineWebhookController {

    private static final Logger log = LoggerFactory.getLogger(LineWebhookController.class);

    private final LineWebhookService lineWebhookService;

    public LineWebhookController(LineWebhookService lineWebhookService) {
        this.lineWebhookService = lineWebhookService;
    }

    /**
     * LINE Webhook エンドポイント.
     * <p>
     * LINE Platform は署名付きで JSON を POST します。
     * 署名検証後、イベントを処理して messages テーブルに保存します。
     *
     * @param salonId サロンID（URLパスで識別）
     * @param signature X-Line-Signature ヘッダ
     * @param body リクエストボディ（JSON文字列）
     * @return 200 OK
     */
    @PostMapping("/{salonId}")
    public ResponseEntity<String> handleWebhook(
            @PathVariable Long salonId,
            @RequestHeader("X-Line-Signature") String signature,
            @RequestBody String body) {

        log.info("LINE Webhook received: salonId={}", salonId);

        try {
            lineWebhookService.handleWebhook(salonId, signature, body);
            return ResponseEntity.ok("OK");
        } catch (SecurityException e) {
            log.warn("LINE Webhook signature verification failed: salonId={}", salonId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
        } catch (Exception e) {
            log.error("LINE Webhook processing error: salonId={}", salonId, e);
            // LINE にリトライさせないため 200 を返す
            return ResponseEntity.ok("OK");
        }
    }
}
