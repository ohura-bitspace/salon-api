package jp.bitspace.salon.dto.request;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * ホットペッパーメールから抽出した予約情報.
 * <p>
 * メール解析サービスで本文をパースし、このDTOに詰め替える。
 * </p>
 */
@Data
@Builder
public class HotpepperMailContent {

    /** 顧客名（姓） */
    private String customerLastName;

    /** 顧客名（名） */
    private String customerFirstName;

    /** 顧客名カナ（姓） */
    private String customerLastNameKana;

    /** 顧客名カナ（名） */
    private String customerFirstNameKana;

    /** 電話番号 */
    private String phoneNumber;

    /** メールアドレス */
    private String email;

    /** 予約開始日時 */
    private LocalDateTime startTime;

    /** 予約終了日時 */
    private LocalDateTime endTime;

    /** メニュー名 */
    private String menuName;

    /** 指名スタッフ名 */
    private String staffName;

    /** 顧客メモ・要望 */
    private String memo;

    /** 予約ID（ホットペッパー側の管理番号） */
    private String hotpepperReservationId;
}
