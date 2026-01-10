package jp.bitspace.salon.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

	// 顧客・管理 共通で利用する秘密鍵。
    // 優先: jwt.secret -> 互換: app.jwt.secret -> 最終フォールバック（開発用）
    @Value("${jwt.secret:${app.jwt.secret:MySuperSecretKeyForSalonAppMustBeVeryLongAndSecureEnoughToWork==}}")
    private String secretKey;

    // 優先: jwt.expiration-ms -> 互換: app.jwt.expiration-ms -> 7日
    @Value("${jwt.expiration-ms:${app.jwt.expiration-ms:604800000}}")
    private long expirationMs;

    private Key getSigningKey() {
        // Base64エンコードされたキーをデコードして使うのが安全
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 顧客向けJWTを発行します.
     */
	public String generateToken(Long customerId, Long salonId) {

		String tk = Jwts.builder()
				.setSubject(String.valueOf(customerId))
				.claim("salonId", salonId)
                .claim("role", "ROLE_CUSTOMER")
				.claim("userType", "CUSTOMER")
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + expirationMs))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
		return tk;
	}

    // 管理側ログイン成功時に呼ぶメソッド
    public String generateToken(Long staffId, String email, Long salonId, String role, Boolean isSystemAdmin) {
    	
    	String tk = Jwts.builder()
        .setSubject(String.valueOf(staffId))
        .claim("email", email)
        .claim("role", role)
        .claim("salonId", salonId)
        .claim("userType", "STAFF")
        .claim("isSystemAdmin", isSystemAdmin != null && isSystemAdmin)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
    	
    	return tk;
    }

    // リクエストが来た時に呼ぶメソッド
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * トークンを検証してClaimsを返します（署名不正/期限切れ/形式不正は例外）。
     */
    public Claims validateAndExtractClaims(String token) throws JwtException {
        return extractClaims(token);
    }

    /**
     * トークンが有効かどうかを返します.
     */
    public boolean validateToken(String token) {
        try {
            validateAndExtractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 顧客用トークンから customerId を取り出します.
     * <p>
     * 顧客用ではない場合は null を返します。
     */
    public Long getCustomerIdFromToken(String token) {
        Claims claims = validateAndExtractClaims(token);
        Object userType = claims.get("userType");
        Object role = claims.get("role");

        boolean isCustomer = "CUSTOMER".equals(String.valueOf(userType)) || "ROLE_CUSTOMER".equals(String.valueOf(role));
        if (!isCustomer) {
            return null;
        }
        return extractSubjectAsLong(token);
    }

    public Long extractSalonId(String token) {
        Claims claims = validateAndExtractClaims(token);
        Object salonId = claims.get("salonId");
        if (salonId == null) {
            return null;
        }
        if (salonId instanceof Integer i) {
            return i.longValue();
        }
        if (salonId instanceof Long l) {
            return l;
        }
        return Long.valueOf(String.valueOf(salonId));
    }

    public String extractUserType(String token) {
        Claims claims = validateAndExtractClaims(token);
        Object userType = claims.get("userType");
        return userType != null ? String.valueOf(userType) : null;
    }

    public String extractRole(String token) {
        Claims claims = validateAndExtractClaims(token);
        Object role = claims.get("role");
        return role != null ? String.valueOf(role) : null;
    }

    /**
     * トークンからシステム管理者フラグを抽出します.
     */
    public Boolean extractIsSystemAdmin(String token) {
        Claims claims = validateAndExtractClaims(token);
        Object isSystemAdmin = claims.get("isSystemAdmin");
        if (isSystemAdmin == null) {
            return false;
        }
        if (isSystemAdmin instanceof Boolean b) {
            return b;
        }
        return Boolean.parseBoolean(String.valueOf(isSystemAdmin));
    }

    public Long extractSubjectAsLong(String token) {
        Claims claims = validateAndExtractClaims(token);
        String sub = claims.getSubject();
        if (sub == null || sub.isBlank()) {
            return null;
        }
        return Long.valueOf(sub);
    }
}
