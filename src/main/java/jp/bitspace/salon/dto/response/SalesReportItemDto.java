package jp.bitspace.salon.dto.response;

import java.time.LocalDate;

/**
 * 売上レポート明細DTO（フラット配列形式）.
 * <p>
 * GET /api/admin/reports/sales のレスポンス要素として使用します。
 */
public record SalesReportItemDto(
        /** 予約ID */
        Long id,
        /** ステータス: "done"=実績 / "scheduled"=予定 */
        String status,
        /** 来店日（例: 2025-11-01） */
        LocalDate date,
        /** 開始時刻（例: 10:00） */
        String time,
        /** 顧客名 */
        String customerName,
        /** メニュー名 */
        String menuName,
        /** カテゴリID */
        Long categoryId,
        /** カテゴリ名 */
        String categoryName,
        /** 担当スタッフ名 */
        String staffName,
        /** 決済方法（実績のみ。予定はnull） */
        String paymentMethod,
        /** 金額（円） */
        int amount) {
}
