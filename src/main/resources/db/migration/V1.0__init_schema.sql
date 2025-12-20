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

-- 2. 認証用ユーザーテーブル（システム全体でのユーザー、同じメールで複数店舗を持てる設計）
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'ユーザー名',
    email VARCHAR(255) NOT NULL COMMENT 'ログインID',
    password_hash VARCHAR(255) NOT NULL COMMENT 'パスワード',
    
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uq_email (email)
) COMMENT='管理者ユーザー（認証情報）';

-- 3. スタッフ所属テーブル（旧 staffs の役割を分割）
CREATE TABLE staffs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'ユーザーID',
    salon_id BIGINT NOT NULL COMMENT '所属サロンID',
    role ENUM('OWNER', 'STAFF') DEFAULT 'STAFF' COMMENT 'その店での役割',
    
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

-- 4-2. メニューマスタ（クーポン・通常メニュー兼用）
CREATE TABLE menus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    salon_id BIGINT NOT NULL COMMENT '所属サロンID',
    menu_category_id BIGINT DEFAULT NULL COMMENT '所属カテゴリID',
    
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
    FOREIGN KEY (menu_category_id) REFERENCES menu_categories(id) ON DELETE SET NULL
) COMMENT='メニュー・クーポンマスタ';

-- 5. 予約テーブル（ヘッダー情報）
-- 誰が、いつ、どのスタッフで予約したかの基本情報
CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    salon_id BIGINT NOT NULL COMMENT '所属サロンID',
    customer_id BIGINT NOT NULL COMMENT '予約した顧客',
    staff_id BIGINT DEFAULT NULL COMMENT '担当スタッフ（NULLなら指名なし）',
    
    -- 日時管理
    start_time DATETIME NOT NULL COMMENT '開始日時',
    end_time DATETIME NOT NULL COMMENT '終了日時',
    
    -- 予約状態
    -- PENDING: 仮予約, CONFIRMED: 確定, VISITED: 来店済, CANCELED: キャンセル
    status ENUM('PENDING', 'CONFIRMED', 'VISITED', 'CANCELED') DEFAULT 'PENDING' COMMENT 'ステータス',
    
    -- 金額（予約時点での確定金額を保存しておく）
    total_price INT NOT NULL DEFAULT 0 COMMENT '合計金額',
    
    -- 管理用
    memo TEXT COMMENT '店舗側メモ（申し送り事項など）',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (salon_id) REFERENCES salons(id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (staff_id) REFERENCES staffs(id)
) COMMENT='予約情報';

-- 6. 予約明細テーブル（リレーション）
-- 1つの予約の中に「クーポンA」と「オプションB」が含まれる、といった多対多を管理
CREATE TABLE reservation_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL COMMENT '親となる予約ID',
    menu_id BIGINT NOT NULL COMMENT '選択されたメニューID',
    
    -- 価格スナップショット
    -- メニューの価格が後で変わっても、予約時の価格を保持するためにここに値をコピーする
    price_at_booking INT NOT NULL COMMENT '予約時の適用単価',
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES menus(id)
) COMMENT='予約明細';
