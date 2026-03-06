-- 1. サロン（店舗）テーブル
-- 契約プランや店舗情報など、すべての親となるテーブル
CREATE TABLE salons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'サロンID',
    name VARCHAR(255) NOT NULL COMMENT '店舗名',
    plan_type ENUM('FREE', 'MONITOR', 'STANDARD') DEFAULT 'MONITOR' COMMENT '契約プラン',
    is_active BOOLEAN DEFAULT TRUE COMMENT '有効フラグ（料金未納時などにFalse）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='加盟店管理テーブル';

-- 1-1. サロン設定テーブル
-- 営業時間や予約枠の単位など、店舗ごとの運用設定を保持
CREATE TABLE salon_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'サロン設定ID',
    salon_id BIGINT NOT NULL COMMENT 'サロンID',
    opening_time TIME NOT NULL DEFAULT '09:00:00' COMMENT '開店時刻',
    closing_time TIME NOT NULL DEFAULT '21:00:00' COMMENT '閉店時刻',
    regular_holidays CHAR(7) NOT NULL DEFAULT '0000000' COMMENT '曜日フラグ(0=営業,1=休み。月曜始まり)',
    slot_interval INT NOT NULL DEFAULT 30 COMMENT '予約枠の単位(分)',
    preparation_margin_minutes INT NOT NULL DEFAULT 30 COMMENT '準備時間マージン(分)。予約の前後に付加するバッファ',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uq_salon_config (salon_id),
    FOREIGN KEY (salon_id) REFERENCES salons(id)
) COMMENT='サロン営業時間・予約設定';

-- 2. 認証用ユーザーテーブル（システム全体でのユーザー、同じメールで複数店舗を持てる設計）
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'ユーザー名',
    email VARCHAR(255) NOT NULL COMMENT 'ログインID',
    password_hash VARCHAR(255) NOT NULL COMMENT 'パスワード',
    
    is_active BOOLEAN DEFAULT TRUE COMMENT '有効フラグ',
    is_system_admin BOOLEAN DEFAULT FALSE COMMENT 'システム管理者フラグ（全店舗アクセス可能）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uq_email (email)
) COMMENT='管理者ユーザー（認証情報）';

-- 3. スタッフ所属テーブル（旧 staffs の役割を分割）
CREATE TABLE staffs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'ユーザーID',
    salon_id BIGINT NOT NULL COMMENT '所属サロンID',
    role ENUM('ADMIN', 'STAFF') DEFAULT 'STAFF' COMMENT 'その店での役割',
    -- 予約受付対象かどうか（trueならカレンダーに出る）
    is_practitioner BOOLEAN DEFAULT TRUE COMMENT '施術者フラグ',
    is_active BOOLEAN DEFAULT TRUE COMMENT '有効フラグ（この店舗での権限有効化）',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uq_user_salon (user_id, salon_id),
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (salon_id) REFERENCES salons(id)
) COMMENT='店舗所属・権限';

-- 3. 顧客テーブル
-- LINEログインを前提とした顧客情報
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    salon_id BIGINT NOT NULL COMMENT '所属サロンID',
    
    -- LINE連携用カラム
    line_user_id VARCHAR(255) COMMENT 'LINEプラットフォームのユーザーID (U...)',
    line_display_name VARCHAR(255) COMMENT 'LINE上の表示名（初期登録用）',
    line_picture_url TEXT COMMENT 'LINEアイコン画像URL',
    
    -- サロン管理用の顧客情報
    last_name VARCHAR(50) COMMENT '姓',
    first_name VARCHAR(50) COMMENT '名',
    last_name_kana VARCHAR(50) COMMENT 'セイ',
    first_name_kana VARCHAR(50) COMMENT 'メイ',
    phone_number VARCHAR(20) COMMENT '電話番号',
    email VARCHAR(255) COMMENT 'メールアドレス（任意）',
    birthday DATE COMMENT '誕生日',
    
    -- 管理用メモ・タグなど
    admin_memo TEXT COMMENT 'スタッフ共有メモ',
    
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '論理削除フラグ',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (salon_id) REFERENCES salons(id),
    
    -- 重要: 1つのサロン内で、LINE IDは一意である必要がある
    -- (※NULLを許容する場合、MySQLのバージョンによってはUNIQUE制約の挙動に注意)
    UNIQUE KEY uq_salon_line (salon_id, line_user_id)
) COMMENT='顧客情報';

