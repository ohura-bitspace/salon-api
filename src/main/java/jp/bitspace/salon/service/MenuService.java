package jp.bitspace.salon.service;

import java.util.List;
import java.util.Optional;
import jp.bitspace.salon.model.Menu;
import jp.bitspace.salon.repository.MenuRepository;
import org.springframework.stereotype.Service;

@Service
public class MenuService {
    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
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
}
