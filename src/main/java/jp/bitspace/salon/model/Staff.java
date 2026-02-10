package jp.bitspace.salon.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * スタッフエンティティ.
 * <p>
 * ユーザーとサロンの関連を表し、サロンにおけるスタッフの役割や権限を管理します。
 * 1つのUserは複数のStaffレコードを持つことができ（複数店舗勤務）、
 * user_id と salon_id の組み合わせは一意制約により重複できません。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "staffs", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "salon_id"}))
public class Staff {
    /** スタッフID（主キー）. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属ユーザー（外部キー）. */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 所属サロン（外部キー）. */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "salon_id", nullable = false)
    private Salon salon;

    /** 提供可能メニュー一覧. */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "staff_available_menus",
        joinColumns = @JoinColumn(name = "staff_id"),
        inverseJoinColumns = @JoinColumn(name = "menu_id")
    )
    @Builder.Default
    private List<Menu> availableMenus = new ArrayList<>();

    /** 役割（ADMIN=管理者、STAFF=スタッフ）. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.STAFF;

    /** 施術者フラグ（true=施術可能、false=受付等）. */
    @JsonProperty("isPractitioner")
    @Builder.Default
    @Column(name = "is_practitioner")
    private Boolean isPractitioner = true;

    /** アクティブフラグ（false=退職・無効化）. */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /** 作成日時. */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 更新日時. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * エンティティ作成時の初期化処理.
     * <p>
     * 作成日時と更新日時を現在時刻で初期化します。
     * </p>
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * エンティティ更新時の処理.
     * <p>
     * 更新日時を現在時刻で更新します。
     * </p>
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
