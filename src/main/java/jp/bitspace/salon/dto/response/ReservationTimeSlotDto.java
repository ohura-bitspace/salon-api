package jp.bitspace.salon.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 顧客向け：予約時間帯レスポンス.
 * <p>
 * startTime と endTime のみを返すシンプルなDTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationTimeSlotDto {
	/** 予約開始日時 */
	private LocalDateTime startTime;
	
	/** 予約終了日時 */
	private LocalDateTime endTime;
}
