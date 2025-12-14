package jp.bitspace.salon.controller.dto;

/**
 * 管理側ログイン用リクエストDTO.
 * <p>
 * メールアドレスとパスワードで認証を行うための入力です。
 */
public record LoginRequest(
        /** ログインID（メールアドレス） */
        String email,
        /** 平文パスワード */
        String password
) {}
