package jp.bitspace.salon.controller.customer;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.bitspace.salon.dto.response.SalonMenuResponse;
import jp.bitspace.salon.security.CustomerPrincipal;
import jp.bitspace.salon.service.MenuService;

@RestController
@RequestMapping({"/api/customer/menus"})
public class CustomerMenuController {
    private final MenuService menuService;

    public CustomerMenuController(MenuService menuService) {
        this.menuService = menuService;
    }
    
    /**
     * 顧客表示用: クーポン + カテゴリ（中でセクション名でフロントがグルーピング可能）
     * <p>
     * JWT認証済みの顧客が対象。トークンからsalonIdを自動抽出します。
     */
    @GetMapping
    public SalonMenuResponse getMenus(@AuthenticationPrincipal CustomerPrincipal principal) {

        Long salonId = principal.getSalonId();
        return menuService.getSalonMenusGroupedForCustomer(salonId);
    }
}
