package jp.bitspace.salon.service;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        String normalized = normalizeBody(body);

        // 予約番号
        String reservationId = extractValueByLabel(normalized, "予約番号", "■予約番号");

        // 氏名・カナ（取れない場合は null のまま）
        NameParts nameParts = extractNameParts(normalized);

        // 来店日時（開始）+ 所要時間（終了）
        LocalDateTime startTime = extractStartDateTime(normalized);
        Integer durationMinutes = extractDurationMinutes(normalized);
        LocalDateTime endTime = (startTime != null && durationMinutes != null)
                ? startTime.plusMinutes(durationMinutes)
                : null;

        // 指名スタッフ
        String staffName = extractValueByLabel(normalized, "指名スタッフ", "■指名スタッフ");
        if (staffName != null && isNoStaff(staffName)) {
            staffName = null;
        }

        // メニュー名（「■メニュー」の次行/同一行どちらでも拾う）
        String menuName = extractValueByLabel(normalized, "メニュー", "■メニュー");

        // ご要望・ご相談
        String memo = extractValueByLabel(normalized, "ご要望・ご相談", "■ご要望・ご相談");
        if (memo != null && memo.trim().equals("-")) {
            memo = null;
        }
        
        
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
        return subject.contains("HOT PEPPER")
                || subject.contains("ホットペッパー")
                || subject.toLowerCase().contains("hotpepper")
                || subject.contains("SALON BOARD");
    }
    
 // =========================================================
    // Private helpers (robust extraction)
    // =========================================================

    private static String pickBody(MailgunWebhookRequest request) {
        String body = request.getBodyPlain();
        if (body == null || body.isBlank()) {
            body = request.getStrippedText();
        }
        return body != null ? body : "";
    }

    /**
     * 本文を抽出しやすい形に正規化する（軽量に）。
     * - HTMLタグをざっくり除去
     * - 全角→半角（英数記号の一部）+ 全角スペース→半角
     * - 改行/空白の揺れ吸収
     */
    private static String normalizeBody(String s) {
        if (s == null) return "";

        // HTMLっぽさ除去（Mailgunがplainをくれないケースの保険）
        s = s.replaceAll("(?is)<br\\s*/?>", "\n");
        s = s.replaceAll("(?is)</p\\s*>", "\n");
        s = s.replaceAll("(?is)<[^>]+>", "");

        // 全角→半角（！～）+ 全角スペース
        s = toHalfWidth(s);

        // 改行統一
        s = s.replace("\r\n", "\n").replace("\r", "\n");

        // 連続空白圧縮（全角スペースは toHalfWidth で半角に済）
        s = s.replaceAll("[ \\t]+", " ");

        // 行頭行末の空白除去（行単位）
        s = s.replaceAll("(?m)^\\s+|\\s+$", "");

        // 過剰な空行を減らす
        s = s.replaceAll("\\n{3,}", "\n\n");

        return s.trim();
    }

    private static String toHalfWidth(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // 全角英数記号(！～) → 半角
            if (c >= '！' && c <= '～') {
                sb.append((char) (c - 0xFEE0));
            } else if (c == '　') {
                sb.append(' ');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * ラベルの値を抽出（揺れ耐性用）
     * - "■予約番号" のような行ラベルにも対応
     * - "予約番号："のように同一行に値がある場合も対応
     * - 見つからなければ null
     */
    private static String extractValueByLabel(String body, String... labelCandidates) {
        if (body == null || body.isBlank()) return null;

        for (String label : labelCandidates) {
            String v = extractValueByLabelOnce(body, label);
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }

    private static String extractValueByLabelOnce(String body, String label) {
        // 1) 同一行パターン: "予約番号: BE123" / "予約番号：BE123"
        Pattern sameLine = Pattern.compile("(?m)" + Pattern.quote(label) + "\\s*[:：]?\\s*(.+)$");
        Matcher m1 = sameLine.matcher(body);
        if (m1.find()) {
            String value = cleanupValue(m1.group(1));
            if (value != null && !value.isBlank() && !looksLikeJustLabel(value)) {
                return value;
            }
        }

        // 2) 次行パターン: "■予約番号\n　BE123"
        int idx = body.indexOf(label);
        if (idx < 0) return null;

        int nl = body.indexOf('\n', idx);
        if (nl < 0) return null;

        int valueStart = nl + 1;
        // 次の空行/次のラベルっぽい行（■ or □ or ◆）まで
        int valueEnd = findValueEnd(body, valueStart);
        String block = body.substring(valueStart, valueEnd).trim();

        // 先頭行を基本として返す（メニュー等は複数行になり得るので最初の非空行）
        String firstLine = firstNonBlankLine(block);
        return cleanupValue(firstLine);
    }

    private static int findValueEnd(String body, int from) {
        int len = body.length();
        int end = len;

        // 次のラベル開始っぽい行（■/□/◆/◇/▽/▼）を探す
        Pattern nextLabel = Pattern.compile("(?m)^\\s*[■□◆◇▽▼]\\s*");
        Matcher m = nextLabel.matcher(body);
        if (m.find(from)) {
            end = m.start();
        }

        // ただし、見つからない場合は本文末まで
        return end;
    }

    private static String firstNonBlankLine(String s) {
        if (s == null) return null;
        String[] lines = s.split("\\n");
        for (String line : lines) {
            if (line != null && !line.trim().isBlank()) {
                return line.trim();
            }
        }
        return s.trim();
    }

    private static String cleanupValue(String v) {
        if (v == null) return null;
        // 先頭の記号や余計な空白を除去
        v = v.replaceAll("^[・\\-—–:：\\s]+", "");
        v = v.trim();
        return v.isBlank() ? null : v;
    }

    private static boolean looksLikeJustLabel(String v) {
        // 値が空っぽ/短すぎ/ラベルっぽい場合の保険（必要に応じて調整）
        String t = v.trim();
        return t.isEmpty() || t.equals("■") || t.equals("◇");
    }

    private static boolean isNoStaff(String staff) {
        String s = staff.trim();
        return s.equals("指名なし") || s.equals("なし") || s.equals("未指定");
    }

    // -----------------------------
    // Name extraction
    // -----------------------------

    private static final String SPACES = "[\\u3000\\s]";
    // 例: 鯵坂 里保（アジサカ リホ）
    private static final Pattern NAME_WITH_KANA = Pattern.compile(
            "([^\\s\\u3000]+)" + SPACES + "+([^（(]+)\\s*[（(]" +
            "([^\\s\\u3000]+)" + SPACES + "+([^）)]+)[）)]"
    );

    private static NameParts extractNameParts(String body) {
        String nameLine = extractValueByLabel(body, "氏名", "■氏名", "お名前", "名前");
        if (nameLine == null) return null;

        Matcher m = NAME_WITH_KANA.matcher(nameLine);
        if (m.find()) {
            return new NameParts(
                    m.group(1).trim(),
                    m.group(2).trim(),
                    m.group(3).trim(),
                    m.group(4).trim()
            );
        }

        // フォーマットが変わってカナが無い/括弧が無い場合：スペース区切りの姓・名だけ拾う
        String[] parts = nameLine.trim().split("\\s+");
        if (parts.length >= 2) {
            return new NameParts(parts[0], parts[1], null, null);
        }

        log.warn("氏名のパースに失敗: {}", nameLine);
        return null;
    }

    private static final class NameParts {
        final String lastName;
        final String firstName;
        final String lastNameKana;
        final String firstNameKana;

        NameParts(String lastName, String firstName, String lastNameKana, String firstNameKana) {
            this.lastName = lastName;
            this.firstName = firstName;
            this.lastNameKana = lastNameKana;
            this.firstNameKana = firstNameKana;
        }
    }

    // -----------------------------
    // Start datetime extraction
    // -----------------------------

    // ラベル候補（完全一致に依存しない）
    private static final Pattern LABEL_VISIT = Pattern.compile("(来店日時|予約日時|開始日時)\\s*[:：]?");

    // 例: 2026年01月11日（日）11:15 / 2026年1月11日 11:15
    private static final Pattern DT_JP = Pattern.compile(
            "(?<y>\\d{4})\\s*年\\s*(?<m>\\d{1,2})\\s*月\\s*(?<d>\\d{1,2})\\s*日" +
            "\\s*(?:\\((?:.|..)\\)|（(?:.|..)）)?\\s*" +
            "(?<hh>\\d{1,2})\\s*[:：]\\s*(?<mm>\\d{2})"
    );

    // 例: 2026/01/11 11:15, 2026-01-11 11:15
    private static final Pattern DT_SLASH = Pattern.compile(
            "(?<y>\\d{4})\\s*[/\\-]\\s*(?<m>\\d{1,2})\\s*[/\\-]\\s*(?<d>\\d{1,2})\\s+" +
            "(?<hh>\\d{1,2})\\s*[:：]\\s*(?<mm>\\d{2})"
    );

    private static LocalDateTime extractStartDateTime(String body) {
        // 1) ラベル近傍を優先
        String near = extractNearLabel(body, LABEL_VISIT, 0, 220);
        LocalDateTime dt = parseDateTimeFrom(near);
        if (dt != null) return dt;

        // 2) 全文走査（保険）
        dt = parseDateTimeFrom(body);
        if (dt == null) {
            String dateLine = extractValueByLabel(body, "来店日時", "■来店日時");
            log.warn("来店日時のパースに失敗: {}", dateLine);
        }
        return dt;
    }

    private static String extractNearLabel(String body, Pattern labelPattern, int before, int after) {
        if (body == null) return "";
        Matcher m = labelPattern.matcher(body);
        if (!m.find()) return body;
        int start = Math.max(0, m.start() - before);
        int end = Math.min(body.length(), m.end() + after);
        return body.substring(start, end);
    }

    private static LocalDateTime parseDateTimeFrom(String s) {
        LocalDateTime dt = tryParseWithPattern(s, DT_JP);
        if (dt != null) return dt;
        return tryParseWithPattern(s, DT_SLASH);
    }

    private static LocalDateTime tryParseWithPattern(String s, Pattern p) {
        if (s == null) return null;
        Matcher m = p.matcher(s);
        if (!m.find()) return null;

        int y = Integer.parseInt(m.group("y"));
        int mo = Integer.parseInt(m.group("m"));
        int d = Integer.parseInt(m.group("d"));
        int hh = Integer.parseInt(m.group("hh"));
        int mm = Integer.parseInt(m.group("mm"));
        return LocalDateTime.of(y, mo, d, hh, mm);
    }
    
    
 // -----------------------------
    // Duration extraction
    // -----------------------------

    // 例: 所要時間目安:40分 / 所要時間: 90min / 施術時間 60 分
    private static final Pattern DUR_MIN = Pattern.compile(
            "(所要時間|施術時間|目安)\\s*(?:[\\w\\-\\s]*?)\\s*[:：]?\\s*(?<min>\\d{1,3})\\s*(分|min)",
            Pattern.CASE_INSENSITIVE
    );

    // 例: 1時間30分 / 2時間 / 1時間
    private static final Pattern DUR_HM = Pattern.compile(
            "(?<h>\\d{1,2})\\s*時間\\s*(?:(?<m>\\d{1,2})\\s*分)?"
    );

    private static Integer extractDurationMinutes(String body) {
        if (body == null) return null;

        // 1) 「所要時間/施術時間/目安」系を優先
        Matcher m1 = DUR_MIN.matcher(body);
        if (m1.find()) {
            try {
                int min = Integer.parseInt(m1.group("min"));
                return isReasonableDuration(min) ? min : null;
            } catch (NumberFormatException ignore) {
                return null;
            }
        }

        // 2) 「1時間30分」形式
        Matcher m2 = DUR_HM.matcher(body);
        if (m2.find()) {
            try {
                int h = Integer.parseInt(m2.group("h"));
                int min = (m2.group("m") != null) ? Integer.parseInt(m2.group("m")) : 0;
                int total = h * 60 + min;
                return isReasonableDuration(total) ? total : null;
            } catch (NumberFormatException ignore) {
                return null;
            }
        }

        return null;
    }

    private static boolean isReasonableDuration(int minutes) {
        // サロン施術の現実的レンジ（適宜調整）
        return minutes >= 5 && minutes <= 8 * 60;
    }
}
