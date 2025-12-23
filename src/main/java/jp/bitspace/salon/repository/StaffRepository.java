package jp.bitspace.salon.repository;

import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByUserIdAndSalonId(Long userId, Long salonId);
    List<Staff> findByUser(User user);
    List<Staff> findBySalonId(Long salonId);
    List<Staff> findBySalonIdAndIsPractitionerTrue(Long salonId);
}
