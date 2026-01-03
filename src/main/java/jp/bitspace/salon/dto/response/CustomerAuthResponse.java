package jp.bitspace.salon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 顧客向け 認証レスポンス.
 */
@Data
@AllArgsConstructor
public class CustomerAuthResponse {
    private String token;
    private CustomerDto customer;
}
