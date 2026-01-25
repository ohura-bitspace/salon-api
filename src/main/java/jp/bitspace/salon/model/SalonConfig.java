package jp.bitspace.salon.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * サロン設定エンティティ.
 * <p>
 * 営業時間、定休日、予約枠の単位など、店舗ごとの運用設定を管理します。
 */
@Data
@Entity
@Table(name = "salon_configs")
public class SalonConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "salon_id", nullable = false)
    private Long salonId;

    /** 開店時刻 */
    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime = LocalTime.of(9, 0);

    /** 閉店時刻 */
    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime = LocalTime.of(21, 0);

    /**
     * 定休日フラグ（7桁、月曜始まり）.
     * <p>
     * 0=営業、1=休み<br>
     * 例: "0000011" → 土日休み
     */
    @Column(name = "regular_holidays", nullable = false, length = 7)
    private String regularHolidays = "0000000";

    /**
     * 予約枠の単位（分）.
     * <p>
     * 例: 30 → 30分単位、60 → 60分単位
     */
    @Column(name = "slot_interval", nullable = false)
    private Integer slotInterval = 30;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
