package jp.bitspace.salon.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jp.bitspace.salon.model.BookingRoute;
import jp.bitspace.salon.model.ReservationStatus;

/**
 * 予約更新リクエスト.
 */
public record UpdateReservationRequest(
        Long staffId,
        /** カスタマID. */
	    Long customerId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @NotNull ReservationStatus status,
        BookingRoute bookingRoute,
        List<Long> menuIds,
        String memo
) {}
