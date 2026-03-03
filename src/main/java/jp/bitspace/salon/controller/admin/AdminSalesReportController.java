package jp.bitspace.salon.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jp.bitspace.salon.dto.response.SalesReportItemDto;
import jp.bitspace.salon.security.AdminRequestAuthUtil;
import jp.bitspace.salon.service.SalesReportService;

/**
 * 売上レポートコントローラ.
 */
@RestController
@RequestMapping("/api/admin/reports/sales")
public class AdminSalesReportController {

    private final SalesReportService salesReportService;
    private final AdminRequestAuthUtil adminRequestAuthUtil;

    public AdminSalesReportController(SalesReportService salesReportService,
            AdminRequestAuthUtil adminRequestAuthUtil) {
        this.salesReportService = salesReportService;
        this.adminRequestAuthUtil = adminRequestAuthUtil;
    }

    /**
     * 売上レポート明細を取得.
     *
     * @param salonId サロンID
     * @param year    対象年（例: 2025）
     * @param month   対象月（1〜12。省略時は year の全月を返す）
     * @return 売上明細リスト
     */
    @GetMapping
    public ResponseEntity<List<SalesReportItemDto>> getSalesReport(
            HttpServletRequest httpServletRequest,
            @RequestParam Long salonId,
            @RequestParam int year,
            @RequestParam(required = false) Integer month) {
        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);
        List<SalesReportItemDto> items = salesReportService.getSalesItems(salonId, year, month);
        return ResponseEntity.ok(items);
    }
}
