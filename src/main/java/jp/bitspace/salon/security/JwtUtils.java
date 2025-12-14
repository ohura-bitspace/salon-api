package jp.bitspace.salon.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
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
        return Jwts.builder()
                .setSubject(String.valueOf(customerId))
                .claim("salonId", salonId)
                .claim("role", "CUSTOMER")
                .claim("userType", "CUSTOMER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 管理側ログイン成功時に呼ぶメソッド
    public String generateToken(Long staffId, String email, Long salonId, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(staffId))
                .claim("email", email)
                .claim("salonId", salonId)
                .claim("role", role)
                .claim("userType", "STAFF")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // リクエストが来た時に呼ぶメソッド
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
