package jp.bitspace.salon.repository;

import java.util.List;
import jp.bitspace.salon.model.MenuCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    @EntityGraph(attributePaths = "menus")
    List<MenuCategory> findBySalonIdOrderByDisplayOrderAscIdAsc(Long salonId);
}
