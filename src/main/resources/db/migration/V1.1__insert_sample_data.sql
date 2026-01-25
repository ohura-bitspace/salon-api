-- ============================================================
-- 1. サロン（店舗）データの投入
-- ============================================================
-- ID=1: メインの開発用サロン
-- ID=2: お客様の本番サロン
INSERT INTO salons (id, name, plan_type, is_active) VALUES 
(1, 'bit Salon', 'STANDARD', true),
(2, 'Salon-up-s', 'STANDARD', true);

-- ============================================================
-- 2. スタッフデータの投入
-- ============================================================
-- パスワードは全て 'password' をBCryptでハッシュ化したものです

-- 認証情報（users）を先に投入
INSERT INTO users (id, name, email, password_hash, is_active, is_system_admin) VALUES
(1, '大浦 智史', 'ohura721@com', '$2a$10$4hInjKtm/Kd.eZf24UBOFOAmgXFyf/ESW0KkpWfQIPhGyn.KExCtK', true, false),
(2, '梶原 洋平', 'yohei@mock.jp', '$2a$10$4hInjKtm/Kd.eZf24UBOFOAmgXFyf/ESW0KkpWfQIPhGyn.KExCtK', true, false),
(3, 'YOKO', 'yoko@mock.jp', '$2a$10$4hInjKtm/Kd.eZf24UBOFOAmgXFyf/ESW0KkpWfQIPhGyn.KExCtK', true, false),
(4, 'システム管理者', 'admin@system.jp', '$2a$10$4hInjKtm/Kd.eZf24UBOFOAmgXFyf/ESW0KkpWfQIPhGyn.KExCtK', true, true);

-- 各サロンごとの所属（staffs）を投入
-- (user_id, salon_id, role)
-- システム管理者（user_id=5）は全店舗アクセス可能なため、staffsへの登録は任意
-- ここでは便宜上サロン1に所属として登録
INSERT INTO staffs (user_id, salon_id, role, is_practitioner, is_active) VALUES
(1, 1, 'ADMIN', true, true),
(2, 2, 'ADMIN', false, true),
(3, 2, 'ADMIN', true, true);

-- ============================================================
-- 3. 顧客データの投入
-- ============================================================

INSERT INTO customers 
(salon_id, line_user_id, line_display_name, last_name, first_name, last_name_kana, first_name_kana, phone_number, email, admin_memo) 
VALUES 
-- パターンA: LINE連携済み & 顧客情報登録済みの優良顧客 (サロン1)
(1, 'U11111111111111111111111111111111', 'Hanako.S', '佐藤', '花子', 'サトウ', 'ハナコ', '090-1111-2222', 'hanako@example.com', '肌が敏感なので注意。コーヒーが好き。'),
-- パターンB: 別サロンの顧客（サロン1の管理画面に出ないことを確認用） (サロン2)
(2, 'U33333333333333333333333333333333', 'Jiro.T', '田中', '三郎', 'タナカ', 'サブロウ', '090-5555-6666', 'jiro@mock.jp', '別店舗の客');

-- ============================================================
-- 4-1. カテゴリマスタのデータ投入 (menu_categories) ★新規追加
-- ============================================================
-- まず「箱（タブ）」を作ります。
INSERT INTO menu_categories 
(id, salon_id, name, display_order) 
VALUES 
-- サロン1 (Carnet) 用カテゴリ
(1, 1, '脱毛メニュー', 1),

-- サロン2 (salon-up-s) 用カテゴリ
(2, 2, '脱毛', 1),
(3, 2, 'マッサージ', 2);

-- ============================================================
-- 4-1.5. メニューセクションのデータ投入 (menu_sections) ★新規追加
-- ============================================================
INSERT INTO menu_sections
(id, menu_category_id, name, display_order)
VALUES
-- サロン1 (Carnet)
(1, 1, '全身', 1),
(2, 1, 'VIO', 2),

-- サロン2 (salon-up-s)
(3, 2, '脱毛', 1),
(4, 3, 'アロマリンパマッサージ～心地よい香りでリラックス', 2),
(5, 3, 'スクラブトリートメント～角質を落として全身美肌', 1),
(6, 3, 'スタンダード', 2),
(7, 3, 'ショート', 3);


-- ============================================================
-- 4-2. メニューマスタのデータ投入 (menus) ★修正
-- ============================================================
-- categoryカラム(ENUM)を廃止し、menu_category_idに変更しています。

INSERT INTO menus 
(id, salon_id, menu_category_id, menu_section_id, title, description, original_price, discounted_price, duration_minutes, item_type, tag, display_order, is_active) 
VALUES 
-- --- サロン1 (Carnet) のメニュー ---
-- カテゴリID:1 (脱毛) に紐付け

