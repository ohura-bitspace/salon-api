package jp.bitspace.salon.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.bitspace.salon.dto.request.CreateReservationRequest;
import jp.bitspace.salon.dto.response.AdminReservationResponse;
import jp.bitspace.salon.model.Customer;
import jp.bitspace.salon.model.Menu;
import jp.bitspace.salon.model.Reservation;
import jp.bitspace.salon.model.ReservationItem;
import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.repository.CustomerRepository;
import jp.bitspace.salon.repository.MenuRepository;
import jp.bitspace.salon.repository.ReservationItemRepository;
import jp.bitspace.salon.repository.ReservationRepository;
import jp.bitspace.salon.repository.StaffRepository;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final MenuRepository menuRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;

    public ReservationService(
        ReservationRepository reservationRepository,
        ReservationItemRepository reservationItemRepository,
        MenuRepository menuRepository,
        CustomerRepository customerRepository,
        StaffRepository staffRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationItemRepository = reservationItemRepository;
        this.menuRepository = menuRepository;
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public List<Reservation> findBySalonId(Long salonId) {
        return reservationRepository.findBySalonIdOrderByStartTimeDesc(salonId);
    }

    /**
     * 管理側向け: 予約一覧を画面用DTOに整形して返却.
     */
    public List<AdminReservationResponse> findAdminBySalonId(Long salonId) {
        List<Reservation> reservations = reservationRepository.findBySalonIdOrderByStartTimeDesc(salonId);
        return reservations.stream().map(this::toAdminReservationResponse).collect(Collectors.toList());
    }

    /**
     * 管理側向け: 日付範囲で予約一覧を取得し、画面用DTOに整形して返却.
     * <p>
     * to は「その日付の0:00」を終端とする半開区間 [from, to) として扱います。
     */
    public List<AdminReservationResponse> findBySalonIdAndDateRange(Long salonId, LocalDate from, LocalDate to) {
        if (salonId == null) {
            throw new IllegalArgumentException("salonId is required");
        }
        if (from == null || to == null) {
            throw new IllegalArgumentException("from/to are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before to");
        }

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atStartOfDay();

        List<Reservation> reservations = reservationRepository
                .findBySalonIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
                        salonId,
                        fromDateTime,
                        toDateTime
                );

        return reservations.stream().map(this::toAdminReservationResponse).collect(Collectors.toList());
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    public List<ReservationItem> findItemsByReservationId(Long reservationId) {
        return reservationItemRepository.findByReservationId(reservationId);
    }

    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public void deleteById(Long id) {
        reservationItemRepository.deleteByReservationId(id);
        reservationRepository.deleteById(id);
    }

    @Transactional
    public Reservation createWithItems(CreateReservationRequest request) {
        if (request.salonId() == null || request.startTime() == null) {
            throw new IllegalArgumentException("salonId/startTime are required");
        }
        if (request.customerId() == null) {
            throw new IllegalArgumentException("customerId is required (until JWT auth is implemented)");
        }
        if (request.menuIds() == null || request.menuIds().isEmpty()) {
            throw new IllegalArgumentException("menuIds is required");
        }

        int totalPrice = 0;
        int totalDurationMinutes = 0;

        for (Long menuId : request.menuIds()) {
            if (menuId == null) {
                throw new IllegalArgumentException("menuIds contains null");
            }
            Menu menu = menuRepository.findById(menuId)
                    .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));
            if (!request.salonId().equals(menu.getSalonId())) {
                throw new IllegalArgumentException("Menu does not belong to salonId: " + menuId);
            }
            totalPrice += determinePrice(menu, null);
            totalDurationMinutes += menu.getDurationMinutes() != null ? menu.getDurationMinutes() : 0;
        }

        LocalDateTime endTime = request.startTime().plusMinutes(totalDurationMinutes);

        Reservation reservation = new Reservation();
        reservation.setSalonId(request.salonId());
        reservation.setCustomerId(request.customerId());
        reservation.setStaffId(request.staffId());
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(endTime);
        reservation.setMemo(request.memo());
        reservation.setTotalPrice(totalPrice);

        Reservation saved = reservationRepository.save(reservation);

        for (Long menuId : request.menuIds()) {
            Menu menu = menuRepository.findById(menuId)
                    .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));

            ReservationItem item = new ReservationItem();
            item.setReservationId(saved.getId());
            item.setMenuId(menuId);
            item.setPriceAtBooking(determinePrice(menu, null));
            reservationItemRepository.save(item);
        }

        return saved;
    }

    private int determinePrice(Menu menu, Integer requestedPrice) {
        if (requestedPrice != null) {
            return requestedPrice;
        }
        if (menu.getDiscountedPrice() != null) {
            return menu.getDiscountedPrice();
        }
        return menu.getOriginalPrice() != null ? menu.getOriginalPrice() : 0;
    }

    private AdminReservationResponse toAdminReservationResponse(Reservation reservation) {
        String customerName = "不明";
        if (reservation.getCustomerId() != null) {
            Optional<Customer> customerOpt = customerRepository.findById(reservation.getCustomerId());
            if (customerOpt.isPresent()) {
                Customer customer = customerOpt.get();
                if (customer.getLastName() != null && customer.getFirstName() != null) {
                    customerName = customer.getLastName() + " " + customer.getFirstName();
                } else if (customer.getLineDisplayName() != null) {
                    customerName = customer.getLineDisplayName();
                }
            }
        }

        String staffName = "未定";
        if (reservation.getStaffId() != null) {
            Optional<Staff> staffOpt = staffRepository.findById(reservation.getStaffId());
            if (staffOpt.isPresent()) {
                staffName = staffOpt.get().getName();
            }
        }

        List<ReservationItem> items = reservationItemRepository.findByReservationId(reservation.getId());
        String menuNames = items.stream()
                .map(item -> menuRepository.findById(item.getMenuId()).map(Menu::getTitle).orElse("不明"))
                .collect(Collectors.joining("、"));

        String statusText = switch (reservation.getStatus()) {
            case PENDING -> "仮予約";
            case CONFIRMED -> "確定";
            case VISITED -> "来店";
            case CANCELED -> "キャンセル";
        };

        return new AdminReservationResponse(
                String.valueOf(reservation.getId()),
                "Beauty予約",
                reservation.getStartTime(),
                reservation.getEndTime(),
                customerName,
                menuNames,
                staffName,
                statusText,
                reservation.getMemo()
        );
    }
}
