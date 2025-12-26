package jp.bitspace.salon.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.bitspace.salon.model.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findBySalonIdOrderByStartTimeDesc(Long salonId);

    List<Reservation> findBySalonIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
            Long salonId,
            LocalDateTime from,
            LocalDateTime to
    );

    List<Reservation> findBySalonIdAndCustomerIdIsNullOrderByStartTimeDesc(Long salonId);
}
