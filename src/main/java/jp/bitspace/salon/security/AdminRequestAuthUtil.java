package jp.bitspace.salon.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 管理API向けの認可チェック用ユーティリティ.
 *
 * <p>
 * コントローラ内で愚直に書くと同じ処理が増えるため、
 * 「Bearerトークンの取得」→「JWT解析」→「STAFF判定」→「salonId一致判定」
 * をここに集約する.
 * </p>
 */
@Component
public class AdminRequestAuthUtil {
    private final JwtUtils jwtUtils;

    public AdminRequestAuthUtil(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * 管理用APIとしてアクセス可能かをチェックする.
     *
     * <p>
     * - Authorizationヘッダが無い/不正 → 401
     * - userType が STAFF でない → 403
     * - リクエストの salonId と トークン内 salonId が一致しない → 403
     * </p>
     *
     * @param request HTTPリクエスト
     * @param requestedSalonId クエリ等で受け取ったsalonId
     * @return トークン内の salonId（後続処理で使いたい場合のため返す）
     */
    public Long requireStaffAndSalonMatch(HttpServletRequest request, Long requestedSalonId) {
    	// TODO チェック内容は後日整理する
        
    	// リクエストヘッダからトークン取得
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isBlank() || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        // `Bearer ` の後ろが実トークン
        String token = authorization.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        // 署名不正・期限切れ等は JwtUtils 側で例外になる想定
        String userType;
        Long tokenSalonId;
        Boolean isSystemAdmin;
        try {
            userType = jwtUtils.extractUserType(token);
            tokenSalonId = jwtUtils.extractSalonId(token);
            isSystemAdmin = jwtUtils.extractIsSystemAdmin(token);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        // 管理APIは STAFF トークンのみ許可
        if (!"STAFF".equals(userType)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        // システム管理者の場合はsalonIdチェックをスキップ
        if (isSystemAdmin != null && isSystemAdmin) {
            return tokenSalonId; // システム管理者は全店舗アクセス可能
        }

        // TODO [あとで]多店舗対応のため、リクエストの salonId と トークン内 salonId が一致することを保証
        //if (requestedSalonId == null || tokenSalonId == null || !requestedSalonId.equals(tokenSalonId)) {
        //    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        //}

        return tokenSalonId;
    }
}
