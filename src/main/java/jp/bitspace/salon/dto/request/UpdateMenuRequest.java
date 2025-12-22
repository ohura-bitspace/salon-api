package jp.bitspace.salon.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMenuRequest {
    private String title;
    private Long menuSectionId;
    private String description;
    private String imageUrl;
    private Integer originalPrice;
    private Integer discountedPrice;
    private Integer durationMinutes;
    private String itemType;
    private Long menuCategoryId;
    private String tag;
    private Integer displayOrder;
    private Boolean isActive;
}
