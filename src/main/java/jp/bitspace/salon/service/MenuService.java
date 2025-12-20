package jp.bitspace.salon.service;

import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.stream.Collectors;
import jp.bitspace.salon.dto.request.CreateMenuRequest;
import jp.bitspace.salon.dto.request.UpdateMenuRequest;
import jp.bitspace.salon.dto.response.CategoryDto;
import jp.bitspace.salon.dto.response.MenuDto;
import jp.bitspace.salon.dto.response.SalonMenuResponse;
import jp.bitspace.salon.model.Menu;
import jp.bitspace.salon.model.MenuCategory;
import jp.bitspace.salon.model.MenuItemType;
import jp.bitspace.salon.repository.MenuCategoryRepository;
import jp.bitspace.salon.repository.MenuRepository;
import org.springframework.stereotype.Service;

@Service
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;

    public MenuService(MenuRepository menuRepository, MenuCategoryRepository menuCategoryRepository) {
        this.menuRepository = menuRepository;
        this.menuCategoryRepository = menuCategoryRepository;
    }

    public List<Menu> findAll() {
        return menuRepository.findAll();
    }

    public List<Menu> findBySalonId(Long salonId) {
        return menuRepository.findBySalonIdOrderByDisplayOrderAscIdAsc(salonId);
    }

    public Optional<Menu> findById(Long id) {
        return menuRepository.findById(id);
    }

    public Menu save(Menu menu) {
        return menuRepository.save(menu);
    }

    public void deleteById(Long id) {
        menuRepository.deleteById(id);
    }

    /**
     * 指定したサロンのメニュー情報を「クーポン」と「カテゴリごとのメニュー」に分けて取得
     */
    public SalonMenuResponse getSalonMenusGrouped(Long salonId) {
        return buildSalonMenusGrouped(salonId, false);
    }

    /**
     * 顧客表示用（非表示メニューを除外）
     */
    public SalonMenuResponse getSalonMenusGroupedForCustomer(Long salonId) {
        return buildSalonMenusGrouped(salonId, true);
    }

    private SalonMenuResponse buildSalonMenusGrouped(Long salonId, boolean customerOnly) {
        List<Menu> allMenus = menuRepository.findBySalonIdOrderByDisplayOrderAscIdAsc(salonId);

        List<MenuDto> coupons = allMenus.stream()
            .filter(menu -> menu.getItemType() == MenuItemType.COUPON)
            .filter(menu -> !customerOnly || Boolean.TRUE.equals(menu.getIsActive()))
            .sorted(Comparator.comparing(Menu::getDisplayOrder).thenComparing(Menu::getId))
            .map(this::convertToMenuDto)
            .collect(Collectors.toList());

        List<MenuCategory> categories = menuCategoryRepository.findBySalonIdOrderByDisplayOrderAscIdAsc(salonId);
        List<CategoryDto> categoryDtos = categories.stream()
            .map(category -> {
                List<MenuDto> menuDtos = category.getMenus().stream()
                    .filter(menu -> menu.getItemType() != MenuItemType.COUPON)
                    .filter(menu -> !customerOnly || Boolean.TRUE.equals(menu.getIsActive()))
                    .sorted(Comparator.comparing(Menu::getDisplayOrder).thenComparing(Menu::getId))
                    .map(this::convertToMenuDto)
                    .collect(Collectors.toList());
                return CategoryDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .displayOrder(category.getDisplayOrder())
                    .menus(menuDtos)
                    .build();
            })
            .filter(categoryDto -> !customerOnly || (categoryDto.getMenus() != null && !categoryDto.getMenus().isEmpty()))
            .collect(Collectors.toList());

        return SalonMenuResponse.builder()
            .coupons(coupons)
            .categories(categoryDtos)
            .build();
    }

    /**
     * メニューを作成（CreateMenuRequest から）
     */
    public Menu createMenu(Long salonId, CreateMenuRequest request) {
        MenuCategory menuCategory = null;
        if (request.getMenuCategoryId() != null) {
            menuCategory = menuCategoryRepository.findById(request.getMenuCategoryId())
                .orElse(null);
        }

        Menu menu = Menu.builder()
            .salonId(salonId)
            .menuCategory(menuCategory)
            .title(request.getTitle())
            .sectionName(request.getSectionName())
            .description(request.getDescription())
            .imageUrl(request.getImageUrl())
            .originalPrice(request.getOriginalPrice())
            .discountedPrice(request.getDiscountedPrice())
            .durationMinutes(request.getDurationMinutes())
            .itemType(MenuItemType.valueOf(request.getItemType()))
            .tag(request.getTag())
            .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .build();

        return menuRepository.save(menu);
    }

    /**
     * メニューを更新（CreateMenuRequest から）
     */
    public Menu updateMenu(Long menuId, CreateMenuRequest request) {
        Optional<Menu> existingMenu = menuRepository.findById(menuId);
        if (existingMenu.isEmpty()) {
            throw new IllegalArgumentException("Menu not found with id: " + menuId);
        }

        Menu menu = existingMenu.get();
        menu.setTitle(request.getTitle());
        menu.setSectionName(request.getSectionName());
        menu.setDescription(request.getDescription());
        menu.setImageUrl(request.getImageUrl());
        menu.setOriginalPrice(request.getOriginalPrice());
        menu.setDiscountedPrice(request.getDiscountedPrice());
        menu.setDurationMinutes(request.getDurationMinutes());
        menu.setItemType(MenuItemType.valueOf(request.getItemType()));
        menu.setTag(request.getTag());
        menu.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        menu.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        if (request.getMenuCategoryId() != null) {
            MenuCategory menuCategory = menuCategoryRepository.findById(request.getMenuCategoryId())
                .orElse(null);
            menu.setMenuCategory(menuCategory);
        } else {
            menu.setMenuCategory(null);
        }

        return menuRepository.save(menu);
    }

    public Menu updateMenu(Long menuId, UpdateMenuRequest request) {
        Optional<Menu> existingMenu = menuRepository.findById(menuId);
        if (existingMenu.isEmpty()) {
            throw new IllegalArgumentException("Menu not found with id: " + menuId);
        }

        Menu menu = existingMenu.get();
        menu.setTitle(request.getTitle());
        menu.setSectionName(request.getSectionName());
        menu.setDescription(request.getDescription());
        menu.setImageUrl(request.getImageUrl());
        menu.setOriginalPrice(request.getOriginalPrice());
        menu.setDiscountedPrice(request.getDiscountedPrice());
        menu.setDurationMinutes(request.getDurationMinutes());
        menu.setItemType(MenuItemType.valueOf(request.getItemType()));
        menu.setTag(request.getTag());
        menu.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        menu.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        if (request.getMenuCategoryId() != null) {
            MenuCategory menuCategory = menuCategoryRepository.findById(request.getMenuCategoryId())
                .orElse(null);
            menu.setMenuCategory(menuCategory);
        } else {
            menu.setMenuCategory(null);
        }

        return menuRepository.save(menu);
    }

    /**
     * Menu エンティティを MenuDto に変換
     */
    private MenuDto convertToMenuDto(Menu menu) {
        return MenuDto.builder()
            .id(menu.getId())
            .menuCategoryId(menu.getMenuCategory() != null ? menu.getMenuCategory().getId() : null)
            .title(menu.getTitle())
            .sectionName(menu.getSectionName())
            .description(menu.getDescription())
            .imageUrl(menu.getImageUrl())
            .originalPrice(menu.getOriginalPrice())
            .discountedPrice(menu.getDiscountedPrice())
            .durationMinutes(menu.getDurationMinutes())
            .itemType(menu.getItemType().toString())
            .tag(menu.getTag())
            .displayOrder(menu.getDisplayOrder())
            .isActive(menu.getIsActive())
            .build();
    }
}
