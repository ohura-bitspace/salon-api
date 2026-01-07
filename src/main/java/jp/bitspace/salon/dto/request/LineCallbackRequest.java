package jp.bitspace.salon.dto.request;

import lombok.Data;

/**
 * LINE認証コールバックリクエスト（スタブ）.
 */
@Data
public class LineCallbackRequest {
    /** 認可コード */
    private String code;

    /** CSRF対策用のstate */
    private String state;

    /** 所属サロンID */
    private Long salonId;
    
    /** リプレイ攻撃対策用のnonce */
    private String nonce;
    
    /** リダイレクトURI */
    private String redirectUri;
}