-- 4-1. メニューカテゴリ（UI上の「タブ」や「ブロック」に相当）
CREATE TABLE menu_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    salon_id BIGINT NOT NULL COMMENT '所属サロンID',
    
    name VARCHAR(50) NOT NULL COMMENT 'カテゴリ名（例: 脱毛、フェイシャル、回数券）',
    display_order INT DEFAULT 0 COMMENT '表示順序',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (salon_id) REFERENCES salons(id)
) COMMENT='メニューカテゴリマスタ';

-- 4-1.5. メニューセクション（カテゴリ内の小分類）
CREATE TABLE menu_sections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    menu_category_id BIGINT NOT NULL COMMENT '親カテゴリID',
    name VARCHAR(100) NOT NULL COMMENT 'セクション名',
    display_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (menu_category_id) REFERENCES menu_categories(id)
) COMMENT='メニューセクションマスタ';

-- 4-2. メニューマスタ（クーポン・通常メニュー兼用）
CREATE TABLE menus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    salon_id BIGINT NOT NULL COMMENT '所属サロンID',
    menu_category_id BIGINT DEFAULT NULL COMMENT '所属カテゴリID',
    menu_section_id BIGINT DEFAULT NULL COMMENT '所属セクションID',
    
    title VARCHAR(255) NOT NULL COMMENT 'メニュー名',
    description TEXT COMMENT '説明文',
    
    image_url VARCHAR(255) DEFAULT NULL,
    
    original_price INT NOT NULL DEFAULT 0,
    discounted_price INT DEFAULT NULL,
    
    duration_minutes INT NOT NULL DEFAULT 60,
    
    item_type ENUM('COUPON', 'MENU', 'OPTION') NOT NULL DEFAULT 'MENU',
    
    tag VARCHAR(50) COMMENT 'バッジ表示用（例: 人気No.1、新規のみ）',
    
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (salon_id) REFERENCES salons(id),
    FOREIGN KEY (menu_category_id) REFERENCES menu_categories(id) ON DELETE SET NULL,
    FOREIGN KEY (menu_section_id) REFERENCES menu_sections(id) ON DELETE SET NULL
) COMMENT='メニュー・クーポンマスタ';

-- 4-3. スタッフ提供可能メニュー（中間テーブル）
CREATE TABLE staff_available_menus (
    staff_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,

    PRIMARY KEY (staff_id, menu_id),
    FOREIGN KEY (staff_id) REFERENCES staffs(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES menus(id) ON DELETE CASCADE
) COMMENT='スタッフ提供可能メニュー';

-- 5. 予約テーブル（ヘッダー情報）
-- 誰が、いつ、どのスタッフで予約したかの基本情報
CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    salon_id BIGINT NOT NULL COMMENT '所属サロンID',
    customer_id BIGINT DEFAULT NULL COMMENT '予約した顧客（NULL時は枠確保）',
    staff_id BIGINT DEFAULT NULL COMMENT '担当スタッフ（NULLなら指名なし）',
    
    -- 日時管理
    start_time DATETIME NOT NULL COMMENT '開始日時',
    end_time DATETIME NOT NULL COMMENT '終了日時',
    
    -- 予約状態
    -- PENDING: 仮予約, CONFIRMED: 確定, VISITED: 来店済, CANCELED: キャンセル
    status ENUM('PENDING', 'CONFIRMED', 'VISITED', 'CANCELED') DEFAULT 'PENDING' COMMENT 'ステータス',
    
    -- 金額（予約時点での確定金額を保存しておく）
    total_price INT NOT NULL DEFAULT 0 COMMENT '合計金額',
    
    -- 拡張性を持たせるため ENUM
    booking_route ENUM('HP', 'PHONE', 'LINE', 'STORE', 'OTHER') DEFAULT NULL COMMENT '予約経路',
    
    -- メモ項目
    memo TEXT COMMENT 'お客様側メモ（要望・伝達事項など）',
    staff_memo TEXT COMMENT '店側事前メモ（スタッフ間の共有事項・注意点など）',
    treatment_memo TEXT COMMENT '施術メモ（来店後の施術内容や顧客反応の記録）',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (salon_id) REFERENCES salons(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (staff_id) REFERENCES staffs(id)
) COMMENT='予約情報';

-- 6. 予約明細テーブル
-- メニューが決まっていない状態でも明細枠を作れるよう、menu_id を NULL 許容に変更
CREATE TABLE reservation_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL COMMENT '親となる予約ID',
    menu_id BIGINT DEFAULT NULL COMMENT '選択されたメニューID（NULL許容）',
    
    -- 価格スナップショット
    -- メニュー未定の場合は 0 を想定
    price_at_booking INT NOT NULL DEFAULT 0 COMMENT '予約時の適用単価',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES menus(id)
) COMMENT='予約明細';

