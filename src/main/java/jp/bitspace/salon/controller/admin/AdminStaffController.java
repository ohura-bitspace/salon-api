package jp.bitspace.salon.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jp.bitspace.salon.dto.response.StaffResponse;
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
}
