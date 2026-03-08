package jp.bitspace.salon.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * メッセージスレッドレスポンス（顧客ごとの最新メッセージ・未読数）.
 */
@Data
@Builder
public class MessageThreadResponse {

    private Long customerId;
    private String customerName;
    private String linePictureUrl;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
}
