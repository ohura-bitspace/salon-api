package jp.bitspace.salon.dto.response;

import java.util.List;
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
    private String title;
    private String description;
    private String imageUrl;
    private Integer originalPrice;
    private Integer discountedPrice;
    private Integer durationMinutes;
    private String itemType;
    private String tag;
    private Integer displayOrder;
    private Boolean isActive;
}
