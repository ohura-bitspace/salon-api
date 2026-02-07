package jp.bitspace.salon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jp.bitspace.salon.dto.request.HotpepperMailContent;
import jp.bitspace.salon.dto.request.MailgunWebhookRequest;

/**
 * ホットペッパー予約通知メールの解析サービス.
 * <p>
 * メール本文（プレーンテキスト or HTML）をパースし、
 * 予約情報を {@link HotpepperMailContent} に抽出する。
 * </p>
 */
@Service
public class HotpepperMailParserService {

    private static final Logger log = LoggerFactory.getLogger(HotpepperMailParserService.class);

    /**
     * Mailgun から受信したメール内容をパースし、予約情報を抽出する.
     *
     * @param request Mailgun Webhook リクエスト
     * @return 抽出した予約情報
     */
    public HotpepperMailContent parse(MailgunWebhookRequest request) {
        log.info("ホットペッパーメール解析開始: subject={}", request.getSubject());

        String body = request.getBodyPlain();
        if (body == null || body.isBlank()) {
            body = request.getStrippedText();
        }

        // TODO: 実際のホットペッパー予約通知メールのフォーマットに合わせて解析を実装する
        //
        // ホットペッパー予約メールの典型的な構造:
        // - 件名: 「【ホットペッパービューティー】予約が入りました」など
        // - 本文に含まれる情報:
        //   - 顧客名
        //   - 予約日時
        //   - メニュー名
        //   - 指名スタッフ
        //   - 電話番号
        //   - 予約番号
        //   - その他要望・メモ
        //
        // 解析例:
        // String customerName = extractByRegex(body, "お名前[：:]\\s*(.+)");
        // String dateTimeStr  = extractByRegex(body, "予約日時[：:]\\s*(.+)");
        // ...

        HotpepperMailContent content = HotpepperMailContent.builder()
                .customerLastName(null)   // TODO: メール本文から抽出
                .customerFirstName(null)  // TODO: メール本文から抽出
                .customerLastNameKana(null)
                .customerFirstNameKana(null)
                .phoneNumber(null)        // TODO: メール本文から抽出
                .email(null)              // TODO: メール本文から抽出
                .startTime(null)          // TODO: メール本文から抽出
                .endTime(null)            // TODO: メール本文から抽出
                .menuName(null)           // TODO: メール本文から抽出
                .staffName(null)          // TODO: メール本文から抽出
                .memo(null)               // TODO: メール本文から抽出
                .hotpepperReservationId(null) // TODO: メール本文から抽出
                .build();

        log.info("ホットペッパーメール解析完了: reservationId={}", content.getHotpepperReservationId());
        return content;
    }

    /**
     * メール件名がホットペッパーの予約通知メールかどうかを判定する.
     *
     * @param subject メール件名
     * @return ホットペッパー予約通知の場合 true
     */
    public boolean isHotpepperReservationMail(String subject) {
        if (subject == null) {
            return false;
        }
        // TODO: 実際の件名パターンに合わせて調整
        return subject.contains("ホットペッパー") || subject.contains("hotpepper");
    }
}
