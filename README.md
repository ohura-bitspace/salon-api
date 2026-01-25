# salon-api

# イメージをビルド
docker build -t salon-api:latest .

# または、タグ付き
docker build -t salon-api:0.0.1 .

# イメージをビルドしてコンテナを起動
docker-compose up --build

# コンテナと「ボリューム（データ保持領域）」を削除
docker-compose down -v

# バックエンド（Java）イメージの保存
docker save -o salon-api.tar salon-api:latest
# フロントエンド（React）イメージの保存
docker save -o salon-frontend.tar salon-frontend:latest

# メモリ監視
docker stats