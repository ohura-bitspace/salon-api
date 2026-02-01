package jp.bitspace.salon.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jp.bitspace.salon.dto.request.CreateReservationRequest;
import jp.bitspace.salon.dto.request.UpdateReservationRequest;
import jp.bitspace.salon.dto.response.AdminReservationResponse;
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
    @Transactional(readOnly = true)
    public List<AdminReservationResponse> findAdminBySalonId(Long salonId) {
        List<Reservation> reservations = reservationRepository.findBySalonIdOrderByStartTimeDesc(salonId);
        return reservations.stream().map(this::toAdminReservationResponse).collect(Collectors.toList());
    }

    /**
     * 管理側向け: 日付範囲で予約一覧を取得し、画面用DTOに整形して返却.
     * <p>
     * to は「その日付の0:00」を終端とする半開区間 [from, to) として扱います。
     */
    @Transactional(readOnly = true)
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
                .findBySalonIdAndStartTimeGreaterThanEqualAndStartTimeLessThanAndStatusNotOrderByStartTimeAsc(
                        salonId,
                        fromDateTime,
                        toDateTime,
                        jp.bitspace.salon.model.ReservationStatus.CANCELED
                );

        return reservations.stream().map(this::toAdminReservationResponse).collect(Collectors.toList());
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    /**
     * 管理側向け: 予約IDを指定して1件取得し、画面用DTOに整形して返却.
     */
    @Transactional(readOnly = true)
    public AdminReservationResponse getAdminReservationResponse(Long reservationId, Long salonId) {
        if (reservationId == null) {
            throw new IllegalArgumentException("reservationId is required");
        }
        if (salonId == null) {
            throw new IllegalArgumentException("salonId is required");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .filter(r -> r.getSalonId() != null && r.getSalonId().equals(salonId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

        return toAdminReservationResponse(reservation);
    }

    public List<ReservationItem> findItemsByReservationId(Long reservationId) {
        return reservationItemRepository.findByReservationId(reservationId);
    }

    public Reservation save(Reservation reservation) {
        return reservationRepository.save(reservation);
    }
    
    @Transactional
    public void deleteById(Long id) {
        reservationItemRepository.deleteByReservationId(id);
        reservationRepository.deleteById(id);
    }

    @Transactional
    public Reservation updateWithItems(Long reservationId, UpdateReservationRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        // 基本情報の更新
        reservation.setStaffId(request.staffId());
        reservation.setCustomerId(request.customerId());
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(request.endTime());
        reservation.setStatus(request.status());
        reservation.setBookingRoute(request.bookingRoute());
        reservation.setMemo(request.memo());

        // メニューが指定されている場合、明細を再作成
        if (request.menuIds() != null) {
            // 既存の明細を削除
            reservationItemRepository.deleteByReservationId(reservationId);

            // 新しい明細を作成し、合計金額を再計算
            int totalPrice = 0;
            for (Long menuId : request.menuIds()) {
                if (menuId == null) continue;
                Menu menu = menuRepository.findById(menuId)
                        .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));
                if (!reservation.getSalonId().equals(menu.getSalonId())) {
                    throw new IllegalArgumentException("Menu does not belong to salonId: " + menuId);
                }

                ReservationItem item = new ReservationItem();
                item.setReservationId(reservationId);
                item.setMenuId(menuId);
                item.setPriceAtBooking(determinePrice(menu, null));
                reservationItemRepository.save(item);

                totalPrice += determinePrice(menu, null);
            }
            reservation.setTotalPrice(totalPrice);
        }

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation createWithItems(CreateReservationRequest request) {
        if (request.salonId() == null || request.startTime() == null) {
            throw new IllegalArgumentException("salonId/startTime are required");
        }
        // customerId, menuIds の必須チェックを削除（枠確保・メニュー未定を許容）

        int totalPrice = 0;
        LocalDateTime endTime;

        if (request.menuIds() != null && !request.menuIds().isEmpty()) {
            int totalDurationMinutes = 0;
            for (Long menuId : request.menuIds()) {
                if (menuId == null) {
                    continue;
                }
                Menu menu = menuRepository.findById(menuId)
                        .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));
                if (!request.salonId().equals(menu.getSalonId())) {
                    throw new IllegalArgumentException("Menu does not belong to salonId: " + menuId);
                }
                totalPrice += determinePrice(menu, null);
                totalDurationMinutes += menu.getDurationMinutes() != null ? menu.getDurationMinutes() : 0;
            }
            endTime = request.startTime().plusMinutes(totalDurationMinutes);
        } else {
            // メニュー未定の場合は指定された終了時刻を使用、なければデフォルト60分
            if (request.endTime() != null) {
                endTime = request.endTime();
            } else {
                endTime = request.startTime().plusMinutes(60);
            }
        }

        Reservation reservation = new Reservation();
        reservation.setSalonId(request.salonId());
        reservation.setCustomerId(request.customerId());
        reservation.setStaffId(request.staffId());
		reservation.setBookingRoute(request.bookingRoute());
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(endTime);
        reservation.setMemo(request.memo());
        reservation.setTotalPrice(totalPrice);
        reservation.setStatus(ReservationStatus.CONFIRMED);// 確定

        Reservation saved = reservationRepository.save(reservation);

        if (request.menuIds() != null && !request.menuIds().isEmpty()) {
            for (Long menuId : request.menuIds()) {
                if (menuId == null) continue;
                Menu menu = menuRepository.findById(menuId)
                        .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));

                ReservationItem item = new ReservationItem();
                item.setReservationId(saved.getId());
                item.setMenuId(menuId);
                item.setPriceAtBooking(determinePrice(menu, null));
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

    private AdminReservationResponse toAdminReservationResponse(Reservation reservation) {
        String customerName = "未設定";
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
            	Staff staff = staffOpt.get();
            	if (staff.getUser() != null) {
                    staffName = staff.getUser().getName();
                }
            }
        }

        List<ReservationItem> items = reservationItemRepository.findByReservationId(reservation.getId());
        String menuNames = items.isEmpty() ? "未設定" : items.stream()
            .map(item -> {
                if (item.getMenuId() == null) return "未設定";
                return menuRepository.findById(item.getMenuId()).map(Menu::getTitle).orElse("不明");
            })
            .collect(Collectors.joining("、"));

        List<Long> menuIds = items.stream()
            .map(ReservationItem::getMenuId)
            .filter(id -> id != null)
            .collect(Collectors.toList());

        String statusText = switch (reservation.getStatus()) {
            case PENDING -> "仮予約";
            case CONFIRMED -> "確定";
            case VISITED -> "来店済";
            case CANCELED -> "キャンセル";
        };

        return new AdminReservationResponse(
            String.valueOf(reservation.getId()),
            "Beauty予約",
            reservation.getStartTime(),
            reservation.getEndTime(),
            customerName,
            reservation.getCustomerId(),
            menuIds,
            menuNames,
            staffName,
            reservation.getBookingRoute(),
            statusText,
            reservation.getMemo()
        );
    }

    /**
     * 顧客向け：予約キャンセル.
     * <p>
     * 予約のステータスを CANCELED に変更します。
     * すでに VISITED や CANCELED の場合は例外をスローします。
     * 
     * @param reservationId 予約ID
     * @param customerId ログイン中の顧客ID
     * @param salonId サロンID（バリデーション用）
     * @throws ResponseStatusException 予約が存在しない、権限がない、または不正な状態の場合
     */
    @Transactional
    public void cancelReservation(Long reservationId, Long customerId, Long salonId) {
        if (reservationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reservationId is required");
        }
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");
        }
        if (salonId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "salonId is required");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));

        // サロンIDの整合性確認
        if (!salonId.equals(reservation.getSalonId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid salon");
        }

        // 顧客IDの整合性確認（他人の予約をキャンセルできないようにする）
        if (!customerId.equals(reservation.getCustomerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your reservation");
        }

        // ステータスチェック（来店済みやキャンセル済みはキャンセル不可）
        if (reservation.getStatus() == jp.bitspace.salon.model.ReservationStatus.VISITED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel visited reservation");
        }
        if (reservation.getStatus() == jp.bitspace.salon.model.ReservationStatus.CANCELED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already canceled");
        }

        // ステータスをCANCELEDに更新
        reservation.setStatus(jp.bitspace.salon.model.ReservationStatus.CANCELED);
        reservationRepository.save(reservation);
    }
}