-- ID:1 [脱毛] 全身脱毛
(1, 1, 1, 1, '全身脱毛（顔・VIO含む）', '最新マシンを使用した全身脱毛コースです。最短60分で完了します。', 
 22000, NULL, 60, 'MENU', '人気No.1', 1, true),

-- ID:2 [脱毛] VIOセット
(2, 1, 1, 2, 'VIOセット脱毛', 'デリケートゾーンの3点セットです。痛みの少ない施術を心がけています。', 
 8800, NULL, 30, 'MENU', NULL, 2, true),


-- --- サロン2 (salon-up-s) のメニュー ---

-- ▼ クーポン (item_type='COUPON')
-- クーポンもカテゴリID:2 (脱毛) に紐付けておくと、バッジで[脱毛]と表示できます

-- ID:3 全身脱毛クーポン
(3, 2, 2, NULL, '【初回限定】お得クーポン☆全身脱毛', '顔全体/両腕/両脇/胸部/腹部/ヒップ/両脚/背中全体/VIOを全て施術します', 
 28000, 22000, 90, 'COUPON', '新規のみ', 1, true),

-- ID:4 選べる5か所クーポン
(4, 2, 2, NULL, '【初回限定】自由に選べる5か所脱毛', 'ご希望の施術部位を5ヵ所お選び下さい（顔全体/両腕/両脇 など）', 
 19000, 15000, 60, 'COUPON', '新規のみ', 2, true),


-- ▼ 通常メニュー (item_type='MENU')
-- カテゴリID:2 (脱毛) に紐付け

-- ID:5 VIOセット
(5, 2, 2, 3, 'VIOセット ～令和の新常識～', '人気のVIOセットコースです。', 
 6000, NULL, 45, 'MENU', NULL, 10, true),

-- ID:6 Vライン
(6, 2, 2, 3, 'Vライン ～令和の新常識～', NULL, 
 3000, NULL, 30, 'MENU', NULL, 11, true),

-- ID:7 Iライン
(7, 2, 2, 3, 'Iライン ～令和の新常識～', NULL, 
 3000, NULL, 30, 'MENU', NULL, 12, true),

-- ID:8 Oライン
(8, 2, 2, 3, 'Oライン ～令和の新常識～', NULL, 
 3000, NULL, 30, 'MENU', NULL, 13, true),

-- ID:9 男性ヒゲ
(9, 2, 2, 3, '男性ヒゲセット ～毎朝の髭剃りとおさらば～', '男性のお客様限定。毎朝の処理が楽になります。', 
 5000, NULL, 45, 'MENU', 'メンズ', 14, true),


-- ▼ アロマメニュー
-- カテゴリID:3 (アロマ) に紐付け

-- ID:10 120分
(10, 2, 3, 4, 'アロマリンパマッサージ 120分', 'たっぷりと時間をかけて全身の疲れを癒やします。', 
 17000, NULL, 120, 'MENU', NULL, 20, true),

-- ID:11 90分
(11, 2, 3, 4, 'アロマリンパマッサージ 90分', 'スタンダードな90分コースです。', 
 13000, NULL, 90, 'MENU', NULL, 21, true),

-- ID:12 60分
(12, 2, 3, 4, 'アロマリンパマッサージ 60分', 'お試しや時間のあいた時に最適な60分コース。', 
 11000, NULL, 60, 'MENU', NULL, 22, true);


-- ============================================================
-- 5. 予約データの投入 (reservations)
-- ============================================================
-- ここは変更ありませんが、整合性のため記載します。

INSERT INTO reservations 
(id, salon_id, customer_id, staff_id, start_time, end_time, status, total_price, memo) 
VALUES 
-- ID:1 [来店済] 佐藤花子さん / 過去の予約 / スタッフ鈴木担当
(1, 1, 1, 2, '2025-11-01 10:00:00', '2025-11-01 11:30:00', 'VISITED', 9800, '初回のためカウンセリング長めに実施。肌トラブルなし。'),
-- ID:2 [確定] 佐藤花子さん / 未来の予約 / スタッフ山田担当
(2, 1, 1, 1, '2025-12-20 14:00:00', '2025-12-20 15:15:00', 'CONFIRMED', 23100, '前回施術後も良好とのこと。');

-- ============================================================
-- 6. 予約詳細データの投入 (reservation_items)
-- ============================================================
-- ここも変更ありません。Menu IDは維持しています。

INSERT INTO reservation_items 
(reservation_id, menu_id, price_at_booking) 
VALUES 
-- 予約ID:1 (佐藤さん・初回) -> メニューID:2 (VIOセット)
(1, 2, 9800),

-- 予約ID:2 (佐藤さん・2回目) -> メニューID:1 (全身脱毛)
(2, 1, 22000);
