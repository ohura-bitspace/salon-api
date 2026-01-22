package jp.bitspace.salon.controller.customer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jp.bitspace.salon.dto.response.SalonMenuResponse;
import jp.bitspace.salon.service.MenuService;

@RestController
@RequestMapping({"/api/customer/menus"})
public class CustomerMenuController {
    private final MenuService menuService;

    public CustomerMenuController(MenuService menuService) {
        this.menuService = menuService;
    }
    
    // TODO トークン
    /**
     * 顧客表示用: クーポン + カテゴリ（中でセクション名でフロントがグルーピング可能）
     */
    @GetMapping
    public SalonMenuResponse getMenus(@RequestParam(name = "salonId") Long salonId) {
    	System.out.println("menus, salonId="  + salonId);
        return menuService.getSalonMenusGroupedForCustomer(salonId);
    }
}
