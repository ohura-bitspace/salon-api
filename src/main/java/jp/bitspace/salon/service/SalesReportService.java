package jp.bitspace.salon.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.bitspace.salon.dto.response.SalesReportResponse;
import jp.bitspace.salon.dto.response.SalesReportResponse.SalesReportItem;
import jp.bitspace.salon.dto.response.SalesReportResponse.SalesReportSummary;
import jp.bitspace.salon.model.Customer;
import jp.bitspace.salon.model.Menu;
import jp.bitspace.salon.model.Payment;
import jp.bitspace.salon.model.Reservation;
import jp.bitspace.salon.model.ReservationItem;
import jp.bitspace.salon.model.ReservationStatus;
import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.repository.CustomerRepository;
import jp.bitspace.salon.repository.MenuRepository;
import jp.bitspace.salon.repository.PaymentRepository;
import jp.bitspace.salon.repository.ReservationItemRepository;
import jp.bitspace.salon.repository.ReservationRepository;
import jp.bitspace.salon.repository.StaffRepository;

/**
 * 売上レポートサービス.
 */
@Service
public class SalesReportService {

    private final ReservationRepository reservationRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final MenuRepository menuRepository;

    public SalesReportService(
            ReservationRepository reservationRepository,
            ReservationItemRepository reservationItemRepository,
            PaymentRepository paymentRepository,
            CustomerRepository customerRepository,
            StaffRepository staffRepository,
            MenuRepository menuRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationItemRepository = reservationItemRepository;
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
        this.menuRepository = menuRepository;
    }

    /**
     * 売上レポートを取得.
     *
     * @param salonId サロンID
     * @param year    対象年
     * @param month   対象月（1〜12。nullの場合は年間集計）
     * @return 売上レポートレスポンス
     */
    @Transactional(readOnly = true)
    public SalesReportResponse getReport(Long salonId, int year, Integer month) {
        // 集計期間を決定
        LocalDateTime from;
        LocalDateTime to;
        if (month != null) {
            LocalDate start = LocalDate.of(year, month, 1);
            from = start.atStartOfDay();
            to = start.plusMonths(1).atStartOfDay();
        } else {
            from = LocalDate.of(year, 1, 1).atStartOfDay();
            to = LocalDate.of(year + 1, 1, 1).atStartOfDay();
        }

        // 実績（来店済み）予約を取得
        List<Reservation> actualReservations = reservationRepository
                .findBySalonIdAndStartTimeGreaterThanEqualAndStartTimeLessThanAndStatusInOrderByStartTimeAsc(
                        salonId, from, to, List.of(ReservationStatus.VISITED));

        // 予定（未来の予約）を取得
        List<Reservation> scheduledReservations = reservationRepository
                .findBySalonIdAndStartTimeGreaterThanEqualAndStartTimeLessThanAndStatusInOrderByStartTimeAsc(
                        salonId, from, to, List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));

        // 全予約IDをまとめて関連データを一括取得（N+1対策）
        List<Long> allReservationIds = Stream.concat(
                actualReservations.stream().map(Reservation::getId),
                scheduledReservations.stream().map(Reservation::getId))
                .collect(Collectors.toList());

        // 予約アイテム（メニュー）を一括取得 → reservationId → items
        Map<Long, List<ReservationItem>> itemsByReservationId = reservationItemRepository
                .findByReservationIdIn(allReservationIds).stream()
                .collect(Collectors.groupingBy(ReservationItem::getReservationId));

