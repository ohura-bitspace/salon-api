package jp.bitspace.salon.dto.response;

import java.time.LocalDateTime;

import jp.bitspace.salon.model.Message;
import jp.bitspace.salon.model.MessageType;
import jp.bitspace.salon.model.SenderType;
import lombok.Builder;
import lombok.Data;

/**
 * メッセージレスポンス.
 */
@Data
@Builder
public class MessageResponse {

    private Long id;
    private Long salonId;
    private Long customerId;
    private SenderType senderType;
    private MessageType messageType;
    private String text;
    private String lineMessageId;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    /**
     * MessageエンティティからMessageResponseへ変換.
     */
    public static MessageResponse fromEntity(Message message) {
        return MessageResponse.builder()
            .id(message.getId())
            .salonId(message.getSalonId())
            .customerId(message.getCustomerId())
            .senderType(message.getSenderType())
            .messageType(message.getMessageType())
            .text(message.getText())
            .lineMessageId(message.getLineMessageId())
            .isRead(message.getIsRead())
            .readAt(message.getReadAt())
            .createdAt(message.getCreatedAt())
            .build();
    }
}
