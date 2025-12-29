package jp.bitspace.salon.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jp.bitspace.salon.dto.response.CustomerDetailResponse;
import jp.bitspace.salon.dto.response.CustomerResponse;
import jp.bitspace.salon.dto.response.VisitHistoryDto;
import jp.bitspace.salon.model.Customer;
import jp.bitspace.salon.model.Menu;
import jp.bitspace.salon.model.Reservation;
import jp.bitspace.salon.model.ReservationItem;
import jp.bitspace.salon.model.ReservationStatus;
import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.repository.CustomerRepository;
import jp.bitspace.salon.repository.MenuRepository;
import jp.bitspace.salon.repository.ReservationItemRepository;
import jp.bitspace.salon.repository.ReservationRepository;
import jp.bitspace.salon.repository.StaffRepository;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final MenuRepository menuRepository;
    private final StaffRepository staffRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            ReservationRepository reservationRepository,
            ReservationItemRepository reservationItemRepository,
            MenuRepository menuRepository,
            StaffRepository staffRepository
    ) {
        this.customerRepository = customerRepository;
        this.reservationRepository = reservationRepository;
        this.reservationItemRepository = reservationItemRepository;
        this.menuRepository = menuRepository;
        this.staffRepository = staffRepository;
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

        /**
         * カルテ詳細取得.
         * <p>
         * 顧客基本情報 + 来店履歴（予約のVISITED）を返す。
         */
        public CustomerDetailResponse getCustomerDetail(Long customerId, Long salonId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (salonId == null) {
            throw new IllegalArgumentException("salonId is required");
        }

        Customer customer = customerRepository.findById(customerId)
            .filter(c -> c.getSalonId() != null && c.getSalonId().equals(salonId))
            .filter(c -> c.getIsDeleted() == null || !c.getIsDeleted())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        List<Reservation> reservations = reservationRepository
            .findBySalonIdAndCustomerIdAndStatusOrderByStartTimeDesc(salonId, customerId, ReservationStatus.VISITED);

        Set<Long> staffIds = reservations.stream()
            .map(Reservation::getStaffId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
        Map<Long, Staff> staffById = staffRepository.findAllById(staffIds).stream()
            .collect(Collectors.toMap(Staff::getId, s -> s));

        List<VisitHistoryDto> visitHistories = reservations.stream()
            .map(reservation -> {
                List<ReservationItem> items = reservationItemRepository.findByReservationId(reservation.getId());
                Set<Long> menuIds = items.stream()
                    .map(ReservationItem::getMenuId)
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());

                Map<Long, Menu> menuById = menuRepository.findAllById(menuIds).stream()
                    .collect(Collectors.toMap(Menu::getId, m -> m));

                String menuTitle = items.stream()
                    .map(ReservationItem::getMenuId)
                    .filter(id -> id != null)
                    .map(menuById::get)
                    .filter(m -> m != null && m.getTitle() != null)
                    .map(Menu::getTitle)
                    .distinct()
                    .collect(Collectors.joining("、"));

                Staff staff = reservation.getStaffId() == null ? null : staffById.get(reservation.getStaffId());
                String staffName = staff != null && staff.getUser() != null ? staff.getUser().getName() : "";

                    Long price = reservation.getTotalPrice() == null ? null : reservation.getTotalPrice().longValue();
                    String memo = reservation.getMemo() != null ? reservation.getMemo() : "";
                    String treatmentMemo = reservation.getTreatmentMemo() != null ? reservation.getTreatmentMemo() : "";

                return new VisitHistoryDto(
                    reservation.getId(),
                    reservation.getStartTime() != null ? reservation.getStartTime().toLocalDate() : null,
                    menuTitle,
                    staffName,
                    price,
                            memo,
                    treatmentMemo
                );
            })
            .toList();

        return new CustomerDetailResponse(
            customer.getId(),
            buildCustomerName(customer),
            buildCustomerNameKana(customer),
            customer.getPhoneNumber(),
            customer.getEmail(),
            customer.getBirthday(),
            customer.getAdminMemo(),
            visitHistories
        );
        }
}
