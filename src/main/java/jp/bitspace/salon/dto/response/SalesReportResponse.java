package jp.bitspace.salon.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * 売上レポートレスポンスDTO.
 * <p>
 * 画面上部のKPIカード（summary）と顧客別明細（items）を返します。
 */
public record SalesReportResponse(
        SalesReportSummary summary,
        List<SalesReportItem> items) {

    /**
     * KPIサマリー.
     */
    public record SalesReportSummary(
            /** 実績合計金額（来店済み） */
            int actualAmount,
            /** 実績件数 */
            int actualCount,
            /** 予定合計金額（予約済み） */
            int scheduledAmount,
            /** 予定件数 */
            int scheduledCount,
            /** 見込み合計金額（実績＋予定） */
            int totalAmount) {
    }

    /**
     * 明細行.
     */
    public record SalesReportItem(
            /** 予約ID */
            Long reservationId,
            /** 来店日 */
            LocalDate date,
            /** 顧客名 */
            String customerName,
            /** メニュー名（複数の場合は読点区切り） */
            String menu,
            /** 担当スタッフ名 */
            String staff,
            /** 決済方法（実績のみ。予定はnull） */
            String paymentMethod,
            /** ステータス: "done"=実績 / "scheduled"=予定 */
            String status,
            /** 金額（円） */
            int amount) {
    }
}
