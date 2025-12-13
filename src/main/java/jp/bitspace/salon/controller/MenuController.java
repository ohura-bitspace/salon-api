package jp.bitspace.salon.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import jp.bitspace.salon.model.Menu;
import jp.bitspace.salon.service.MenuService;

@RestController
@RequestMapping("/api/menus")
public class MenuController {
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }
    
    /**
     * 全メニュー取得（サロンID指定）.
     * @param salonId サロンID
     * @return 全メニュー
     */
    @GetMapping
    public List<Menu> getMenus(@RequestParam(name = "salonId") Long salonId) {
        return menuService.findBySalonId(salonId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMenu(@PathVariable Long id) {
        Optional<Menu> menu = menuService.findById(id);
        if (menu.isPresent()) {
            return ResponseEntity.ok(menu.get());
        }
        return ResponseEntity.status(404).body(Map.of("error", "Menu not found"));
    }

    @PostMapping
    public ResponseEntity<Menu> createMenu(@RequestBody Menu menu) {
        return ResponseEntity.ok(menuService.save(menu));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMenu(@PathVariable Long id, @RequestBody Menu updated) {
        Optional<Menu> existing = menuService.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Menu not found"));
        }
        updated.setId(id);
        return ResponseEntity.ok(menuService.save(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMenu(@PathVariable Long id) {
        menuService.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
