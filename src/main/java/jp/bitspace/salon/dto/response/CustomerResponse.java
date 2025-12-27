package jp.bitspace.salon.dto.response;

/**
 * 顧客リスト表示用レスポンスDTO.
 * <p>
 * 選択リストなどで必要な最小限の情報のみを含みます。
 */
public record CustomerResponse(
    Long id,
    String name,
    String nameKana,
    /** 直近来店日. */
    String lastVisit
) {}
