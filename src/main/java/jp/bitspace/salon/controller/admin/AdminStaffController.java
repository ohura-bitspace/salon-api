package jp.bitspace.salon.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jp.bitspace.salon.dto.request.CreateStaffRequest;
import jp.bitspace.salon.dto.request.UpdateStaffRequest;
import jp.bitspace.salon.dto.response.StaffResponse;
import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.security.AdminRequestAuthUtil;
import jp.bitspace.salon.service.StaffService;

@RestController
@RequestMapping("/api/admin/staffs")
public class AdminStaffController {
    private final StaffService staffService;
    private final AdminRequestAuthUtil adminRequestAuthUtil;

    public AdminStaffController(StaffService staffService, AdminRequestAuthUtil adminRequestAuthUtil) {
        this.staffService = staffService;
        this.adminRequestAuthUtil = adminRequestAuthUtil;
    }

    /**
     * 管理画面用: 所属サロンの全スタッフ取得（施術者/非施術者どちらも含む）
     */
    @GetMapping
    public List<StaffResponse> getAllStaffs(
            HttpServletRequest httpServletRequest,
            @RequestParam(name = "salonId") Long salonId
    ) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        return staffService.findStaffResponseBySalonId(salonId);
    }

    /**
     * 予約画面用: 施術者のみ取得.
     */
    @GetMapping("/practitioners")
    public List<StaffResponse> getPractitioners(
            HttpServletRequest httpServletRequest,
            @RequestParam(name = "salonId") Long salonId
    ) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        return staffService.findPractitionersResponseBySalonId(salonId);
    }

    /**
     * スタッフ詳細取得（編集時に使用）
     */
    @GetMapping("/{id}")
    public StaffResponse getStaffById(
            HttpServletRequest httpServletRequest,
            @PathVariable Long id,
            @RequestParam(name = "salonId") Long salonId
    ) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        StaffResponse staffRespons = staffService.findStaffResponseById(id, salonId);
        System.out.println(staffRespons);
        return staffRespons;
    }

    /**
     * スタッフ作成
     */
    @PostMapping
	public StaffResponse createStaff(
			HttpServletRequest httpServletRequest,
			@RequestParam(name = "salonId") Long salonId,
			@RequestBody CreateStaffRequest request) {
		adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
		request.setSalonId(salonId);
		Staff newStaff = staffService.createStaff(request);
		StaffResponse response = staffService.findStaffResponseById(newStaff.getId(), salonId);
		return response;
	}

    /**
     * スタッフ更新
     */
    @PutMapping("/{id}")
    public StaffResponse updateStaff(
            HttpServletRequest httpServletRequest,
            @PathVariable Long id,
            @RequestParam(name = "salonId", required = false) Long salonId,
            @RequestBody UpdateStaffRequest request
    ) {
        Long resolvedSalonId = resolveSalonId(id, salonId);
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, resolvedSalonId);
        staffService.updateStaff(id, request);
        return staffService.findStaffResponseById(id, resolvedSalonId);
    }

    /**
     * スタッフ削除
     */
    @DeleteMapping("/{id}")
    public void deleteStaff(
            HttpServletRequest httpServletRequest,
            @PathVariable Long id,
            @RequestParam(name = "salonId", required = false) Long salonId
    ) {
        Long resolvedSalonId = resolveSalonId(id, salonId);
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, resolvedSalonId);
        staffService.deleteById(id);
    }

    private Long resolveSalonId(Long staffId, Long requestedSalonId) {
        if (requestedSalonId != null) {
            return requestedSalonId;
        }
        return staffService.findById(staffId)
                .map(staff -> staff.getSalon() != null ? staff.getSalon().getId() : null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff not found: " + staffId));
    }
}
