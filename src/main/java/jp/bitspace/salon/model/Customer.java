package jp.bitspace.salon.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "salon_id", nullable = false)
    private Long salonId;

    @Column(name = "line_user_id")
    private String lineUserId;

    @Column(name = "line_display_name")
    private String lineDisplayName;

    @Column(name = "line_picture_url", columnDefinition = "TEXT")
    private String linePictureUrl;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name_kana", length = 50)
    private String lastNameKana;

    @Column(name = "first_name_kana", length = 50)
    private String firstNameKana;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(length = 255)
    private String email;

    @Column
    private java.time.LocalDate birthday;

    @Column(name = "admin_memo", columnDefinition = "TEXT")
    private String adminMemo;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

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
