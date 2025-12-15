package jp.bitspace.salon.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.bitspace.salon.dto.request.CreateReservationRequest;
import jp.bitspace.salon.model.Menu;
import jp.bitspace.salon.model.Reservation;
import jp.bitspace.salon.model.ReservationItem;
import jp.bitspace.salon.repository.MenuRepository;
import jp.bitspace.salon.repository.ReservationItemRepository;
import jp.bitspace.salon.repository.ReservationRepository;

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
        return reservationRepository.findBySalonIdOrderByStartTimeDesc(salonId);
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
}
