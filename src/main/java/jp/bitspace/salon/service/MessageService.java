package jp.bitspace.salon.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.bitspace.salon.dto.request.SendMessageRequest;
import jp.bitspace.salon.dto.response.MessageResponse;
import jp.bitspace.salon.model.Message;
import jp.bitspace.salon.model.MessageType;
import jp.bitspace.salon.model.SenderType;
import jp.bitspace.salon.repository.MessageRepository;

/**
 * メッセージサービス.
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * 顧客とのメッセージ履歴を取得.
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesBySalonAndCustomer(Long salonId, Long customerId) {
        return messageRepository.findBySalonIdAndCustomerIdOrderByCreatedAtAsc(salonId, customerId)
            .stream()
            .map(MessageResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * 管理者からメッセージを送信.
     */
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        Message message = new Message();
        message.setSalonId(request.getSalonId());
        message.setCustomerId(request.getCustomerId());
        message.setSenderType(SenderType.ADMIN);
        message.setMessageType(MessageType.TEXT);
        message.setText(request.getText());
        message.setIsRead(true); // 管理者が送信したメッセージは既読扱い

        Message saved = messageRepository.save(message);
        return MessageResponse.fromEntity(saved);
    }

    /**
     * 顧客のメッセージを既読にする.
     */
    @Transactional
    public void markAsRead(Long salonId, Long customerId) {
        List<Message> unreadMessages = messageRepository
            .findBySalonIdAndCustomerIdAndIsReadFalse(salonId, customerId);

        LocalDateTime now = LocalDateTime.now();
        for (Message message : unreadMessages) {
            message.setIsRead(true);
            message.setReadAt(now);
        }
        messageRepository.saveAll(unreadMessages);
    }

    /**
     * サロンの未読メッセージ数を取得.
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long salonId) {
        return messageRepository.countBySalonIdAndIsReadFalse(salonId);
    }
}
