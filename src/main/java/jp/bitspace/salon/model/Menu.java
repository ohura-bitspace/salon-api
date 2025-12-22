package jp.bitspace.salon.model;

import java.time.LocalDateTime;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode; // 追加
import lombok.NoArgsConstructor;
import lombok.ToString; // 追加

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "menus")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "salon_id", nullable = false)
    private Long salonId;

    // ★修正1: EAGERに変更（常にカテゴリ情報を取得）
    // ★修正2: @JsonIgnore を削除（必要なら残してもOKですが、EAGERなら情報を活用する方が自然です）
    // ★修正3: Lombokの無限ループ防止（EAGERにするなら必須）
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_category_id")
    private MenuCategory menuCategory;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_section_id")
    private MenuSection menuSection;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Builder.Default
    @Column(name = "original_price", nullable = false)
    private Integer originalPrice = 0;

    @Column(name = "discounted_price")
    private Integer discountedPrice;

    @Builder.Default
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 60;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private MenuItemType itemType = MenuItemType.MENU;

    @Column(length = 50)
    private String tag;

    @Builder.Default
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

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

    // 便利機能: フロントエンド用に ID と 名前 をフラットに返す
    
    @JsonProperty("menuCategoryId")
    public Long getMenuCategoryId() {
        return menuCategory != null ? menuCategory.getId() : null;
    }

    @JsonProperty("menuSectionId")
    public Long getMenuSectionId() {
        return menuSection != null ? menuSection.getId() : null;
    }

    @JsonProperty("categoryName")
    public String getCategoryName() {
        return menuCategory != null ? menuCategory.getName() : null;
    }
}