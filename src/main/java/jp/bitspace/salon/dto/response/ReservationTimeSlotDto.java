package jp.bitspace.salon.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 顧客向け：予約時間帯レスポンス.
 * <p>
 * startTime と endTime に加え、日付と定休日フラグを返すDTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationTimeSlotDto {
	/** 対象日 */
	private LocalDate date;

	/** 予約開始日時 */
	private LocalDateTime startTime;
	
	/** 予約終了日時 */
	private LocalDateTime endTime;

	/** 定休日フラグ */
	private boolean holiday;
}
