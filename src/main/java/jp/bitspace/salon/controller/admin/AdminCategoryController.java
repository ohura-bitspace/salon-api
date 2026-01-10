package jp.bitspace.salon.controller.admin;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jp.bitspace.salon.dto.request.MenuCategoryRequest;
import jp.bitspace.salon.model.MenuCategory;
import jp.bitspace.salon.repository.MenuCategoryRepository;
import jp.bitspace.salon.security.AdminRequestAuthUtil;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {
    private final MenuCategoryRepository menuCategoryRepository;
    private final AdminRequestAuthUtil adminRequestAuthUtil;
    
    // [実装メモ]トークン認証方式は、本当はシンプルに
    // コントローラの引数として、@RequestHeaderで取得したかったが、一旦、HttpServletRequestで取得する方式としたので、
    // とりあえずこの方式で進む

    public AdminCategoryController(MenuCategoryRepository menuCategoryRepository, AdminRequestAuthUtil adminRequestAuthUtil) {
        this.menuCategoryRepository = menuCategoryRepository;
        this.adminRequestAuthUtil = adminRequestAuthUtil;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createCategory(HttpServletRequest request, @Valid @RequestBody MenuCategoryRequest body) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(request, body.getSalonId());

        MenuCategory category = MenuCategory.builder()
            .salonId(body.getSalonId())
            .name(body.getName())
            .displayOrder(body.getDisplayOrder() != null ? body.getDisplayOrder() : 0)
            .build();

        return ResponseEntity.ok(menuCategoryRepository.save(category));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateCategory(HttpServletRequest request, @PathVariable Long id,
            @Valid @RequestBody MenuCategoryRequest body) {
		return menuCategoryRepository.findById(id)
				.<ResponseEntity<?>> map(category -> {
					adminRequestAuthUtil.requireStaffAndSalonMatch(request, category.getSalonId());
					// salonId の変更は許可しない（安全のため）
					category.setName(body.getName());
					category.setDisplayOrder(body.getDisplayOrder() != null ? body.getDisplayOrder() : 0);
					return ResponseEntity.ok(menuCategoryRepository.save(category));
				})
				.orElse(ResponseEntity.status(404).body(Map.of("error", "Category not found")));
	}

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteCategory(HttpServletRequest request, @PathVariable Long id) {
        return menuCategoryRepository.findById(id)
            .<ResponseEntity<?>>map(category -> {
                adminRequestAuthUtil.requireStaffAndSalonMatch(request, category.getSalonId());
                menuCategoryRepository.delete(category);
                return ResponseEntity.ok(Map.of("deleted", true));
            })
            .orElse(ResponseEntity.status(404).body(Map.of("error", "Category not found")));
    }
}
