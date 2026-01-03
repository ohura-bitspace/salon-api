package jp.bitspace.salon.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 顧客向けPrincipal.
 * <p>
 * JWTから復元した最小限の情報を保持します。
 */
@Getter
@AllArgsConstructor
public class CustomerPrincipal {
    private final Long customerId;
    private final Long salonId;
}
