package jp.bitspace.salon.repository;

import jp.bitspace.salon.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByLineUserIdAndSalonId(String lineUserId, Long salonId);

        @Query("""
                        SELECT c
                        FROM Customer c
                        WHERE c.salonId = :salonId
                            AND (c.isDeleted = false OR c.isDeleted IS NULL)
                        """)
        List<Customer> findActiveBySalonId(@Param("salonId") Long salonId);
}
