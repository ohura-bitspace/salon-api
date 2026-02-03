package jp.bitspace.salon.dto.request;

/**
 * サロン設定更新リクエスト.
 *
 * @param openingTime 開店時刻（HH:mm形式、例: "09:00"）
 * @param closingTime 閉店時刻（HH:mm形式、例: "21:00"）
 * @param regularHolidays 定休日フラグ（7桁文字列、0=営業/1=休み、月曜始まり、例: "0000011"=土日休み）
 * @param slotInterval 予約枠の単位（分、例: 30）
 */
public record UpdateSalonConfigRequest(
        String openingTime,
        String closingTime,
        String regularHolidays,
        Integer slotInterval,
        String name
) {}
