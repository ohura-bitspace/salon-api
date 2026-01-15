# salon-api

# イメージをビルド
docker build -t salon-api:latest .

# または、タグ付き
docker build -t salon-api:0.0.1 .

# イメージをビルドしてコンテナを起動
docker-compose up --build