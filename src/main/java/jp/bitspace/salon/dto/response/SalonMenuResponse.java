package jp.bitspace.salon.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * サロンメニューレスポンス.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalonMenuResponse {
	/** クーポンメニュー. */
    private List<MenuDto> coupons;
    /** 通常メニュー. */
    private List<CategoryDto> categories;
}
