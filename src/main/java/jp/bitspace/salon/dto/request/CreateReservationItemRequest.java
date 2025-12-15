package jp.bitspace.salon.dto.request;

/**
 * 予約作成リクエストにおける「明細（メニュー選択）」情報を表すレコード。
 * <p>
 * どのメニューを選んだか、および予約時点での適用価格を保持します。
 * </p>
 *
 * @param menuId         選択されたメニュー（またはクーポン）のID
 * @param priceAtBooking 予約時点での単価（円）。
 * <p>※重要: 将来マスタの価格が変更されても影響を受けないよう、
 * 予約時の価格をここで確定して送信します。</p>
 */
public record CreateReservationItemRequest(
    Long menuId,
    Integer priceAtBooking
) {}
