package jp.bitspace.salon.repository;

import java.util.List;
import jp.bitspace.salon.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findBySalonIdOrderByDisplayOrderAscIdAsc(Long salonId);
}
