package jp.bitspace.salon.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jp.bitspace.salon.model.ReservationStatus;

/**
 * 予約更新リクエスト.
 */
public record UpdateReservationRequest(
        Long staffId,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @NotNull ReservationStatus status,
        String memo
) {}
