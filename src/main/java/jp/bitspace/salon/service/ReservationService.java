package jp.bitspace.salon.service;

import java.util.List;
import java.util.Optional;
import jp.bitspace.salon.controller.dto.CreateReservationItemRequest;
import jp.bitspace.salon.controller.dto.CreateReservationRequest;
import jp.bitspace.salon.model.Menu;
import jp.bitspace.salon.model.Reservation;
import jp.bitspace.salon.model.ReservationItem;
import jp.bitspace.salon.model.ReservationStatus;
import jp.bitspace.salon.repository.MenuRepository;
import jp.bitspace.salon.repository.ReservationItemRepository;
import jp.bitspace.salon.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final MenuRepository menuRepository;

    public ReservationService(
        ReservationRepository reservationRepository,
        ReservationItemRepository reservationItemRepository,
        MenuRepository menuRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationItemRepository = reservationItemRepository;
        this.menuRepository = menuRepository;
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public List<Reservation> findBySalonId(Long salonId) {
        return reservationRepository.findBySalonIdOrderByStartAtDesc(salonId);
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
        if (request.salonId() == null || request.customerId() == null || request.startAt() == null || request.endAt() == null) {
            throw new IllegalArgumentException("salonId/customerId/startAt/endAt are required");
        }

        Reservation reservation = new Reservation();
        reservation.setSalonId(request.salonId());
        reservation.setCustomerId(request.customerId());
        reservation.setStaffId(request.staffId());
        reservation.setStartAt(request.startAt());
        reservation.setEndAt(request.endAt());
        reservation.setStatus(request.status() != null ? request.status() : ReservationStatus.PENDING);
        reservation.setMemo(request.memo());

        int total = 0;
        if (request.items() != null) {
            for (CreateReservationItemRequest itemReq : request.items()) {
                if (itemReq == null || itemReq.menuId() == null) {
                    throw new IllegalArgumentException("menuId is required for each item");
                }
                Menu menu = menuRepository.findById(itemReq.menuId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + itemReq.menuId()));
                if (!request.salonId().equals(menu.getSalonId())) {
                    throw new IllegalArgumentException("Menu does not belong to salonId: " + itemReq.menuId());
                }
                int price = determinePrice(menu, itemReq.priceAtBooking());
                total += price;
            }
        }
        reservation.setTotalPrice(total);

        Reservation saved = reservationRepository.save(reservation);

        if (request.items() != null) {
            for (CreateReservationItemRequest itemReq : request.items()) {
                Menu menu = menuRepository.findById(itemReq.menuId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + itemReq.menuId()));
                int price = determinePrice(menu, itemReq.priceAtBooking());

                ReservationItem item = new ReservationItem();
                item.setReservationId(saved.getId());
                item.setMenuId(itemReq.menuId());
                item.setPriceAtBooking(price);
                reservationItemRepository.save(item);
            }
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
}
