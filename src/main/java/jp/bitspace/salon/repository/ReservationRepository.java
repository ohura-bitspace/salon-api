package jp.bitspace.salon.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.bitspace.salon.model.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    interface CustomerLastVisitProjection {
        Long getCustomerId();

        LocalDateTime getLastVisit();
    }

    List<Reservation> findBySalonIdOrderByStartTimeDesc(Long salonId);

    List<Reservation> findBySalonIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
            Long salonId,
            LocalDateTime from,
            LocalDateTime to
    );

    List<Reservation> findBySalonIdAndCustomerIdIsNullOrderByStartTimeDesc(Long salonId);

        @Query("""
                        SELECT r.customerId AS customerId, MAX(r.startTime) AS lastVisit
                        FROM Reservation r
                        WHERE r.salonId = :salonId
                            AND r.customerId IS NOT NULL
                            AND r.status = jp.bitspace.salon.model.ReservationStatus.VISITED
                        GROUP BY r.customerId
                        """)
        List<CustomerLastVisitProjection> findLastVisitBySalonId(@Param("salonId") Long salonId);
}
