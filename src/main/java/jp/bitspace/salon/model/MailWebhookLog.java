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
 * メール Webhook 受信ログエンティティ.
 */
@Data
@Entity
@Table(name = "mail_webhook_logs")
public class MailWebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "salon_id", nullable = false)
    private Long salonId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MailWebhookStatus status = MailWebhookStatus.SUCCESS;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(length = 500)
    private String subject;

    @Column(length = 255)
    private String sender;

    @Column(length = 255)
    private String recipient;

    @Column(name = "body_plain", columnDefinition = "MEDIUMTEXT")
    private String bodyPlain;

    @Column(name = "mailgun_message_id", length = 255)
    private String mailgunMessageId;

    @Column(name = "mailgun_timestamp", length = 20)
    private String mailgunTimestamp;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
