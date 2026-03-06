package jp.bitspace.salon.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * メッセージ送信リクエスト.
 */
@Data
public class SendMessageRequest {

    @NotNull(message = "salonIdは必須です")
    private Long salonId;

    @NotNull(message = "customerIdは必須です")
    private Long customerId;

    /** メッセージ本文. */
    @NotNull(message = "textは必須です")
    private String text;
}
