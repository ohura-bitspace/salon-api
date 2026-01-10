package jp.bitspace.salon.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.bitspace.salon.model.Payment;

/**
 * 決済リポジトリ.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * サロンIDで決済一覧を取得.
     */
    List<Payment> findBySalonId(Long salonId);
    
    /**
     * 予約IDで決済一覧を取得.
     */
    List<Payment> findByReservationId(Long reservationId);
    
    /**
     * 顧客IDで決済一覧を取得.
     */
    List<Payment> findByCustomerId(Long customerId);
    
    /**
     * サロンIDと期間で決済一覧を取得.
     */
    @Query("SELECT p FROM Payment p WHERE p.salonId = :salonId AND p.paymentAt >= :startDate AND p.paymentAt < :endDate ORDER BY p.paymentAt DESC")
    List<Payment> findBySalonIdAndDateRange(
        @Param("salonId") Long salonId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * サロンIDと顧客IDで決済一覧を取得.
     */
    List<Payment> findBySalonIdAndCustomerId(Long salonId, Long customerId);
}
