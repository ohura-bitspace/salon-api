-- ============================================================
-- 1. サロン（店舗）データの投入
-- ============================================================
-- ID=1: メインの開発用サロン（スタンダードプラン）
-- ID=2: マルチテナント検証用の別サロン（フリープラン）
INSERT INTO salons (id, name, plan_type, is_active) VALUES 
(1, 'Beauty Salon Carnet', 'STANDARD', true),
(2, 'Hair Studio Mock', 'FREE', true);

-- ============================================================
-- 2. スタッフデータの投入
-- ============================================================
-- パスワードは全て 'password' をBCryptでハッシュ化したものです

INSERT INTO staffs (salon_id, name, email, password_hash, role) VALUES 
-- サロン1 (Carnet) のオーナー
(1, '山田 太郎', 'owner@carnet.jp', '$2a$10$4hInjKtm/Kd.eZf24UBOFOAmgXFyf/ESW0KkpWfQIPhGyn.KExCtK', 'OWNER'),
-- サロン1 (Carnet) のスタッフ
(1, '鈴木 花子', 'staff@carnet.jp', '$2a$10$4hInjKtm/Kd.eZf24UBOFOAmgXFyf/ESW0KkpWfQIPhGyn.KExCtK', 'STAFF'),
-- サロン2 (Mock) のオーナー（データ混在チェック用）
(2, '田中 次郎', 'owner@mock.jp', '$2a$10$4hInjKtm/Kd.eZf24UBOFOAmgXFyf/ESW0KkpWfQIPhGyn.KExCtK', 'OWNER');

-- ============================================================
-- 3. 顧客データの投入
-- ============================================================

INSERT INTO customers 
(salon_id, line_user_id, line_display_name, last_name, first_name, last_name_kana, first_name_kana, phone_number, email, admin_memo) 
VALUES 
-- パターンA: LINE連携済み & 顧客情報登録済みの優良顧客 (サロン1)
(1, 'U11111111111111111111111111111111', 'Hanako.S', '佐藤', '花子', 'サトウ', 'ハナコ', '090-1111-2222', 'hanako@example.com', '肌が敏感なので注意。コーヒーが好き。'),

-- パターンB: LINE連携のみ（初回来店前など、詳細情報未登録） (サロン1)
(1, 'U22222222222222222222222222222222', 'Mike_Line', NULL, NULL, NULL, NULL, NULL, NULL, 'LINEから問い合わせあり。'),

-- パターンC: 電話予約のみ（LINE未連携）のアナログ顧客 (サロン1)
(1, NULL, NULL, '鈴木', '一郎', 'スズキ', 'イチロウ', '080-3333-4444', NULL, '電話予約メイン。ガラケー使用。'),

-- パターンD: 別サロンの顧客（サロン1の管理画面に出ないことを確認用） (サロン2)
(2, 'U33333333333333333333333333333333', 'Jiro.T', '田中', '三郎', 'タナカ', 'サブロウ', '090-5555-6666', 'jiro@mock.jp', '別店舗の客');