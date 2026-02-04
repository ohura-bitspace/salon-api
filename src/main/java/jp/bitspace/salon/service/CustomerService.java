package jp.bitspace.salon.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jp.bitspace.salon.dto.request.CreateCustomerRequest;
import jp.bitspace.salon.dto.request.UpdateCustomerPersonalInfoRequest;
import jp.bitspace.salon.dto.response.CustomerDetailResponse;
import jp.bitspace.salon.dto.response.CustomerResponse;
import jp.bitspace.salon.dto.response.ReservationSlotsResponse;
import jp.bitspace.salon.dto.response.ReservationTimeSlotDto;
import jp.bitspace.salon.dto.response.VisitHistoryDto;
import jp.bitspace.salon.model.Customer;
import jp.bitspace.salon.model.Menu;
import jp.bitspace.salon.model.Reservation;
import jp.bitspace.salon.model.ReservationItem;
import jp.bitspace.salon.model.ReservationStatus;
import jp.bitspace.salon.model.SalonConfig;
import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.repository.CustomerRepository;
import jp.bitspace.salon.repository.MenuRepository;
import jp.bitspace.salon.repository.ReservationItemRepository;
import jp.bitspace.salon.repository.ReservationRepository;
import jp.bitspace.salon.repository.SalonConfigRepository;
import jp.bitspace.salon.repository.StaffRepository;

/**
 * 顧客サービスクラス.
 */
