package jp.bitspace.salon.dto.response;

/**
 * サロン設定レスポンス.
 *
 * @param id サロン設定ID
 * @param salonId サロンID
 * @param openingTime 開店時刻（HH:mm形式）
 * @param closingTime 閉店時刻（HH:mm形式）
 * @param regularHolidays 定休日フラグ（7桁文字列、0=営業/1=休み、月曜始まり）
 * @param slotInterval 予約枠の単位（分）
 * @param preparationMarginMinutes 準備時間マージン（分）
 */
public record SalonConfigResponse(
        Long id,
        Long salonId,
        String openingTime,
        String closingTime,
        String regularHolidays,
        Integer slotInterval,
        Integer preparationMarginMinutes,
        String salonName,
        String planType
) {}
