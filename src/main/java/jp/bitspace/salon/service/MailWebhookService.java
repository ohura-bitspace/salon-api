package jp.bitspace.salon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.bitspace.salon.dto.request.HotpepperMailContent;
import jp.bitspace.salon.dto.request.MailgunWebhookRequest;
import jp.bitspace.salon.dto.response.MailWebhookResponse;
import jp.bitspace.salon.model.BookingRoute;
import jp.bitspace.salon.model.Reservation;
import jp.bitspace.salon.model.ReservationStatus;
import jp.bitspace.salon.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;

/**
 * メール Webhook 処理サービス.
 * <p>
 * Mailgun から受信したメールを解析し、予約テーブルに反映する。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class MailWebhookService {

    private static final Logger log = LoggerFactory.getLogger(MailWebhookService.class);

    private final MailgunSignatureVerifier signatureVerifier;
    private final HotpepperMailParserService hotpepperMailParserService;
    private final ReservationRepository reservationRepository;

    /** 対象のサロン ID. */
    private Long defaultSalonId = 2L;

    /**
     * ホットペッパー予約通知メールを処理する.
     *
     * @param request Mailgun Webhook リクエスト
     * @return 処理結果
     */
    @Transactional
    public MailWebhookResponse processHotpepperMail(MailgunWebhookRequest request) {
        // 1. 署名検証
        if (!signatureVerifier.verify(request.getTimestamp(), request.getToken(), request.getSignature())) {
            log.warn("Mailgun 署名検証失敗。リクエストを拒否します。");
            return MailWebhookResponse.error("署名検証に失敗しました");
        }

        // 2. ホットペッパー予約メールか判定
        if (!hotpepperMailParserService.isHotpepperReservationMail(request.getSubject())) {
            log.info("ホットペッパー予約通知メールではありません: subject={}", request.getSubject());
            return MailWebhookResponse.ok("予約通知メールではないためスキップしました");
        }

        // 3. メール本文を解析
        HotpepperMailContent content = hotpepperMailParserService.parse(request);

        // 4. 予約を作成
        Reservation reservation = createReservationFromMail(content);

        log.info("ホットペッパー予約を登録しました: reservationId={}", reservation.getId());
        return MailWebhookResponse.ok("予約を登録しました", reservation.getId());
    }

    /**
     * 解析したメール内容から予約を作成する.
     *
     * @param content 解析済みメール内容
     * @return 作成された予約エンティティ
     */
    private Reservation createReservationFromMail(HotpepperMailContent content) {
        Reservation reservation = new Reservation();

        reservation.setSalonId(defaultSalonId);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setBookingRoute(BookingRoute.HP);

        // 予約日時
        if (content.getStartTime() != null) {
            reservation.setStartTime(content.getStartTime());
        }
        if (content.getEndTime() != null) {
            reservation.setEndTime(content.getEndTime());
        }

        // メモ（ホットペッパー予約番号＋顧客の要望を格納）
        StringBuilder memo = new StringBuilder();
        if (content.getHotpepperReservationId() != null) {
            memo.append("[HPB予約番号: ").append(content.getHotpepperReservationId()).append("]\n");
        }
        if (content.getCustomerLastName() != null || content.getCustomerFirstName() != null) {
            memo.append("[顧客名: ")
                .append(content.getCustomerLastName() != null ? content.getCustomerLastName() : "")
                .append(" ")
                .append(content.getCustomerFirstName() != null ? content.getCustomerFirstName() : "")
                .append("]\n");
        }
        if (content.getPhoneNumber() != null) {
            memo.append("[電話番号: ").append(content.getPhoneNumber()).append("]\n");
        }
        if (content.getMenuName() != null) {
            memo.append("[メニュー: ").append(content.getMenuName()).append("]\n");
        }
        if (content.getStaffName() != null) {
            memo.append("[指名スタッフ: ").append(content.getStaffName()).append("]\n");
        }
        if (content.getMemo() != null) {
            memo.append("[要望: ").append(content.getMemo()).append("]\n");
        }
        
        // 原文でいいかも
        reservation.setMemo(memo.toString().trim());

        // TODO: 顧客名・電話番号からCustomerを検索 or 新規作成して customer_id を設定
        // TODO: スタッフ名から staff_id を検索して設定
        // TODO: メニュー名から menu を検索し、totalPrice / ReservationItem を設定

        return reservationRepository.save(reservation);
    }
}
