package jp.bitspace.salon.controller.dto;

import java.time.LocalDateTime;
import java.util.List;

import jp.bitspace.salon.model.ReservationStatus;

/**
 * 新規予約作成APIのリクエストボディを表すレコード。
 * <p>
 * 予約のヘッダー情報（日時・顧客・スタッフ）と、
 * 選択されたメニュー明細リストを集約してサーバーへ送信するために使用します。
 * </p>
 *
 * @param salonId    予約対象のサロンID（マルチテナント識別用）
 * @param customerId 予約を行う顧客のID
 * @param staffId    担当スタッフのID。<br>
 * ※指名なし（フリー）の場合は {@code null} を許容します。
 * @param startAt    施術開始日時
 * @param endAt      施術終了日時
 * @param status     初期ステータス（通常は {@code PENDING} または {@code CONFIRMED}）
 * @param memo       店舗側管理用メモ、または顧客からの申し送り事項
 * @param items      選択されたメニュー（明細）のリスト。1件以上必須。
 */
public record CreateReservationRequest(
    Long salonId,
    Long customerId,
    Long staffId,
    LocalDateTime startAt,
    LocalDateTime endAt,
    ReservationStatus status,
    String memo,
    List<CreateReservationItemRequest> items
) {}
