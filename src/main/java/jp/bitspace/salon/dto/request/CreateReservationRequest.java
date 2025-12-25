package jp.bitspace.salon.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jp.bitspace.salon.model.BookingRoute;

/**
 * 予約作成リクエスト.
 */
public record CreateReservationRequest(
		@NotNull
	    Long salonId,

	    // ※JWT認証を入れたら、ここは不要になります（トークンから取るため）。
	    // 今はまだ認証未実装なので残しておいてOKです。
	    Long customerId,

	    Long staffId, // 指名なしならnull

	    BookingRoute bookingRoute,

	    @NotNull
	    @Future(message = "過去の日時は指定できません")
	    LocalDateTime startTime, // 項目名は startAt が一般的ですが startTime でもOK

	    @NotEmpty(message = "メニューを選択してください")
	    List<Long> menuIds, // ★ここが重要！ 明細オブジェクトではなく「IDのリスト」にする

	    String memo
) {}
