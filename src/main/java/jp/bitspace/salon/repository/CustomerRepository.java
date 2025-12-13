package jp.bitspace.salon.repository;

import jp.bitspace.salon.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByLineUserIdAndSalonId(String lineUserId, Long salonId);
}
