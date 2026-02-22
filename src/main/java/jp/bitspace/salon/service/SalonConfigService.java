package jp.bitspace.salon.service;

import jp.bitspace.salon.model.SalonConfig;
import jp.bitspace.salon.repository.SalonConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * サロン設定サービス.
 */
@Service
public class SalonConfigService {
    private final SalonConfigRepository salonConfigRepository;

    public SalonConfigService(SalonConfigRepository salonConfigRepository) {
        this.salonConfigRepository = salonConfigRepository;
    }

    /**
     * サロンIDで設定を取得.
     *
     * @param salonId サロンID
     * @return サロン設定
     */
    public Optional<SalonConfig> findBySalonId(Long salonId) {
        return salonConfigRepository.findBySalonId(salonId);
    }

    /**
     * サロン設定を保存または更新.
     *
     * @param salonConfig サロン設定
     * @return 保存されたサロン設定
     */
    @Transactional
    public SalonConfig save(SalonConfig salonConfig) {
        return salonConfigRepository.save(salonConfig);
    }

    /**
     * サロン設定を更新（既存レコードがない場合は新規作成）.
     *
     * @param salonId サロンID
     * @param openingTime 開店時刻（HH:mm形式）
     * @param closingTime 閉店時刻（HH:mm形式）
     * @param regularHolidays 定休日フラグ（7桁文字列）
     * @param slotInterval 予約枠の単位（分）
     * @param preparationMarginMinutes 準備時間マージン（分）
     * @return 更新されたサロン設定
     */
    @Transactional
    public SalonConfig updateOrCreate(Long salonId, String openingTime, String closingTime,
                                      String regularHolidays, Integer slotInterval,
                                      Integer preparationMarginMinutes) {
        SalonConfig config = salonConfigRepository.findBySalonId(salonId)
                .orElse(new SalonConfig());

        config.setSalonId(salonId);
        config.setOpeningTime(java.time.LocalTime.parse(openingTime));
        config.setClosingTime(java.time.LocalTime.parse(closingTime));
        config.setRegularHolidays(regularHolidays);
        config.setSlotInterval(slotInterval);
        if (preparationMarginMinutes != null) {
            config.setPreparationMarginMinutes(preparationMarginMinutes);
        }

        return salonConfigRepository.save(config);
    }
}
