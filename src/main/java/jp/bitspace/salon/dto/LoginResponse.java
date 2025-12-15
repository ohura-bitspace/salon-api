package jp.bitspace.salon.dto;

/**
 * 管理側ログイン用レスポンスDTO.
 * <p>
 * ログイン成功時に、JWTトークンと画面表示に必要な最小限の情報を返します。
 */
public record LoginResponse(
        /** 認証用JWTトークン */
        String token,
        /** スタッフ名 */
        String name,
        /** 権限（例: OWNER / STAFF） */
        String role,
        /** 所属サロンID */
        Long salonId
) {}
