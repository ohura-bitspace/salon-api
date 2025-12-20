package jp.bitspace.salon.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jp.bitspace.salon.dto.request.CreateMenuRequest;
import jp.bitspace.salon.dto.request.UpdateMenuRequest;
import jp.bitspace.salon.model.Menu;
import jp.bitspace.salon.security.AdminRequestAuthUtil;
import jp.bitspace.salon.service.MenuService;

@RestController
@RequestMapping("/api/admin/menus")
public class AdminMenuController {
    private final MenuService menuService;
    private final AdminRequestAuthUtil adminRequestAuthUtil;

    public AdminMenuController(MenuService menuService, AdminRequestAuthUtil adminRequestAuthUtil) {
        this.menuService = menuService;
        this.adminRequestAuthUtil = adminRequestAuthUtil;
    }
    
    /**
     * メニュー取得.
     * @param httpServletRequest request
     * @param salonId サロンID
     * @return List<Menu>
     */
    @GetMapping
    public List<Menu> getMenus(HttpServletRequest httpServletRequest, @RequestParam(name = "salonId") Long salonId) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        return menuService.findBySalonId(salonId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMenu(@PathVariable Long id) {
        return menuService.findById(id)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Menu not found")));
    }

    @PostMapping
    public ResponseEntity<?> createMenu(
        HttpServletRequest httpServletRequest,
        @RequestParam(name = "salonId") Long salonId,
        @Valid @RequestBody CreateMenuRequest request
    ) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        Menu created = menuService.createMenu(salonId, request);
        return ResponseEntity.ok(created);
    }

	@PutMapping("/{id}")
	public ResponseEntity<?> updateMenu(
			HttpServletRequest httpServletRequest,
			@PathVariable Long id,
			@Valid @RequestBody UpdateMenuRequest request) {
		// salonIdはMenu側に紐づくため、ここでは簡易に更新実行（必要なら取得して認可に使う）
		try {
			Menu updated = menuService.updateMenu(id, request);
			return ResponseEntity.ok(updated);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
		}
	}
    
    /**
     * メニュー削除
     * @param id id
     * @return ResponseEntity
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMenu(@PathVariable Long id) {
        menuService.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
