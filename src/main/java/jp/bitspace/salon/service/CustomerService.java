package jp.bitspace.salon.service;

import jp.bitspace.salon.model.Customer;
import jp.bitspace.salon.dto.response.CustomerResponse;
import jp.bitspace.salon.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    /**
     * 顧客リストをレスポンスDTOに変換.
     * @return 顧客レスポンスリスト
     */
    public List<CustomerResponse> findAllAsResponse() {
        return findAll().stream()
                .map(customer -> new CustomerResponse(
                    customer.getId(),
                    buildCustomerName(customer),
                    buildCustomerNameKana(customer)
                ))
                .collect(Collectors.toList());
    }

    private String buildCustomerName(Customer customer) {
        if (customer.getLastName() != null && customer.getFirstName() != null) {
            return customer.getLastName() + " " + customer.getFirstName();
        } else if (customer.getLineDisplayName() != null) {
            return customer.getLineDisplayName();
        }
        return "不明";
    }

    private String buildCustomerNameKana(Customer customer) {
        if (customer.getLastNameKana() != null && customer.getFirstNameKana() != null) {
            return customer.getLastNameKana() + " " + customer.getFirstNameKana();
        }
        return "";
    }
}