@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final MenuRepository menuRepository;
    private final StaffRepository staffRepository;
    private final SalonConfigRepository salonConfigRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            ReservationRepository reservationRepository,
            ReservationItemRepository reservationItemRepository,
            MenuRepository menuRepository,
            StaffRepository staffRepository,
            SalonConfigRepository salonConfigRepository
    ) {
        this.customerRepository = customerRepository;
        this.reservationRepository = reservationRepository;
        this.reservationItemRepository = reservationItemRepository;
        this.menuRepository = menuRepository;
        this.staffRepository = staffRepository;
        this.salonConfigRepository = salonConfigRepository;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    /**
     * 顧客IDとサロンIDが一致する顧客を取得します.
     */
    public Customer findByIdAndSalonIdOrThrow(Long customerId, Long salonId) {
        if (customerId == null || salonId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId and salonId are required");
        }

        return customerRepository.findById(customerId)
                .filter(c -> c.getSalonId() != null && c.getSalonId().equals(salonId))
                .filter(c -> c.getIsDeleted() == null || !c.getIsDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    /**
     * LINEコールバック（スタブ）用に固定顧客を取得または作成します.
     */
    public Customer getOrCreateStubCustomer(Long salonId) {
        if (salonId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "salonId is required");
        }

        Optional<Customer> existing = customerRepository.findById(1L)
                .filter(c -> c.getSalonId() != null && c.getSalonId().equals(salonId));
        if (existing.isPresent()) {
            return existing.get();
        }

        Customer customer = new Customer();
        customer.setSalonId(salonId);
        customer.setLineDisplayName("LINEユーザー(スタブ)");
        customer.setLineUserId("stub");
        return customerRepository.save(customer);
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
     * 顧客削除（物理削除）.
     * @param customerId 顧客ID
     * @param salonId サロンID
     */
    @Transactional
    public void deleteCustomer(Long customerId, Long salonId) {
        Customer customer = findByIdAndSalonIdOrThrow(customerId, salonId);
        customerRepository.deleteById(customer.getId());
    }

    /**
     * 顧客作成（管理者による手動作成）.
     * @param request 顧客作成リクエスト
     * @return 作成された顧客
     */
    @Transactional
    public Customer createCustomer(CreateCustomerRequest request) {
        if (request.salonId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "salonId is required");
        }

        Customer customer = new Customer();
        customer.setSalonId(request.salonId());
        customer.setLastName(request.lastName());
        customer.setFirstName(request.firstName());
        customer.setLastNameKana(request.lastNameKana());
        customer.setFirstNameKana(request.firstNameKana());
        customer.setPhoneNumber(request.phoneNumber());
        customer.setEmail(request.email());
        customer.setBirthday(request.birthday());
        customer.setAdminMemo(request.adminMemo());
        customer.setIsDeleted(false);

        return customerRepository.save(customer);
    }

    /**
     * 顧客をCustomerResponseに変換します.
     * @param customer 顧客エンティティ
     * @return 顧客レスポンス
     */
    public CustomerResponse toCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                "", // 作成直後は来店履歴なし
                customer.getLineDisplayName(),
                customer.getLastName(),
                customer.getFirstName()
        );
    }

    /**
     * LINEプロフィールから顧客を作成または更新します.
     */
    public Customer createOrUpdateByLineProfile(Long salonId, String lineUserId, String displayName, String pictureUrl, String email) {
        if (salonId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "salonId is required");
        }
        if (lineUserId == null || lineUserId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lineUserId is required");
        }

        Optional<Customer> existing = customerRepository.findByLineUserIdAndSalonId(lineUserId, salonId);
        Customer customer;
        if (existing.isPresent()) {
            customer = existing.get();
        } else {
            customer = new Customer();
            customer.setSalonId(salonId);
            customer.setLineUserId(lineUserId);
        }

        if (displayName != null) {
            customer.setLineDisplayName(displayName);
        }
        if (pictureUrl != null) {
            customer.setLinePictureUrl(pictureUrl);
        }
        if (email != null && !email.isBlank()) {
            customer.setEmail(email);
        }

        return customerRepository.save(customer);
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
                            lastVisitString,
                            customer.getLineDisplayName(),
                            customer.getLastName(),
                            customer.getFirstName()
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
				.findBySalonIdAndCustomerIdAndStatusOrderByStartTimeDesc(salonId, customerId,
						ReservationStatus.VISITED);

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
                        LocalDate visitDate = reservation.getStartTime() != null ? reservation.getStartTime().toLocalDate() : null;
                        LocalDateTime visitDateTime = reservation.getStartTime();
                        String memo = reservation.getMemo() != null ? reservation.getMemo() : "";
                        String treatmentMemo = reservation.getTreatmentMemo() != null ? reservation.getTreatmentMemo() : "";
                        // 予約ステータス
                        ReservationStatus status = reservation.getStatus();

                        return new VisitHistoryDto(
                            reservation.getId(),
                            visitDate,
                            visitDateTime,
                            menuTitle,
                            staffName,
                            price,
                            status,
                            memo,
                            treatmentMemo);
				})
				.toList();
		
		
		long totSales = 0;
		for (VisitHistoryDto visitHistoryDto : visitHistories) {
			totSales += visitHistoryDto.price();
		}
		
		return new CustomerDetailResponse(
				customer.getId(),
				buildCustomerName(customer),
				buildCustomerNameKana(customer),
				customer.getPhoneNumber(),
				customer.getEmail(),
				customer.getBirthday(),
				customer.getAdminMemo(),
				visitHistories,
				visitHistories.size(),
				totSales,
				customer.getLastName(),
				customer.getFirstName(),
				customer.getLastNameKana(),
				customer.getFirstNameKana());
	}

    /**
     * 来店履歴の施術メモを更新.
     * @param reservationId 予約ID
     * @param treatmentMemo 施術メモ
     * @return 更新後の予約
     */
    @Transactional
    public Reservation updateTreatmentMemo(Long reservationId, String treatmentMemo) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
        reservation.setTreatmentMemo(treatmentMemo);
        return reservationRepository.save(reservation);
    }

    /**
     * 顧客の管理メモを更新.
     * @param customerId 顧客ID
     * @param salonId サロンID
     * @param adminMemo 管理メモ
     * @return 更新後の顧客
     */
    @Transactional
    public Customer updateAdminMemo(Long customerId, Long salonId, String adminMemo) {
        Customer customer = findByIdAndSalonIdOrThrow(customerId, salonId);
        customer.setAdminMemo(adminMemo);
        return customerRepository.save(customer);
    }

    /**
     * 顧客個人情報を更新.
     * <p>
     * lastName, firstName, lastNameKana, firstNameKana, phoneNumber, email, birthday を更新します。
     * @param customerId 顧客ID
     * @param salonId サロンID
     * @param request 顧客個人情報更新リクエスト
     * @return 更新後の顧客
     */
    @Transactional
    public Customer updateCustomerPersonalInfo(Long customerId, Long salonId, UpdateCustomerPersonalInfoRequest request) {
        Customer customer = findByIdAndSalonIdOrThrow(customerId, salonId);
        
        if (request.lastName() != null) {
            customer.setLastName(request.lastName());
        }
        if (request.firstName() != null) {
            customer.setFirstName(request.firstName());
        }
        if (request.lastNameKana() != null) {
            customer.setLastNameKana(request.lastNameKana());
        }
        if (request.firstNameKana() != null) {
            customer.setFirstNameKana(request.firstNameKana());
        }
        if (request.phoneNumber() != null) {
            customer.setPhoneNumber(request.phoneNumber());
        }
        if (request.email() != null) {
            customer.setEmail(request.email());
        }
        if (request.birthday() != null) {
            customer.setBirthday(request.birthday());
        }
        
        return customerRepository.save(customer);
    }

	/**
	 * 顧客向け：予約履歴（来店済み）一覧.
	 * <p>
	 * memo / treatmentMemo は現時点では返さないため null を設定します。
	 */
	public List<VisitHistoryDto> getVisitHistory(Long customerId, Long salonId) {
		if (customerId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");
		}
		if (salonId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "salonId is required");
		}

		// サロン整合性 + 削除フラグ確認
		customerRepository.findById(customerId)
				.filter(c -> c.getSalonId() != null && c.getSalonId().equals(salonId))
				.filter(c -> c.getIsDeleted() == null || !c.getIsDeleted())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        List<Reservation> reservations = reservationRepository
                .findBySalonIdAndCustomerIdAndStatusInOrderByStartTimeDesc(
                        salonId,
                        customerId,
                        List.of(ReservationStatus.VISITED, ReservationStatus.CONFIRMED)
                );

		Set<Long> staffIds = reservations.stream()
				.map(Reservation::getStaffId)
				.filter(id -> id != null)
				.collect(Collectors.toSet());
        Map<Long, Staff> staffById = staffRepository.findAllById(staffIds).stream()
                .collect(Collectors.toMap(Staff::getId, s -> s));

        // 来店済み/確定予約を VisitHistoryDto にマッピング（メモ類は返さない）
        List<VisitHistoryDto> visitHistoryList = reservations.stream()
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
                        LocalDate visitDate = reservation.getStartTime() != null ? reservation.getStartTime().toLocalDate() : null;
                        LocalDateTime visitDateTime = reservation.getStartTime();
                        ReservationStatus status = reservation.getStatus(); // VISITED / CONFIRMED

                        return new VisitHistoryDto(
                            reservation.getId(),
                            visitDate,
                            visitDateTime,
                            menuTitle,
                            staffName,
                            price,
                            status,
                            null,
                            null);
                })
                .toList();

        return visitHistoryList;
	}


    /**
     * 顧客向け：予約時間帯一覧（開始時刻と終了時刻のみ）.
     * <p>
     * from～to の日付範囲内の予約から、startTime と endTime のみを抽出して返します。
     */
    public ReservationSlotsResponse findReservationSlotsBySalonIdAndDateRange(Long salonId, LocalDate from, LocalDate to) {
		
        if (salonId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "salonId is required");
        }
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from/to are required");
        }
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be before to");
        }

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atStartOfDay();

        List<Reservation> reservations = reservationRepository
                .findBySalonIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
                        salonId,
                        fromDateTime,
                        toDateTime
                    );

        List<ReservationTimeSlotDto> slots = reservations.stream()
                .map(r -> new ReservationTimeSlotDto(
                        r.getStartTime().toLocalDate(),
                        r.getStartTime(),
                        r.getEndTime(),
                        false
                    ))
                .toList();
        
        // salonConfigRepositoryから開店時刻、閉店時刻を取得して設定
		Optional<SalonConfig> findBySalonId = salonConfigRepository.findBySalonId(salonId);
		SalonConfig salonConfig = findBySalonId.orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SalonConfig not found. salonId=" + salonId));

		LocalTime openingTime = salonConfig.getOpeningTime();
		LocalTime closingTime = salonConfig.getClosingTime();

        return new ReservationSlotsResponse(openingTime, closingTime, slots);
    }

    private boolean isHolidaySlot(LocalDate date, String regularHolidays) {
        if (regularHolidays == null || regularHolidays.length() < 7) {
            regularHolidays = "0000000";
        }
        int dayOfWeek = date.getDayOfWeek().getValue() - 1; // 0=月曜, ..., 6=日曜
        if (dayOfWeek < 0 || dayOfWeek >= regularHolidays.length()) {
            return false;
        }
        char holidayFlag = regularHolidays.charAt(dayOfWeek);
        return holidayFlag == '1';
    }
}
