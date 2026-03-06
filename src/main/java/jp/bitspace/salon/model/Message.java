package jp.bitspace.salon.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * LINEメッセージ履歴エンティティ.
 */
@Data
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "salon_id", nullable = false)
    private Long salonId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** 送信者種別. */
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private SenderType senderType;

    /** メッセージ種別. */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType = MessageType.TEXT;

    /** メッセージ本文（IMAGE/STAMPの場合はNULL可）. */
    @Column(columnDefinition = "TEXT")
    private String text;

    /** LINE側のメッセージID. */
    @Column(name = "line_message_id", length = 255)
    private String lineMessageId;

    /** 管理者側の既読フラグ. */
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /** 既読日時. */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (messageType == null) {
            messageType = MessageType.TEXT;
        }
        if (isRead == null) {
            isRead = false;
        }
    }
}
