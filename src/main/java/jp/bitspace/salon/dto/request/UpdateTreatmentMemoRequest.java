package jp.bitspace.salon.dto.request;

/**
 * 施術メモ更新リクエスト.
 */
public record UpdateTreatmentMemoRequest(
        /** 施術メモ */
        String treatmentMemo
) {}
