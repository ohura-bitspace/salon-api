package jp.bitspace.salon.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ユーザーエンティティ.
 * <p>
 * システムを利用するすべてのユーザー（スタッフ）の認証情報を管理します。
 * 1つのUserは複数のStaffレコード（複数店舗への所属）を持つことができます。
 * </p>
 */
@Data
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = {"email"}))
public class User {
    /** ユーザーID（主キー）. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ユーザー名（最大100文字）. */
    @Column(nullable = false, length = 100)
    private String name;

    /** メールアドレス（ログインID、ユニーク）. */
    @Column(nullable = false, length = 255, unique = true)
    private String email;

    /** パスワードハッシュ（BCrypt等で暗号化）. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** アクティブフラグ（false=無効化されたユーザー）. */
    @Column(name = "is_active")
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
