package jp.bitspace.salon.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jp.bitspace.salon.dto.request.SendMessageRequest;
import jp.bitspace.salon.dto.response.MessageResponse;
import jp.bitspace.salon.dto.response.MessageThreadResponse;
import jp.bitspace.salon.model.Customer;
import jp.bitspace.salon.model.Message;
import jp.bitspace.salon.model.MessageType;
import jp.bitspace.salon.model.SenderType;
import jp.bitspace.salon.repository.CustomerRepository;
import jp.bitspace.salon.repository.MessageRepository;

/**
 * メッセージサービス.
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final CustomerRepository customerRepository;
    private final LineMessagingClient lineMessagingClient;

    public MessageService(MessageRepository messageRepository,
                          CustomerRepository customerRepository,
                          LineMessagingClient lineMessagingClient) {
        this.messageRepository = messageRepository;
        this.customerRepository = customerRepository;
        this.lineMessagingClient = lineMessagingClient;
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
     * <p>
     * DB保存後、顧客の line_user_id を使って LINE Messaging API でプッシュ送信します。
     */
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        // 顧客の line_user_id を取得
        Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "顧客が見つかりません"));

        if (customer.getLineUserId() == null || customer.getLineUserId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "この顧客はLINE連携されていません");
        }

        // DB保存
        Message message = new Message();
        message.setSalonId(request.getSalonId());
        message.setCustomerId(request.getCustomerId());
        message.setSenderType(SenderType.ADMIN);
        message.setMessageType(MessageType.TEXT);
        message.setText(request.getText());
        message.setIsRead(true); // 管理者が送信したメッセージは既読扱い

        Message saved = messageRepository.save(message);

        // LINE Messaging API でプッシュ送信
        lineMessagingClient.pushTextMessage(
            request.getSalonId(),
            customer.getLineUserId(),
            request.getText()
        );

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
     * メッセージスレッド一覧を取得（顧客ごとの最新メッセージ・未読数付き）.
     */
    @Transactional(readOnly = true)
    public List<MessageThreadResponse> getThreads(Long salonId) {
        return messageRepository.findThreadsBySalonId(salonId)
            .stream()
            .map(row -> MessageThreadResponse.builder()
                .customerId(((Number) row[0]).longValue())
                .customerName((String) row[1])
                .lastMessage((String) row[2])
                .lastMessageAt(row[3] instanceof java.sql.Timestamp ts
                    ? ts.toLocalDateTime()
                    : (LocalDateTime) row[3])
                .unreadCount(((Number) row[4]).longValue())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * サロンの未読メッセージ数を取得.
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long salonId) {
        return messageRepository.countBySalonIdAndIsReadFalse(salonId);
    }
}
