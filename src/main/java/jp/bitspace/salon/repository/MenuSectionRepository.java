package jp.bitspace.salon.repository;

import java.util.List;
import jp.bitspace.salon.model.MenuSection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuSectionRepository extends JpaRepository<MenuSection, Long> {
    @EntityGraph(attributePaths = "menus")
    List<MenuSection> findByMenuCategoryIdOrderByDisplayOrderAsc(Long menuCategoryId);
}
