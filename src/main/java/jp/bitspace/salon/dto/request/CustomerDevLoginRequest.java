package jp.bitspace.salon.dto.request;

import lombok.Data;

/**
 * 顧客向け 開発用ログインリクエスト.
 */
@Data
public class CustomerDevLoginRequest {
    /** 所属サロンID */
    private Long salonId;

    /** 顧客ID */
    private Long customerId;
}
