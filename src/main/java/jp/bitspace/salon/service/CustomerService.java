package jp.bitspace.salon.service;

import jp.bitspace.salon.model.Customer;
import jp.bitspace.salon.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    public void deleteById(Long id) {
        customerRepository.deleteById(id);
    }

    public Optional<Customer> findByLineUserIdAndSalonId(String lineUserId, Long salonId) {
        return customerRepository.findByLineUserIdAndSalonId(lineUserId, salonId);
    }

    /**
     * 開発用: IDを指定してログイン（LINE連携スキップ）
     */
    public Optional<Customer> loginByDevId(Long id) {
        return customerRepository.findById(id);
    }
}
