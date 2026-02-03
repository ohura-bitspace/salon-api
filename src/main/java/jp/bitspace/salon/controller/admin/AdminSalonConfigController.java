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
import jp.bitspace.salon.service.SalonService;
import jp.bitspace.salon.model.Salon;

/**
 * サロン設定管理コントローラー.
 */
@RestController
@RequestMapping("/api/admin/salon-config")
public class AdminSalonConfigController {
    private final SalonConfigService salonConfigService;
    private final AdminRequestAuthUtil adminRequestAuthUtil;
    private final SalonService salonService;

    public AdminSalonConfigController(SalonConfigService salonConfigService, 
                                      AdminRequestAuthUtil adminRequestAuthUtil,
                                      SalonService salonService) {
        this.salonConfigService = salonConfigService;
        this.adminRequestAuthUtil = adminRequestAuthUtil;
        this.salonService = salonService;
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
        Salon salon = salonService.findById(salonId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "サロンが見つかりません"
            ));

        String planType = salon.getPlanType() != null ? salon.getPlanType().name() : null;
        return toResponse(config, salon.getName(), planType);
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
        // サロン名がリクエストに含まれていれば更新する
        if (updateRequest.name() != null) {
            Salon salon = salonService.findById(salonId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "サロンが見つかりません"
                ));
            salon.setName(updateRequest.name());
            salonService.save(salon);
        }
        String salonName = salonService.findById(salonId).map(Salon::getName).orElse(null);
        String planType = salonService.findById(salonId).map(s -> s.getPlanType() != null ? s.getPlanType().name() : null).orElse(null);
        return toResponse(updated, salonName, planType);
    }

    /**
     * エンティティからレスポンスDTOへ変換.
     */
    private SalonConfigResponse toResponse(SalonConfig config, String salonName, String planType) {
        return new SalonConfigResponse(
                config.getId(),
                config.getSalonId(),
                config.getOpeningTime().toString(),
                config.getClosingTime().toString(),
                config.getRegularHolidays(),
                config.getSlotInterval(),
                salonName,
                planType
        );
    }
}
