package jp.bitspace.salon.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jp.bitspace.salon.model.BookingRoute;
import jp.bitspace.salon.model.ReservationStatus;

/**
 * 予約作成リクエスト.
 */
public record CreateReservationRequest(
		@NotNull
	    Long salonId,

	    // ※JWT認証を入れたら、ここは不要になります（トークンから取るため）。
	    // 但し、管理側からの予約では必要
	    Long customerId,

	    Long staffId, // 指名なしならnull

	    BookingRoute bookingRoute,
	    /** ステータス. */
	    ReservationStatus status,

	    @NotNull
	    LocalDateTime startTime,
	    
	    // 枠だけ予約のケースで使用
	    LocalDateTime endTime,

	    List<Long> menuIds, // ★ここが重要！ 明細オブジェクトではなく「IDのリスト」にする

	    String memo
) {}
