package jp.bitspace.salon.dto.response;

/**
 * 管理側ログイン用レスポンスDTO.
 * <p>
 * ログイン成功時に、JWTトークンと画面表示に必要な最小限の情報を返します。
 */
public record LoginResponse(
        /** 認証用JWTトークン */
        String token,
        /** ユーザID */
        Long userId,
        /** スタッフID */
        Long staffId,
        /** スタッフ名 */
        String name,
        /** 権限（例: ADMIN / STAFF） */
        String role,
        /** 所属サロンID */
        Long salonId,
        /** 所属サロン名 */
        String salonName,
        /** システム管理者フラグ（全店舗アクセス可能） */
        Boolean isSystemAdmin
) {}
