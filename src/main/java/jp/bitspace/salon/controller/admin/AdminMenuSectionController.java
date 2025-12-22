package jp.bitspace.salon.controller.admin;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jp.bitspace.salon.dto.request.MenuSectionRequest;
import jp.bitspace.salon.model.MenuCategory;
import jp.bitspace.salon.model.MenuSection;
import jp.bitspace.salon.repository.MenuCategoryRepository;
import jp.bitspace.salon.repository.MenuSectionRepository;
import jp.bitspace.salon.security.AdminRequestAuthUtil;

@RestController
@RequestMapping("/api/admin/menu-sections")
public class AdminMenuSectionController {
    private final MenuSectionRepository menuSectionRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final AdminRequestAuthUtil adminRequestAuthUtil;

    public AdminMenuSectionController(MenuSectionRepository menuSectionRepository,
                                      MenuCategoryRepository menuCategoryRepository,
                                      AdminRequestAuthUtil adminRequestAuthUtil) {
        this.menuSectionRepository = menuSectionRepository;
        this.menuCategoryRepository = menuCategoryRepository;
        this.adminRequestAuthUtil = adminRequestAuthUtil;
    }

    @PostMapping
    public ResponseEntity<?> createSection(HttpServletRequest request, @Valid @RequestBody MenuSectionRequest body) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(request, body.getSalonId());

        MenuCategory category = menuCategoryRepository.findById(body.getCategoryId())
            .orElse(null);
        if (category == null || !category.getSalonId().equals(body.getSalonId())) {
            return ResponseEntity.status(400).body(Map.of("error", "Invalid category for salon"));
        }

        MenuSection section = MenuSection.builder()
            .menuCategory(category)
            .name(body.getName())
            .displayOrder(body.getDisplayOrder() != null ? body.getDisplayOrder() : 0)
            .build();

        return ResponseEntity.ok(menuSectionRepository.save(section));
    }

	@PutMapping("/{id}")
	public ResponseEntity<?> updateSection(HttpServletRequest request, @PathVariable Long id,
			@Valid @RequestBody MenuSectionRequest body) {
		return menuSectionRepository.findById(id)
				.<ResponseEntity<?>> map(section -> {
					adminRequestAuthUtil.requireStaffAndSalonMatch(request, section.getMenuCategory().getSalonId());

					// category変更を許可する場合のみ処理
					if (body.getCategoryId() != null
							&& !body.getCategoryId().equals(section.getMenuCategory().getId())) {
						MenuCategory newCategory = menuCategoryRepository.findById(body.getCategoryId()).orElse(null);
						if (newCategory == null
								|| !newCategory.getSalonId().equals(section.getMenuCategory().getSalonId())) {
							return ResponseEntity.status(400).body(Map.of("error", "Invalid category for salon"));
						}
						section.setMenuCategory(newCategory);
					}

					section.setName(body.getName());
					section.setDisplayOrder(body.getDisplayOrder() != null ? body.getDisplayOrder() : 0);
					return ResponseEntity.ok(menuSectionRepository.save(section));
				})
				.orElse(ResponseEntity.status(404).body(Map.of("error", "Section not found")));
	}

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSection(HttpServletRequest request, @PathVariable Long id) {
        return menuSectionRepository.findById(id)
            .<ResponseEntity<?>>map(section -> {
                adminRequestAuthUtil.requireStaffAndSalonMatch(request, section.getMenuCategory().getSalonId());
                menuSectionRepository.delete(section);
                return ResponseEntity.ok(Map.of("deleted", true));
            })
            .orElse(ResponseEntity.status(404).body(Map.of("error", "Section not found")));
    }
}
