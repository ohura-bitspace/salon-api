package jp.bitspace.salon.repository;

import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByUserIdAndSalonId(Long userId, Long salonId);
    List<Staff> findByUser(User user);
    List<Staff> findBySalonId(Long salonId);
    List<Staff> findBySalonIdAndIsPractitionerTrue(Long salonId);
    
    /**
     * サロンIDでスタッフを検索（システム管理者を除く）.
     * 
     * @param salonId サロンID
     * @return システム管理者以外のスタッフリスト
     */
    @Query("SELECT s FROM Staff s JOIN s.user u WHERE s.salon.id = :salonId AND (u.isSystemAdmin = false OR u.isSystemAdmin IS NULL)")
    List<Staff> findBySalonIdExcludingSystemAdmin(@Param("salonId") Long salonId);
}
