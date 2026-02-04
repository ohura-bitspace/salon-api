package jp.bitspace.salon.dto.response;

import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 予約可能スロット一覧レスポンス.
 * <p>
 * 営業時間情報とスロットリストを返します。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSlotsResponse {
	
	/** 開店時刻 */
	private LocalTime openingTime;
	
	/** 閉店時刻 */
	private LocalTime closingTime;
	
	/** 予約可能スロットリスト */
	private List<ReservationTimeSlotDto> slots;
}
