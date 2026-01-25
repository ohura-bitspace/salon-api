package jp.bitspace.salon.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jp.bitspace.salon.dto.request.UpdateSalonConfigRequest;
import jp.bitspace.salon.dto.response.SalonConfigResponse;
import jp.bitspace.salon.model.SalonConfig;
import jp.bitspace.salon.security.AdminRequestAuthUtil;
import jp.bitspace.salon.service.SalonConfigService;

/**
 * サロン設定管理コントローラー.
 */
@RestController
@RequestMapping("/api/admin/salon-config")
public class AdminSalonConfigController {
    private final SalonConfigService salonConfigService;
    private final AdminRequestAuthUtil adminRequestAuthUtil;

    public AdminSalonConfigController(SalonConfigService salonConfigService, 
                                      AdminRequestAuthUtil adminRequestAuthUtil) {
        this.salonConfigService = salonConfigService;
        this.adminRequestAuthUtil = adminRequestAuthUtil;
    }

    /**
     * サロン設定取得.
     */
    @GetMapping
    public SalonConfigResponse getSalonConfig(HttpServletRequest request, @RequestParam(name = "salonId") Long salonId) {
    	// 管理API共通の認可チェック（STAFFトークン + salonId一致）
    	adminRequestAuthUtil.requireStaffAndSalonMatch(request, salonId);
        SalonConfig config = salonConfigService.findBySalonId(salonId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "サロン設定が見つかりません"
                ));

        return toResponse(config);
    }

    /**
     * サロン設定更新.
     */
    @PutMapping
    public SalonConfigResponse updateSalonConfig(
    		HttpServletRequest request, @RequestParam(name = "salonId") Long salonId,
            @RequestBody UpdateSalonConfigRequest updateRequest) {
        // 認証とサロンID取得
    	adminRequestAuthUtil.requireStaffAndSalonMatch(request, salonId);
        // 更新または新規作成
        SalonConfig updated = salonConfigService.updateOrCreate(
                salonId,
                updateRequest.openingTime(),
                updateRequest.closingTime(),
                updateRequest.regularHolidays(),
                updateRequest.slotInterval()
        );

        return toResponse(updated);
    }

    /**
     * エンティティからレスポンスDTOへ変換.
     */
    private SalonConfigResponse toResponse(SalonConfig config) {
        return new SalonConfigResponse(
                config.getId(),
                config.getSalonId(),
                config.getOpeningTime().toString(),
                config.getClosingTime().toString(),
                config.getRegularHolidays(),
                config.getSlotInterval()
        );
    }
}
