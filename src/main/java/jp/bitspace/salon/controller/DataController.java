package jp.bitspace.salon.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jp.bitspace.salon.dto.request.CreateCustomerRequest;
import jp.bitspace.salon.dto.request.UpdateAdminMemoRequest;
import jp.bitspace.salon.dto.request.UpdateCustomerPersonalInfoRequest;
import jp.bitspace.salon.dto.request.UpdateTreatmentMemoRequest;
import jp.bitspace.salon.dto.response.CustomerDetailResponse;
import jp.bitspace.salon.dto.response.CustomerResponse;
import jp.bitspace.salon.model.Customer;
import jp.bitspace.salon.model.Salon;
import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.security.AdminRequestAuthUtil;
import jp.bitspace.salon.service.CustomerService;
import jp.bitspace.salon.service.SalonService;
import jp.bitspace.salon.service.StaffService;

@RestController
@RequestMapping("/api")
public class DataController {
    private final SalonService salonService;
    private final StaffService staffService;
    private final CustomerService customerService;
    private final AdminRequestAuthUtil adminRequestAuthUtil;

    public DataController(SalonService salonService, StaffService staffService, CustomerService customerService, AdminRequestAuthUtil adminRequestAuthUtil) {
        this.salonService = salonService;
        this.staffService = staffService;
        this.customerService = customerService;
        this.adminRequestAuthUtil = adminRequestAuthUtil;
    }

    @GetMapping("/salons")
    public List<Salon> getAllSalons() {
        return salonService.findAll();
    }

    @GetMapping("/staffs")
    public List<Staff> getAllStaffs() {
        return staffService.findAll();
    }
    
    /**
     * 顧客リスト取得.
     * @return 顧客リスト
     */
    @GetMapping("/customers")
	public List<CustomerResponse> getAllCustomers(
			HttpServletRequest httpServletRequest,
			@RequestParam(name = "salonId") Long salonId) {
		adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
		return customerService.findAllAsResponse(salonId);
	}

    /**
     * 顧客作成（管理者による手動作成）.
     * @param request 顧客作成リクエスト
     * @return 作成された顧客レスポンス
     */
    @PostMapping("/customers")
    public ResponseEntity<CustomerResponse> createCustomer(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody CreateCustomerRequest request) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, request.salonId());
        Customer created = customerService.createCustomer(request);
        CustomerResponse response = customerService.toCustomerResponse(created);
        return ResponseEntity.ok(response);
    }

    /**
     * 顧客削除.
     * <p>
     * 顧客を物理削除します。
     * @param customerId 顧客ID
     * @param salonId サロンID
     * @return 削除成功（204 No Content）
     */
    @DeleteMapping("/customers/{customerId}")
    public ResponseEntity<Void> deleteCustomer(
            HttpServletRequest httpServletRequest,
            @PathVariable Long customerId,
            @RequestParam(name = "salonId") Long salonId) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        customerService.deleteCustomer(customerId, salonId);
        return ResponseEntity.noContent().build();
    }

    /**
     * カルテ詳細取得.
     * <p>
     * 顧客IDに紐づくカルテ詳細（基本情報 + 来店履歴など）を返します。
     * ※中身の実装は後続対応。
     */
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<CustomerDetailResponse> getCustomerDetail(
            HttpServletRequest httpServletRequest,
            @PathVariable Long customerId,
            @RequestParam(name = "salonId") Long salonId) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        CustomerDetailResponse detail = customerService.getCustomerDetail(customerId, salonId);
        return ResponseEntity.ok(detail);
    }

    /**
     * 来店履歴の施術メモを更新.
     * <p>
     * 予約に紐づく施術メモ（treatmentMemo）のみを更新します。
     */
    @PutMapping("/visit-histories/{visitId}/treatment-memo")
    public ResponseEntity<Void> updateTreatmentMemo(
            HttpServletRequest httpServletRequest,
            @PathVariable Long visitId,
            @RequestParam(name = "salonId") Long salonId,
            @RequestBody UpdateTreatmentMemoRequest request) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        customerService.updateTreatmentMemo(visitId, request.treatmentMemo());
        return ResponseEntity.noContent().build();
    }

    /**
     * 顧客の管理メモを更新.
     * <p>
     * 顧客に紐づく管理メモ（adminMemo）のみを更新します。
     */
    @PutMapping("/customers/{customerId}/admin-memo")
    public ResponseEntity<Void> updateAdminMemo(
            HttpServletRequest httpServletRequest,
            @PathVariable Long customerId,
            @RequestParam(name = "salonId") Long salonId,
            @RequestBody UpdateAdminMemoRequest request) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        customerService.updateAdminMemo(customerId, salonId, request.adminMemo());
        return ResponseEntity.noContent().build();
    }

    /**
     * 顧客個人情報を更新.
     * <p>
     * lastName, firstName, lastNameKana, firstNameKana, phoneNumber, email, birthday を更新します。
     * @param customerId 顧客ID
     * @param salonId サロンID
     * @param request 顧客個人情報更新リクエスト
     * @return 204 No Content
     */
    @PutMapping("/customers/{customerId}/personal-info")
    public ResponseEntity<Void> updateCustomerPersonalInfo(
            HttpServletRequest httpServletRequest,
            @PathVariable Long customerId,
            @RequestParam(name = "salonId") Long salonId,
            @RequestBody UpdateCustomerPersonalInfoRequest request) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        customerService.updateCustomerPersonalInfo(customerId, salonId, request);
        return ResponseEntity.noContent().build();
    }
}
