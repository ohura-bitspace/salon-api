package jp.bitspace.salon.dto.response;

/**
 * 顧客リスト表示用レスポンスDTO.
 * <p>
 * 選択リストなどで必要な最小限の情報のみを含みます。
 */
public record CustomerResponse(
    Long id,
    /** 直近来店日. */
    String lastVisit,
    String lineDisplayName,
    String lastName,
    String firstName
) {}
