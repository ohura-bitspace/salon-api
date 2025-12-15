package jp.bitspace.salon.dto;

import java.time.LocalDateTime;

/**
 * 管理側予約情報レスポンスDTO.
 * <p>
 * カレンダー表示や予約詳細画面で必要な情報を返します。
 */
public record AdminReservationResponse(
        /** 予約ID */
        String id,
        /** 予約タイトル */
        String title,
        /** 開始日時 */
        LocalDateTime start,
        /** 終了日時 */
        LocalDateTime end,
        /** 背景色 */
        String backgroundColor,
        /** 枠線色 */
        String borderColor,
        /** 顧客名 */
        String customer,
        /** メニュー名（複数の場合はカンマ区切り） */
        String menu,
        /** スタッフ名 */
        String staff,
        /** ステータス（日本語表示用） */
        String status,
        /** メモ */
        String memo
) {}
