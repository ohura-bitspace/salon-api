package jp.bitspace.salon.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import jp.bitspace.salon.dto.response.CustomerResponse;
import jp.bitspace.salon.model.Customer;
import jp.bitspace.salon.repository.CustomerRepository;
import jp.bitspace.salon.repository.ReservationRepository;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final ReservationRepository reservationRepository;

    public CustomerService(CustomerRepository customerRepository, ReservationRepository reservationRepository) {
        this.customerRepository = customerRepository;
        this.reservationRepository = reservationRepository;
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
     * @param salonId 
     * @return 顧客レスポンスリスト
     */
    public List<CustomerResponse> findAllAsResponse(Long salonId) {

        List<Customer> customers = customerRepository.findActiveBySalonId(salonId);

        Map<Long, LocalDateTime> lastVisitByCustomerId = new HashMap<>();
        for (ReservationRepository.CustomerLastVisitProjection projection : reservationRepository.findLastVisitBySalonId(salonId)) {
            lastVisitByCustomerId.put(projection.getCustomerId(), projection.getLastVisit());
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

        return customers.stream()
                .map(customer -> {
                    LocalDateTime lastVisit = lastVisitByCustomerId.get(customer.getId());
                    String lastVisitString = lastVisit == null ? "" : dateFormatter.format(lastVisit.toLocalDate());
                    return new CustomerResponse(
                            customer.getId(),
                            buildCustomerName(customer),
                            buildCustomerNameKana(customer),
                            lastVisitString
                    );
                })
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
