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

-- 2. スタッフ（管理画面用ユーザー）テーブル
-- オーナーや従業員がログインするためのテーブル
CREATE TABLE staffs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    salon_id BIGINT NOT NULL COMMENT '所属サロンID',
    name VARCHAR(100) NOT NULL COMMENT 'スタッフ名',
    email VARCHAR(255) NOT NULL COMMENT 'ログインID兼メールアドレス',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt等でハッシュ化したパスワード',
    role ENUM('OWNER', 'STAFF') DEFAULT 'STAFF' COMMENT '権限',
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 制約: 同じサロン内で同じメールアドレスは登録不可（またはシステム全体でユニークでも可）
    UNIQUE KEY uq_email (email),
    FOREIGN KEY (salon_id) REFERENCES salons(id)
) COMMENT='管理画面ログインユーザー';

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
