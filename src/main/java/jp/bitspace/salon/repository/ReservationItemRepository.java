package jp.bitspace.salon.repository;

import java.util.List;
import jp.bitspace.salon.model.ReservationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationItemRepository extends JpaRepository<ReservationItem, Long> {
    List<ReservationItem> findByReservationId(Long reservationId);
    void deleteByReservationId(Long reservationId);
}
