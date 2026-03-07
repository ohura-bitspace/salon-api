package jp.bitspace.salon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jp.bitspace.salon.model.SalonConfig;
import jp.bitspace.salon.repository.SalonConfigRepository;

/**
 * LINE Messaging API クライアント.
 * <p>
 * サロンごとの {@code line_channel_access_token} を使用してメッセージを送信します。
 */
@Service
public class LineMessagingClient {

    private static final Logger log = LoggerFactory.getLogger(LineMessagingClient.class);
    private static final String PUSH_MESSAGE_URL = "https://api.line.me/v2/bot/message/push";

    private final SalonConfigRepository salonConfigRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public LineMessagingClient(SalonConfigRepository salonConfigRepository) {
        this.salonConfigRepository = salonConfigRepository;
    }

    /**
     * テキストメッセージをプッシュ送信.
     *
     * @param salonId サロンID（トークンの取得に使用）
     * @param lineUserId 送信先のLINEユーザーID
     * @param text 送信するテキスト
     */
    public void pushTextMessage(Long salonId, String lineUserId, String text) {
        String accessToken = getAccessToken(salonId);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("to", lineUserId);

        ArrayNode messages = body.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("type", "text");
        message.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                PUSH_MESSAGE_URL,
                new HttpEntity<>(body.toString(), headers),
                String.class
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("LINE Push Message failed: status={}, body={}", response.getStatusCode(), response.getBody());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "LINEメッセージの送信に失敗しました");
            }
        } catch (RestClientException e) {
            log.error("LINE Push Message request error", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "LINEメッセージの送信に失敗しました");
        }
    }

    /**
     * サロンのチャネルアクセストークンを取得.
     */
    private String getAccessToken(Long salonId) {
        SalonConfig config = salonConfigRepository.findBySalonId(salonId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "サロン設定が見つかりません"));

        String token = config.getLineChannelAccessToken();
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "LINE Messaging APIが未設定です");
        }
        return token;
    }
}
