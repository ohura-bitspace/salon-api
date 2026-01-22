package jp.bitspace.salon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * LINEログイン完了レスポンス.
 */
@Data
@AllArgsConstructor
public class CustomerLineLoginResponse {
    private String token;
    private Long salonId;
    private Long customerId;
    private String name;
    private String email;
    private String phone;
}
