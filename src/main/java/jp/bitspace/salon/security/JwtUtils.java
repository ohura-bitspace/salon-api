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

	// application.properties の "app.jwt.secret" を読み込む
    @Value("${app.jwt.secret}")
    private String secretKey; // 変数名をキャメルケースに変更するのが一般的

    // application.properties の "app.jwt.expiration-ms" を読み込む
    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private Key getSigningKey() {
        // Base64エンコードされたキーをデコードして使うのが安全
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ログイン成功時に呼ぶメソッド
	public String generateToken(Long customerId, Long salonId) {

		String tk = Jwts.builder()
				.setSubject(String.valueOf(customerId))
				.claim("salonId", salonId)
				.claim("role", "CUSTOMER")
				.claim("userType", "CUSTOMER")
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + expirationMs))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
		return tk;
	}

    // 管理側ログイン成功時に呼ぶメソッド
    public String generateToken(Long staffId, String email, Long salonId, String role) {
    	
    	String tk = Jwts.builder()
        .setSubject(String.valueOf(staffId))
        .claim("email", email)
        .claim("role", role)
        .claim("userType", "STAFF")
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

    public Long extractSubjectAsLong(String token) {
        Claims claims = validateAndExtractClaims(token);
        String sub = claims.getSubject();
        if (sub == null || sub.isBlank()) {
            return null;
        }
        return Long.valueOf(sub);
    }
}
