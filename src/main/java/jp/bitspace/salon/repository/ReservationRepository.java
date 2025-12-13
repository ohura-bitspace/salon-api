package jp.bitspace.salon.repository;

import java.util.List;
import jp.bitspace.salon.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findBySalonIdOrderByStartAtDesc(Long salonId);
}
