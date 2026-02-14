package jp.bitspace.salon.dto.response;

import java.util.List;

/**
 * カルテ詳細取得レスポンスDTO.
 * <p>
 * ※中身の組み立ては今後の実装で追加します。
 */
public record CustomerDetailResponse(
		// TODO LINE表示名も
        Long customerId,
        String name,
        String nameKana,
        String phoneNumber,
        String email,
        java.time.LocalDate birthday,
        String memo,
        List<VisitHistoryDto> history,
        /** 来店回数. */
        int visitCount,
        Long totalSales,
        // 以下、顧客パーソナル情報で使用する
        String lastName,
        String firstName,
        String lastNameKana,
        String firstNameKana,
        String linePictureUrl
) {}
