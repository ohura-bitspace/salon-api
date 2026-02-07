package jp.bitspace.salon.service;

import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Mailgun Webhook 署名検証サービス.
 * <p>
 * Mailgun から送信された Webhook の署名を検証し、
 * リクエストが正規の Mailgun サーバーから送られたものであることを確認する。
 * </p>
 *
 * @see <a href="https://documentation.mailgun.com/docs/mailgun/api-reference/openapi-final/tag/Webhooks/#tag/Webhooks/Securing-Webhooks">Securing Webhooks</a>
 */
@Service
public class MailgunSignatureVerifier {

    private static final Logger log = LoggerFactory.getLogger(MailgunSignatureVerifier.class);

    @Value("${mailgun.api-key:}")
    private String apiKey;

    /**
     * Mailgun Webhook の署名を検証する.
     *
     * @param timestamp Unix エポック秒のタイムスタンプ
     * @param token     ランダムトークン
     * @param signature HMAC-SHA256 署名
     * @return 署名が正しい場合 true
     */
    public boolean verify(String timestamp, String token, String signature) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Mailgun API キーが設定されていません。署名検証をスキップします。");
            return true; // 開発環境の場合はスキップ
        }

        if (timestamp == null || token == null || signature == null) {
            log.warn("署名検証パラメータが不足しています: timestamp={}, token={}, signature={}",
                    timestamp, token, signature);
            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(apiKey.getBytes(), "HmacSHA256"));
            String data = timestamp + token;
            byte[] digest = mac.doFinal(data.getBytes());
            String computedSignature = HexFormat.of().formatHex(digest);

            boolean valid = computedSignature.equals(signature);
            if (!valid) {
                log.warn("Mailgun 署名検証失敗");
            }
            return valid;
        } catch (Exception e) {
            log.error("Mailgun 署名検証中にエラーが発生しました", e);
            return false;
        }
    }
}
