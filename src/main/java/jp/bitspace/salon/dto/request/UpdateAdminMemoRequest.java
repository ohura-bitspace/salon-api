package jp.bitspace.salon.dto.request;

/**
 * 顧客管理メモ更新リクエスト.
 */
public record UpdateAdminMemoRequest(
        /** 管理メモ */
        String adminMemo
) {}
