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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 決済（売上）エンティティ.
 * <p>
 * 予約（{@link jp.bitspace.salon.model.Reservation}）とは別に、実際のキャッシュフローを記録します。
 * 予約に紐づかない売上を扱えるよう {@code reservationId} はNULL許容です。
 */
@Data
@Entity
@Table(name = "payments")
public class Payment {
    /** 決済ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属サロンID. */
    @Column(name = "salon_id", nullable = false)
    private Long salonId;

    /** 関連する予約ID（予約外売上の場合はNULL）. */
    @Column(name = "reservation_id")
    private Long reservationId;

    /** 顧客ID（顧客が特定できない場合はNULL）. */
    @Column(name = "customer_id")
    private Long customerId;

    /** 決済金額（単位: 円）. */
    @Column(nullable = false)
    private Integer amount;

    /** 決済方法. */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    /** 決済データの登録元（未指定の場合はMANUAL）. */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_source")
    private PaymentSource paymentSource = PaymentSource.MANUAL;

    /** 外部決済ID（Square等）. */
    @Column(name = "external_transaction_id", length = 255)
    private String externalTransactionId;

    /** 決済日時（入金日時）. */
    @Column(name = "payment_at", nullable = false)
    private LocalDateTime paymentAt;

    /** 会計に関するメモ. */
    @Column(columnDefinition = "TEXT")
    private String memo;

    /** 作成日時. */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 更新日時. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 作成時のタイムスタンプ初期化.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (paymentSource == null) {
            paymentSource = PaymentSource.MANUAL;
        }
    }

    /**
     * 更新時のタイムスタンプ更新.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (paymentSource == null) {
            paymentSource = PaymentSource.MANUAL;
        }
    }
}
