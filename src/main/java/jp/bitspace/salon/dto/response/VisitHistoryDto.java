package jp.bitspace.salon.dto.response;

import java.time.LocalDate;

/**
 * カルテ画面で表示する来店履歴DTO.
 */
public record VisitHistoryDto(
        /** 来店履歴のID（予約IDやレシートIDでも可） */
        Long visitId,
        /** 来店日 (YYYY-MM-DD) */
        LocalDate date,
        /** 施術メニュー名 */
        String menu,
        /** 担当スタッフ名 */
        String staff,
        /** 施術料金（単位: 円） */
        Long price,
        /** 施術メモ（編集可能） */
        String treatmentMemo
) {}