        // メニューを一括取得 → menuId → menu
        Set<Long> menuIds = itemsByReservationId.values().stream()
                .flatMap(List::stream)
                .map(ReservationItem::getMenuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Menu> menuById = menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getId, m -> m));

        // 実績の支払い情報を一括取得 → reservationId → payment
        List<Long> actualIds = actualReservations.stream().map(Reservation::getId).collect(Collectors.toList());
        Map<Long, Payment> paymentByReservationId = paymentRepository
                .findByReservationIdIn(actualIds).stream()
                .collect(Collectors.toMap(
                        Payment::getReservationId,
                        p -> p,
                        (a, b) -> a // 同一予約に複数paymentがある場合は先頭を使用
                ));

        // 顧客を一括取得 → customerId → customer
        Set<Long> customerIds = Stream.concat(
                actualReservations.stream().map(Reservation::getCustomerId),
                scheduledReservations.stream().map(Reservation::getCustomerId))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Customer> customerById = customerRepository.findAllById(customerIds).stream()
                .collect(Collectors.toMap(Customer::getId, c -> c));

        // スタッフを一括取得 → staffId → staff
        Set<Long> staffIds = Stream.concat(
                actualReservations.stream().map(Reservation::getStaffId),
                scheduledReservations.stream().map(Reservation::getStaffId))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Staff> staffById = staffRepository.findAllById(staffIds).stream()
                .collect(Collectors.toMap(Staff::getId, s -> s));

        // 明細行を生成
        List<SalesReportItem> items = new ArrayList<>();

        for (Reservation r : actualReservations) {
            Payment payment = paymentByReservationId.get(r.getId());
            int amount = payment != null ? payment.getAmount() : r.getTotalPrice();
            String paymentMethod = payment != null ? payment.getPaymentMethod().name() : null;
            items.add(new SalesReportItem(
                    r.getId(),
                    r.getStartTime().toLocalDate(),
                    resolveCustomerName(r.getCustomerId(), customerById),
                    resolveMenuName(r.getId(), itemsByReservationId, menuById),
                    resolveStaffName(r.getStaffId(), staffById),
                    paymentMethod,
                    "done",
                    amount));
        }

        for (Reservation r : scheduledReservations) {
            items.add(new SalesReportItem(
                    r.getId(),
                    r.getStartTime().toLocalDate(),
                    resolveCustomerName(r.getCustomerId(), customerById),
                    resolveMenuName(r.getId(), itemsByReservationId, menuById),
                    resolveStaffName(r.getStaffId(), staffById),
                    null,
                    "scheduled",
                    r.getTotalPrice()));
        }

        // 日付順でソート
        items.sort(Comparator.comparing(SalesReportItem::date));

        // サマリー算出
        int actualAmount = items.stream()
                .filter(i -> "done".equals(i.status()))
                .mapToInt(SalesReportItem::amount).sum();
        int scheduledAmount = items.stream()
                .filter(i -> "scheduled".equals(i.status()))
                .mapToInt(SalesReportItem::amount).sum();
        SalesReportSummary summary = new SalesReportSummary(
                actualAmount,
                actualReservations.size(),
                scheduledAmount,
                scheduledReservations.size(),
                actualAmount + scheduledAmount);

        return new SalesReportResponse(summary, items);
    }

    private String resolveCustomerName(Long customerId, Map<Long, Customer> customerById) {
        if (customerId == null) return null;
        Customer c = customerById.get(customerId);
        if (c == null) return null;
        String last = c.getLastName() != null ? c.getLastName() : "";
        String first = c.getFirstName() != null ? c.getFirstName() : "";
        return (last + " " + first).trim();
    }

    private String resolveMenuName(Long reservationId, Map<Long, List<ReservationItem>> itemsByReservationId,
            Map<Long, Menu> menuById) {
        List<ReservationItem> ritems = itemsByReservationId.getOrDefault(reservationId, List.of());
        return ritems.stream()
                .map(ri -> ri.getMenuId() != null ? menuById.get(ri.getMenuId()) : null)
                .filter(Objects::nonNull)
                .map(Menu::getTitle)
                .collect(Collectors.joining("、"));
    }

    private String resolveStaffName(Long staffId, Map<Long, Staff> staffById) {
        if (staffId == null) return null;
        Staff s = staffById.get(staffId);
        return s != null ? s.getUser().getName() : null;
    }
}
