package jp.bitspace.salon.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.bitspace.salon.model.Customer;
import jp.bitspace.salon.model.Message;
import jp.bitspace.salon.model.MessageType;
import jp.bitspace.salon.model.SalonConfig;
import jp.bitspace.salon.model.SenderType;
import jp.bitspace.salon.repository.CustomerRepository;
import jp.bitspace.salon.repository.MessageRepository;
import jp.bitspace.salon.repository.SalonConfigRepository;

/**
 * LINE Webhook イベント処理サービス.
 * <p>
 * 署名検証 → イベント解析 → messages テーブルへ保存を行います。
 */
@Service
public class LineWebhookService {

    private static final Logger log = LoggerFactory.getLogger(LineWebhookService.class);

    private final SalonConfigRepository salonConfigRepository;
    private final CustomerRepository customerRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LineWebhookService(SalonConfigRepository salonConfigRepository,
                              CustomerRepository customerRepository,
                              MessageRepository messageRepository) {
        this.salonConfigRepository = salonConfigRepository;
        this.customerRepository = customerRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * Webhook を処理.
     *
     * @param salonId サロンID
     * @param signature X-Line-Signature ヘッダ値
     * @param body リクエストボディ（JSON文字列）
     */
    public void handleWebhook(Long salonId, String signature, String body) {
        SalonConfig config = salonConfigRepository.findBySalonId(salonId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "サロン設定が見つかりません"));

        String channelSecret = config.getLineChannelSecret();
        if (channelSecret == null || channelSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "LINE Messaging APIが未設定です");
        }

        // 署名検証
        if (!verifySignature(channelSecret, body, signature)) {
            throw new SecurityException("Invalid LINE signature");
        }

        // イベント解析・保存
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode events = root.path("events");

            for (JsonNode event : events) {
                processEvent(salonId, event);
            }
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("LINE Webhook イベント解析エラー: salonId={}", salonId, e);
        }
    }

    /**
     * 個別イベントを処理.
     */
    private void processEvent(Long salonId, JsonNode event) {
        String type = event.path("type").asText();

        if (!"message".equals(type)) {
            log.debug("LINE Webhook: メッセージ以外のイベントをスキップ: type={}", type);
            return;
        }

        String lineUserId = event.path("source").path("userId").asText(null);
        if (lineUserId == null) {
            log.warn("LINE Webhook: userId が取得できません");
            return;
        }

        // 顧客を特定（未登録の場合はスキップ）
        Customer customer = customerRepository.findByLineUserIdAndSalonId(lineUserId, salonId)
            .orElse(null);
        if (customer == null) {
            log.info("LINE Webhook: 未登録のLINEユーザー: lineUserId={}, salonId={}", lineUserId, salonId);
            return;
        }

        JsonNode messageNode = event.path("message");
        String messageType = messageNode.path("type").asText("text");
        String lineMessageId = messageNode.path("id").asText(null);

        Message message = new Message();
        message.setSalonId(salonId);
        message.setCustomerId(customer.getId());
        message.setSenderType(SenderType.LINE_USER);
        message.setLineMessageId(lineMessageId);
        message.setIsRead(false);

        switch (messageType) {
            case "text":
                message.setMessageType(MessageType.TEXT);
                message.setText(messageNode.path("text").asText(""));
                break;
            case "image":
                message.setMessageType(MessageType.IMAGE);
                break;
            case "sticker":
                message.setMessageType(MessageType.STAMP);
                break;
            default:
                message.setMessageType(MessageType.OTHER);
                break;
        }

        messageRepository.save(message);
        log.info("LINE メッセージ保存: salonId={}, customerId={}, type={}", salonId, customer.getId(), messageType);
    }

    /**
     * X-Line-Signature を HMAC-SHA256 で検証.
     */
    private boolean verifySignature(String channelSecret, String body, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(channelSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            byte[] digest = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            String computed = Base64.getEncoder().encodeToString(digest);
            return computed.equals(signature);
        } catch (Exception e) {
            log.error("署名検証エラー", e);
            return false;
        }
    }
}
