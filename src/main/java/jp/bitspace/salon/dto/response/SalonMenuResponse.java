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
public class SalonMenuResponse {
    private List<MenuDto> coupons;
    private List<CategoryDto> categories;
}