-- 7. 決済テーブル
-- 実際の売上（キャッシュフロー）を管理
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    salon_id BIGINT NOT NULL COMMENT '所属サロンID',
    reservation_id BIGINT DEFAULT NULL COMMENT '関連する予約ID（NULLなら予約外売上）',
    customer_id BIGINT DEFAULT NULL COMMENT '顧客ID',

    original_amount INT NOT NULL COMMENT '元金額（割引前）',
    discount_amount INT NOT NULL DEFAULT 0 COMMENT '通常割引額',
    point_discount_amount INT NOT NULL DEFAULT 0 COMMENT 'ポイント割引額',
    -- amount = original_amount - discount_amount - point_discount_amount
    amount INT NOT NULL COMMENT '実際の決済金額',

    payment_method ENUM('CASH', 'CARD', 'OTHER') NOT NULL COMMENT '決済方法',
    received_amount INT DEFAULT NULL COMMENT 'お預かり金額（現金払い時のみ、おつり履歴用）',

    payment_source ENUM('SQUARE', 'MANUAL') DEFAULT 'MANUAL' COMMENT 'データソース',
    external_transaction_id VARCHAR(255) DEFAULT NULL COMMENT 'Square等の外部決済ID',

    payment_at DATETIME NOT NULL COMMENT '決済日時',
    memo TEXT COMMENT '会計に関するメモ',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (salon_id) REFERENCES salons(id),
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE SET NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
) COMMENT='決済・売上詳細データ';

-- 8. 施術写真テーブル
-- 1件の来店(reservation)に対して複数枚の写真を保持する
CREATE TABLE visit_photos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '写真ID',
    reservation_id BIGINT NOT NULL COMMENT '予約ID（来店履歴ID）',
    image_url VARCHAR(500) NOT NULL COMMENT '画像ファイルのパス',
    display_order INT NOT NULL DEFAULT 0 COMMENT '表示順序（昇順）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    INDEX idx_visit_photos_reservation (reservation_id)
) COMMENT='施術写真';

-- 9. メッセージテーブル
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    salon_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    
    -- 1. 送信種別（ENUMの方が整合性が保てます）
    sender_type ENUM('LINE_USER', 'ADMIN') NOT NULL COMMENT '送信者種別',
    
    -- 2. メッセージ種別（画像やスタンプへの拡張用）
    message_type ENUM('TEXT', 'IMAGE', 'STAMP', 'OTHER') DEFAULT 'TEXT' NOT NULL,
    
    text TEXT DEFAULT NULL COMMENT 'メッセージ本文（IMAGE/STAMPの場合はNULL可）',
    
    -- 3. LINE側のメッセージID（Webhookの重複検知・既読管理用）
    line_message_id VARCHAR(255) DEFAULT NULL,
    
    -- 4. 既読管理（バッジ表示に必須！）
    is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '管理者側の既読フラグ',
    read_at DATETIME DEFAULT NULL,
    
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (salon_id) REFERENCES salons(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    INDEX idx_messages_salon_customer (salon_id, customer_id),
    INDEX idx_messages_unread_count (salon_id, is_read) COMMENT '未読バッジ集計用',
    INDEX idx_messages_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LINEメッセージ履歴';
