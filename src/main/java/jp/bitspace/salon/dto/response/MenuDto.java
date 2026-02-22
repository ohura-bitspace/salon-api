package jp.bitspace.salon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDto {
    private Long id;
    /** カテゴリID. */
    private Long menuCategoryId;
    private Long menuSectionId;
    private String title;
    private String description;
    
    private String imageUrl;
    /** オリジナル金額. */
    private Integer originalPrice;
    /** ディスカウント金額. */
    private Integer discountedPrice;
    /** 施術時間. */
    private Integer durationMinutes;
    private String itemType;
    private String tag;
    private Integer displayOrder;
    private Boolean isActive;
}
