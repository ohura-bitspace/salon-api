package jp.bitspace.salon.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jp.bitspace.salon.dto.response.SalesReportResponse;
import jp.bitspace.salon.security.AdminRequestAuthUtil;
import jp.bitspace.salon.service.SalesReportService;

/**
 * 売上レポートコントローラ.
 */
@RestController
@RequestMapping("/api/admin/report/sales")
public class AdminSalesReportController {

    private final SalesReportService salesReportService;
    private final AdminRequestAuthUtil adminRequestAuthUtil;

    public AdminSalesReportController(SalesReportService salesReportService,
            AdminRequestAuthUtil adminRequestAuthUtil) {
        this.salesReportService = salesReportService;
        this.adminRequestAuthUtil = adminRequestAuthUtil;
    }

    /**
     * 売上レポートを取得.
     *
     * @param salonId サロンID
     * @param year    対象年（例: 2025）
     * @param month   対象月（1〜12。省略時は年間集計）
     * @return 売上レポート
     */
    @GetMapping
    public ResponseEntity<SalesReportResponse> getSalesReport(
            HttpServletRequest httpServletRequest,
            @RequestParam Long salonId,
            @RequestParam int year,
            @RequestParam(required = false) Integer month) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        SalesReportResponse response = salesReportService.getReport(salonId, year, month);
        return ResponseEntity.ok(response);
    }
}
